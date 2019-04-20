package pw.aru.lib.andeclient.events;

import pw.aru.lib.andeclient.entities.AndeClient;
import pw.aru.lib.andeclient.entities.AndesiteNode;

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