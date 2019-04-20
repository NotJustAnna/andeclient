package pw.aru.lib.andeclient.events.player;

import org.immutables.value.Value;
import pw.aru.lib.andeclient.annotations.Event;
import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.events.AndePlayerEvent;
import pw.aru.lib.andeclient.events.EventType;

import javax.annotation.Nonnull;

@Value.Immutable
@Event
public abstract class WebSocketClosedEvent implements AndePlayerEvent {
    @Override
    @Nonnull
    public abstract AndePlayer player();

    @Nonnull
    public abstract String reason();

    public abstract boolean byRemote();

    public abstract int closeCode();

    @Override
    @Nonnull
    public EventType<WebSocketClosedEvent> type() {
        return EventType.WEB_SOCKET_CLOSED_EVENT;
    }
}
