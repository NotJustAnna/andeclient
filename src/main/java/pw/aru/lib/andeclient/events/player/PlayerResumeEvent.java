package pw.aru.lib.andeclient.events.player;

import org.immutables.value.Value;
import pw.aru.lib.andeclient.annotations.Event;
import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.events.AndePlayerEvent;
import pw.aru.lib.andeclient.events.EventType;

import javax.annotation.Nonnull;

@Value.Immutable
@Event
public abstract class PlayerResumeEvent implements AndePlayerEvent {
    @Override
    @Nonnull
    @Value.Parameter
    public abstract AndePlayer player();

    @Override
    @Nonnull
    public EventType<PlayerResumeEvent> type() {return EventType.PLAYER_RESUME_EVENT;}
}
