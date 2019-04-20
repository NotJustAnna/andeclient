package pw.aru.lib.andeclient.entities.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pw.aru.lib.andeclient.util.AudioTrackUtil;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface PlayerControls {
    void stop();

    void pause();

    void resume();

    void seek(@Nonnegative final long position);

    void volume(@Nonnegative final int volume);

    void play(
        @Nonnull final String trackData,
        @Nonnegative final long startTime,
        @Nonnegative final long endTime,
        final boolean noReplace
    );

    default void play(
        @Nonnull final AudioTrack track,
        @Nonnegative final long startTime,
        @Nonnegative final long endTime,
        final boolean noReplace
    ) {
        play(AudioTrackUtil.fromTrack(track), startTime, endTime, noReplace);
    }

    default void play(
        @Nonnull final AudioTrack track,
        @Nonnegative final long startTime,
        @Nonnegative final long endTime
    ) {
        play(track, startTime, endTime, false);
    }

    default void play(
        @Nonnull final AudioTrack track,
        @Nonnegative final long startTime
    ) {
        play(track, startTime, track.getDuration());
    }

    default void play(
        @Nonnull final AudioTrack track,
        final boolean noReplace
    ) {
        play(track, 0, track.getDuration(), noReplace);
    }

    default void play(@Nonnull final AudioTrack track) {
        play(track, false);
    }
}
