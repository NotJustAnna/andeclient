package pw.aru.libs.andeclient.events.track;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.immutables.value.Value;
import pw.aru.libs.andeclient.annotations.Event;
import pw.aru.libs.andeclient.entities.AndePlayer;
import pw.aru.libs.andeclient.events.EventType;
import pw.aru.libs.andeclient.events.RemoteTrackEvent;
import pw.aru.libs.andeclient.exceptions.RemoteTrackException;

import javax.annotation.Nonnull;

@Value.Immutable
@Event
public abstract class TrackExceptionEvent implements RemoteTrackEvent {
    @Override
    @Nonnull
    public abstract AndePlayer player();

    @Override
    @Nonnull
    public abstract AudioTrack track();

    @Nonnull
    public abstract RemoteTrackException exception();

    @Override
    @Nonnull
    public EventType<TrackExceptionEvent> type() {
        return EventType.TRACK_EXCEPTION_EVENT;
    }
}
