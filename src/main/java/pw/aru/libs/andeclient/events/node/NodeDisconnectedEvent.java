package pw.aru.libs.andeclient.events.node;

import org.immutables.value.Value;
import pw.aru.libs.andeclient.annotations.Event;
import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.events.AndesiteNodeEvent;
import pw.aru.libs.andeclient.events.EventType;

import javax.annotation.Nonnull;

@Value.Immutable
@Event
public abstract class NodeDisconnectedEvent implements AndesiteNodeEvent {
    @Override
    @Nonnull
    @Value.Parameter
    public abstract AndesiteNode node();

    @Nonnull
    @Value.Parameter
    public abstract Reason reason();

    @Override
    @Nonnull
    public EventType<NodeDisconnectedEvent> type() {
        return EventType.NODE_DISCONNECTED_EVENT;
    }

    public enum Reason {
        CLOSED_BY_SERVER, WEBSOCKET_ERROR, TIMED_OUT
    }
}
