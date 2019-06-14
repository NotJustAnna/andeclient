package pw.aru.libs.andeclient.events.node;

import org.immutables.value.Value;
import pw.aru.libs.andeclient.annotations.Event;
import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.events.AndesiteNodeEvent;
import pw.aru.libs.andeclient.events.EventType;

import javax.annotation.Nonnull;

@Value.Immutable
@Event
public abstract class NodeStatsEvent implements AndesiteNodeEvent {
    @Nonnull
    @Override
    public AndesiteNode node() {
        return stats().node();
    }

    @Nonnull
    @Value.Parameter
    public abstract AndesiteNode.Stats stats();

    @Override
    @Nonnull
    public EventType<NodeStatsEvent> type() {
        return EventType.NODE_STATS_EVENT;
    }
}
