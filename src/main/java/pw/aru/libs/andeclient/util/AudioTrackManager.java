package pw.aru.libs.andeclient.util;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.remote.RemoteNodeRegistry;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.OrderedExecutor;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.lava.common.tools.ExecutorTools;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.FAULT;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

/**
 * Implementation of audio player manager that only resolves tracks.
 */
public class AudioTrackManager implements AudioPlayerManager {
    private static final int TRACK_INFO_VERSIONED = 1;
    private static final int TRACK_INFO_VERSION = 2;

    private static final int MAXIMUM_LOAD_REDIRECTS = 5;

    private static final Logger log = LoggerFactory.getLogger(AudioTrackManager.class);

    private final List<AudioSourceManager> sourceManagers = new ArrayList<>();
    private final OrderedExecutor orderedForkJoinExecutor = new OrderedExecutor(ForkJoinPool.commonPool());

    @Override
    public void shutdown() {
        for (AudioSourceManager sourceManager : sourceManagers) {
            sourceManager.shutdown();
        }
    }

    @Override
    public void registerSourceManager(AudioSourceManager sourceManager) {
        sourceManagers.add(sourceManager);
    }

    @Override
    public <T extends AudioSourceManager> T source(Class<T> c) {
        for (AudioSourceManager sourceManager : sourceManagers) {
            if (c.isAssignableFrom(sourceManager.getClass())) {
                return c.cast(sourceManager);
            }
        }

        return null;
    }

    @Override
    public Future<Void> loadItem(final String identifier, final AudioLoadResultHandler resultHandler) {
        try {
            return ForkJoinPool.commonPool().submit(createItemLoader(identifier, resultHandler));
        } catch (RejectedExecutionException e) {
            return handleLoadRejected(identifier, resultHandler, e);
        }
    }

    @Override
    public Future<Void> loadItemOrdered(Object orderingKey, final String identifier, final AudioLoadResultHandler resultHandler) {
        try {
            return orderedForkJoinExecutor.submit(orderingKey, createItemLoader(identifier, resultHandler));
        } catch (RejectedExecutionException e) {
            return handleLoadRejected(identifier, resultHandler, e);
        }
    }

    private Future<Void> handleLoadRejected(String identifier, AudioLoadResultHandler resultHandler, RejectedExecutionException e) {
        FriendlyException exception = new FriendlyException("Cannot queue loading a track, queue is full.", SUSPICIOUS, e);
        ExceptionTools.log(log, exception, "queueing item " + identifier);

        resultHandler.loadFailed(exception);

        return ExecutorTools.COMPLETED_VOID;
    }

    private Callable<Void> createItemLoader(final String identifier, final AudioLoadResultHandler resultHandler) {
        return () -> {
            boolean[] reported = new boolean[1];

            try {
                if (!checkSourcesForItem(new AudioReference(identifier, null), resultHandler, reported)) {
                    log.debug("No matches for track with identifier {}.", identifier);
                    resultHandler.noMatches();
                }
            } catch (Throwable throwable) {
                if (reported[0]) {
                    log.warn("Load result handler for {} threw an exception", identifier, throwable);
                } else {
                    dispatchItemLoadFailure(identifier, resultHandler, throwable);
                }

                ExceptionTools.rethrowErrors(throwable);
            }

            return null;
        };
    }

    private boolean checkSourcesForItem(AudioReference reference, AudioLoadResultHandler resultHandler, boolean[] reported) {
        AudioReference currentReference = reference;

        for (int redirects = 0; redirects < MAXIMUM_LOAD_REDIRECTS && currentReference.identifier != null; redirects++) {
            AudioItem item = checkSourcesForItemOnce(currentReference, resultHandler, reported);
            if (item == null) {
                return false;
            } else if (!(item instanceof AudioReference)) {
                return true;
            }
            currentReference = (AudioReference) item;
        }

        return false;
    }

