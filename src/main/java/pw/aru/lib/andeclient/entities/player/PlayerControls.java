package pw.aru.lib.andeclient.entities.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pw.aru.lib.andeclient.entities.AndeClient;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Control interface of an AndePlayer.
 * Sends control actions to the player's andesite node.
 */
public interface PlayerControls {
    @Nonnull
    @CheckReturnValue
    AndeClient client();

    @Nonnull
    @CheckReturnValue
    Play play();

    @Nonnull
    @CheckReturnValue
    Action pause(boolean isPaused);

    @Nonnull
    @CheckReturnValue
    Action volume(int volume);

    @Nonnull
    @CheckReturnValue
    Mixer mixer();

    @Nonnull
    @CheckReturnValue
    Action filters(PlayerFilter... filters);

    @Nonnull
    @CheckReturnValue
    Action seek(long position);

    @Nonnull
    @CheckReturnValue
    Action stop();

    interface Action {
        @Nonnull
        PlayerControls execute();
    }

    interface Play extends Action {
        @Nonnull
        @CheckReturnValue
        Play track(@Nonnull String trackString);

        @Nonnull
        @CheckReturnValue
        Play track(@Nonnull AudioTrack track);

        @Nonnull
        @CheckReturnValue
        Play start(@Nullable Long timestamp);

        @Nonnull
        @CheckReturnValue
        Play end(@Nullable Long timestamp);

        @Nonnull
        @CheckReturnValue
        Play noReplace();

        @Nonnull
        @CheckReturnValue
        Play replacing();

        @Nonnull
        @CheckReturnValue
        Play pause(@Nullable Boolean isPaused);

        @Nonnull
        @CheckReturnValue
        Play volume(@Nullable Integer volume);
    }

    interface Mixer extends Action {
        @Nonnull
        @CheckReturnValue
        Mixer enable();

        @Nonnull
        @CheckReturnValue
        Mixer disable();
    }
}
