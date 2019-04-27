package pw.aru.libs.andeclient.events.node;

import org.immutables.value.Value;
import pw.aru.libs.andeclient.annotations.Event;
import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.events.AndesiteNodeEvent;
import pw.aru.libs.andeclient.events.EventType;

import javax.annotation.Nonnull;

@Value.Immutable
@Event
public abstract class NodeConnectedEvent implements AndesiteNodeEvent {
    @Override
    @Nonnull
    @Value.Parameter
    public abstract AndesiteNode node();

    @Override
    @Nonnull
    public EventType<NodeConnectedEvent> type() {
        return EventType.NODE_CONNECTED_EVENT;
    }
}