    private AudioItem checkSourcesForItemOnce(AudioReference reference, AudioLoadResultHandler resultHandler, boolean[] reported) {
        for (AudioSourceManager sourceManager : sourceManagers) {
            if (reference.containerDescriptor != null && !(sourceManager instanceof ProbingAudioSourceManager)) {
                continue;
            }

            AudioItem item = sourceManager.loadItem(null, reference);

            if (item != null) {
                if (item instanceof AudioTrack) {
                    log.debug("Loaded a track with identifier {} using {}.", reference.identifier, sourceManager.getClass().getSimpleName());
                    reported[0] = true;
                    resultHandler.trackLoaded((AudioTrack) item);
                } else if (item instanceof AudioPlaylist) {
                    log.debug("Loaded a playlist with identifier {} using {}.", reference.identifier, sourceManager.getClass().getSimpleName());
                    reported[0] = true;
                    resultHandler.playlistLoaded((AudioPlaylist) item);
                }
                return item;
            }
        }

        return null;
    }

    private void dispatchItemLoadFailure(String identifier, AudioLoadResultHandler resultHandler, Throwable throwable) {
        FriendlyException exception = ExceptionTools.wrapUnfriendlyExceptions("Something went wrong when looking up the track", FAULT, throwable);
        ExceptionTools.log(log, exception, "loading item " + identifier);

        resultHandler.loadFailed(exception);
    }

    @Override
    public void encodeTrack(MessageOutput stream, AudioTrack track) throws IOException {
        DataOutput output = stream.startMessage();
        output.write(TRACK_INFO_VERSION);

        AudioTrackInfo trackInfo = track.getInfo();
        output.writeUTF(trackInfo.title);
        output.writeUTF(trackInfo.author);
        output.writeLong(trackInfo.length);
        output.writeUTF(trackInfo.identifier);
        output.writeBoolean(trackInfo.isStream);
        DataFormatTools.writeNullableText(output, trackInfo.uri);

        encodeTrackDetails(track, output);
        output.writeLong(track.getPosition());

        stream.commitMessage(TRACK_INFO_VERSIONED);
    }

    @Override
    public DecodedTrackHolder decodeTrack(MessageInput stream) throws IOException {
        DataInput input = stream.nextMessage();
        if (input == null) {
            return null;
        }

        int version = (stream.getMessageFlags() & TRACK_INFO_VERSIONED) != 0 ? (input.readByte() & 0xFF) : 1;

        AudioTrackInfo trackInfo = new AudioTrackInfo(input.readUTF(), input.readUTF(), input.readLong(), input.readUTF(),
            input.readBoolean(), version >= 2 ? DataFormatTools.readNullableText(input) : null);
        AudioTrack track = decodeTrackDetails(trackInfo, input);
        long position = input.readLong();

        if (track != null) {
            track.setPosition(position);
        }

        stream.skipRemainingBytes();

        return new DecodedTrackHolder(track);
    }

    private void encodeTrackDetails(AudioTrack track, DataOutput output) throws IOException {
        AudioSourceManager sourceManager = track.getSourceManager();
        output.writeUTF(sourceManager.getSourceName());
        sourceManager.encodeTrack(track, output);
    }

    private AudioTrack decodeTrackDetails(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        String sourceName = input.readUTF();

        for (AudioSourceManager sourceManager : sourceManagers) {
            if (sourceName.equals(sourceManager.getSourceName())) {
                return sourceManager.decodeTrack(trackInfo, input);
            }
        }

        return null;
    }

    // EVERYTHING UNSUPPORTED HERE

    @Override
    public void setHttpBuilderConfigurator(Consumer<HttpClientBuilder> ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHttpRequestConfigurator(Function<RequestConfig, RequestConfig> ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setItemLoaderThreadPoolSize(int ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void useRemoteNodes(String... ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableGcMonitoring() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AudioConfiguration getConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUsingSeekGhosting() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUseSeekGhosting(boolean ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFrameBufferDuration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFrameBufferDuration(int ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTrackStuckThreshold(long ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPlayerCleanupThreshold(long ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AudioPlayer createPlayer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RemoteNodeRegistry getRemoteNodeRegistry() {
        throw new UnsupportedOperationException();
    }
}
