package pw.aru.lib.andeclient.events.track;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.immutables.value.Value;
import pw.aru.lib.andeclient.annotations.Event;
import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.events.EventType;
import pw.aru.lib.andeclient.events.RemoteTrackEvent;

import javax.annotation.Nonnull;

@Value.Immutable
@Event
public abstract class TrackEndEvent implements RemoteTrackEvent {
    @Override
    @Nonnull
    public abstract AndePlayer player();

    @Override
    @Nonnull
    public abstract AudioTrack track();

    @Nonnull
    public abstract AudioTrackEndReason reason();

    @Override
    @Nonnull
    public EventType<TrackEndEvent> type() {
        return EventType.TRACK_END_EVENT;
    }
}
