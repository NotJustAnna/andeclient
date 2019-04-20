package pw.aru.lib.andeclient.events.node;

import org.immutables.value.Value;
import pw.aru.lib.andeclient.annotations.Event;
import pw.aru.lib.andeclient.entities.AndesiteNode;
import pw.aru.lib.andeclient.events.AndesiteNodeEvent;
import pw.aru.lib.andeclient.events.EventType;

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
