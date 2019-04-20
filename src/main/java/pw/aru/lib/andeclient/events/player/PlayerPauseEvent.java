package pw.aru.lib.andeclient.events.player;

import org.immutables.value.Value;
import pw.aru.lib.andeclient.annotations.Event;
import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.events.AndePlayerEvent;
import pw.aru.lib.andeclient.events.EventType;

import javax.annotation.Nonnull;

@Value.Immutable
@Event
public abstract class PlayerPauseEvent implements AndePlayerEvent {
    @Override
    @Nonnull
    public abstract AndePlayer player();

    @Override
    @Nonnull
    public EventType<PlayerPauseEvent> type() {
        return EventType.PLAYER_PAUSE_EVENT;
    }
}
