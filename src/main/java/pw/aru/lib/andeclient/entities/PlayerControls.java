package pw.aru.lib.andeclient.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface PlayerControls {
    @Nonnull
    AndeClient client();

    Play play();

    Action pause(boolean isPaused);

    Action volume(int volume);

    Mixer mixer();

    Action filters(PlayerFilter... filters);

    Action seek(long position);

    Action stop();

    interface Action {
        PlayerControls execute();
    }

    interface Play extends Action {
        @Nonnull
        @CheckReturnValue
        Play track(String trackString);

        @Nonnull
        @CheckReturnValue
        Play track(AudioTrack track);

        @Nonnull
        @CheckReturnValue
        Play start(long timestamp);

        @Nonnull
        @CheckReturnValue
        Play end(long timestamp);

        @Nonnull
        @CheckReturnValue
        Play noReplace();

        @Nonnull
        @CheckReturnValue
        Play replacing();

        @Nonnull
        @CheckReturnValue
        Play pause(boolean isPaused);

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

        //TODO Implement
    }

}