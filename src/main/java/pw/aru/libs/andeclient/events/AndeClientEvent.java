package pw.aru.libs.andeclient.events;

import pw.aru.libs.andeclient.entities.AndeClient;

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
