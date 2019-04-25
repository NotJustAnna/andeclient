package pw.aru.lib.andeclient.entities;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.immutables.value.Value;
import pw.aru.lib.andeclient.annotations.SimpleData;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface AudioLoadResult {
    AudioLoadResult NO_MATCHES = new AudioLoadResult() {};

    @SimpleData
    @Value.Immutable
    interface Playlist extends AudioLoadResult {
        @CheckReturnValue
        boolean searchResults();

        @CheckReturnValue
        @Nonnull
        String playlistName();

        @CheckReturnValue
        @Nonnull
        List<AudioTrack> tracks();

        @CheckReturnValue
        @Nullable
        AudioTrack selectedTrack();
    }

    @SimpleData
    @Value.Immutable
    interface Track extends AudioLoadResult {
        @CheckReturnValue
        @Nonnull
        AudioTrack loadedTrack();
    }

    @SimpleData
    @Value.Immutable
    interface Failed extends AudioLoadResult {
        @CheckReturnValue
        @Nonnull
        String cause();

        @CheckReturnValue
        @Nonnull
        FriendlyException.Severity severity();

        default Exception asThrowable() {
            return new FriendlyException(cause(), severity(), null);
        }
    }
}
