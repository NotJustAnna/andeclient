package pw.aru.libs.andeclient.events.track;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.immutables.value.Value;
import pw.aru.libs.andeclient.annotations.Event;
import pw.aru.libs.andeclient.entities.AndePlayer;
import pw.aru.libs.andeclient.events.EventType;
import pw.aru.libs.andeclient.events.RemoteTrackEvent;

import javax.annotation.Nonnull;

@Value.Immutable
@Event
public abstract class TrackStuckEvent implements RemoteTrackEvent {
    @Override
    @Nonnull
    public abstract AndePlayer player();

    @Override
    @Nonnull
    public abstract AudioTrack track();

    public abstract long thresholdMs();

    @Override
    @Nonnull
    public EventType<TrackStuckEvent> type() {
        return EventType.TRACK_STUCK_EVENT;
    }
}
