package pw.aru.libs.andeclient.events;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface RemoteTrackEvent extends AndePlayerEvent {
    @Nonnull
    @CheckReturnValue
    AudioTrack track();
}
