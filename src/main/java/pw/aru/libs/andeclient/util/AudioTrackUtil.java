package pw.aru.libs.andeclient.util;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.iharder.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.aru.libs.andeclient.exceptions.LocalTrackException;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AudioTrackUtil {
    private static final AudioPlayerManager manager = new DefaultAudioPlayerManager();
    private static final Logger logger = LoggerFactory.getLogger(AudioTrackUtil.class);

    static {
        manager.registerSourceManager(new YoutubeAudioSourceManager(true));
        manager.registerSourceManager(new SoundCloudAudioSourceManager(true));
        manager.registerSourceManager(new BeamAudioSourceManager());
        manager.registerSourceManager(new BandcampAudioSourceManager());
        manager.registerSourceManager(new TwitchStreamAudioSourceManager());
        manager.registerSourceManager(new HttpAudioSourceManager());
        manager.registerSourceManager(new VimeoAudioSourceManager());
    }

    @CheckReturnValue
    @Nonnull
    public static AudioTrack fromString(@Nonnull final String data) {
        try {
            return manager.decodeTrack(new MessageInput(new ByteArrayInputStream(Base64.decode(data)))).decodedTrack;
        } catch (final IOException e) {
            logger.error("error when decoding track: {} | {}", data, e);
            throw new LocalTrackException(e);
        }
    }

    @CheckReturnValue
    @Nonnull
    public static String fromTrack(@Nonnull final AudioTrack track) {
        try {
            final var stream = new ByteArrayOutputStream();
            manager.encodeTrack(new MessageOutput(stream), track);
            return Base64.encodeBytes(stream.toByteArray());
        } catch (final IOException e) {
            logger.error("error when encoding track: {} | {}", track.getIdentifier(), e);
            throw new LocalTrackException(e);
        }
    }
}
