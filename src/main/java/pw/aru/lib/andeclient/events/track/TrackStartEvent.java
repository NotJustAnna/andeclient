package pw.aru.lib.andeclient.events.track;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.immutables.value.Value;
import pw.aru.lib.andeclient.annotations.Event;
import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.events.EventType;
import pw.aru.lib.andeclient.events.RemoteTrackEvent;

import javax.annotation.Nonnull;

@Value.Immutable
@Event
public abstract class TrackStartEvent implements RemoteTrackEvent {
    @Override
    @Nonnull
    public abstract AndePlayer player();

    @Override
    @Nonnull
    public abstract AudioTrack track();

    @Override
    @Nonnull
    public EventType<TrackStartEvent> type() {
        return EventType.TRACK_START_EVENT;
    }
}
