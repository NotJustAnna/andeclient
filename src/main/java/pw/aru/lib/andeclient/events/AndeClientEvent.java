package pw.aru.lib.andeclient.events;

import pw.aru.lib.andeclient.entities.AndeClient;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface AndeClientEvent {
    @Nonnull
    @CheckReturnValue
    AndeClient client();

    @Nonnull
    @CheckReturnValue
    EventType type();
}
