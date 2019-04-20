package pw.aru.lib.andeclient.entities.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

public interface PlayerInfo {
    @CheckReturnValue
    @Nullable
    AudioTrack playingTrack();

    @CheckReturnValue
    @Nonnegative
    long timestamp();

    @CheckReturnValue
    @Nonnegative
    long position();

    @CheckReturnValue
    @Nonnegative
    int volume();

    @CheckReturnValue
    boolean paused();
}
