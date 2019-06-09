package pw.aru.libs.andeclient.events.player.update;

import org.immutables.value.Value;
import pw.aru.libs.andeclient.annotations.Event;
import pw.aru.libs.andeclient.entities.AndePlayer;
import pw.aru.libs.andeclient.entities.player.PlayerFilter;
import pw.aru.libs.andeclient.events.AndePlayerEvent;
import pw.aru.libs.andeclient.events.EventType;

import javax.annotation.Nonnull;
import java.util.Collection;

@Value.Immutable
@Event
public abstract class PlayerFilterUpdateEvent implements AndePlayerEvent {
    @Override
    @Nonnull
    public abstract AndePlayer player();

    @Nonnull
    public abstract Collection<? extends PlayerFilter> filters();

    @Nonnull
    public abstract Collection<? extends PlayerFilter> oldFilters();

    @Override
    @Nonnull
    public EventType<PlayerFilterUpdateEvent> type() {
        return EventType.PLAYER_FILTER_UPDATE_EVENT;
    }
}
