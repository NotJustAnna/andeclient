package pw.aru.libs.andeclient.events;

import pw.aru.libs.andeclient.entities.AndeClient;
import pw.aru.libs.andeclient.entities.AndesiteNode;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface AndesiteNodeEvent extends AndeClientEvent {
    @Nonnull
    @CheckReturnValue
    AndesiteNode node();

    @Nonnull
    @Override
    default AndeClient client() {
        return node().client();
    }
}