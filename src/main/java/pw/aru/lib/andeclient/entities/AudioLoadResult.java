package pw.aru.lib.andeclient.entities;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.immutables.value.Value;
import pw.aru.lib.andeclient.annotations.SimpleData;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents the Result of trying to get results for an identifier using the node endpoint
 */
public interface AudioLoadResult {
    /**
     * No matches were found by Andesite.
     */
    AudioLoadResult NO_MATCHES = new AudioLoadResult() {
        @Override
        public String toString() {
            return "AudioLoadResult#NO_MATCHES";
        }
    };

    /**
     * AndeClient couldn't figure out the Audio Load Result.
     */
    AudioLoadResult UNKNOWN = new AudioLoadResult() {
        @Override
        public String toString() {
            return "AudioLoadResult#UNKNOWN";
        }
    };

    /**
     * A Playlist or Search Result was found by Andesite.
     */
    @SimpleData
    @Value.Immutable
    interface Playlist extends AudioLoadResult {
        /**
         * Returns if this result is meant to be search results.
         *
         * @return true if the result was a search result, false if the result was a playlist.
         */
        @CheckReturnValue
        boolean searchResults();

        /**
         * Returns the name of the playlist.
         * @return the playlist name string.
         */
        @CheckReturnValue
        @Nonnull
        String playlistName();

        /**
         * Returns the tracks of the playlist.
         * @return a list of audio tracks.
         */
        @CheckReturnValue
        @Nonnull
        List<AudioTrack> tracks();

        /**
         * Returns the selected track of the playlist, if any.
         * @return an audio track, if there was a selected track, or null.
         */
        @CheckReturnValue
        @Nullable
        AudioTrack selectedTrack();

        /**
         * Returns the index of the selected index, or -1 if none.
         *
         * @return the index.
         */
        @CheckReturnValue
        int selectedIndex();
    }

    /**
     * A Single Track was found by Andesite.
     */
    @SimpleData
    @Value.Immutable
    interface Track extends AudioLoadResult {
        /**
         * The Track found by Andesite.
         * @return an audio track.
         */
        @CheckReturnValue
        @Nonnull
        AudioTrack track();
    }

    /**
     * Andesite failed trying to load the results and errored.
     */
    @SimpleData
    @Value.Immutable
    interface Failed extends AudioLoadResult {
        /**
         * The cause of the error.
         * @return the cause string.
         */
        @CheckReturnValue
        @Nonnull
        String cause();

        /**
         * The severity of the error.
         * @return a enum representing the severity.
         */
        @CheckReturnValue
        @Nonnull
        FriendlyException.Severity severity();

        /**
         * Returns this result as an Exception.
         *
         * @return a friendly exception.
         */
        default FriendlyException asException() {
            return new FriendlyException(cause(), severity(), null);
        }
    }
}
