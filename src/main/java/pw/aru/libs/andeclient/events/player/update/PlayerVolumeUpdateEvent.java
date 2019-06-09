package pw.aru.libs.andeclient.events.player.update;

import org.immutables.value.Value;
import pw.aru.libs.andeclient.annotations.Event;
import pw.aru.libs.andeclient.entities.AndePlayer;
import pw.aru.libs.andeclient.events.AndePlayerEvent;
import pw.aru.libs.andeclient.events.EventType;

import javax.annotation.Nonnull;

@Value.Immutable
@Event
public abstract class PlayerVolumeUpdateEvent implements AndePlayerEvent {
    @Override
    @Nonnull
    @Value.Parameter
    public abstract AndePlayer player();

    @Value.Parameter
    public abstract int value();

    @Value.Parameter
    public abstract int oldValue();

    @Override
    @Nonnull
    public EventType<PlayerVolumeUpdateEvent> type() {
        return EventType.PLAYER_VOLUME_UPDATE_EVENT;
    }
}
