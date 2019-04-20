package pw.aru.lib.andeclient.events;

import pw.aru.lib.andeclient.entities.AndeClient;
import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.entities.AndesiteNode;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public interface AndePlayerEvent extends AndeClientEvent, AndesiteNodeEvent {
    @Nonnull
    @CheckReturnValue
    AndePlayer player();

    @Nonnull
    @Override
    default AndeClient client() {
        return player().client();
    }

    @Nonnull
    @Override
    default AndesiteNode node() {
        return player().connectedNode();
    }

    @Nonnegative
    @CheckReturnValue
    default long guildId() {
        return player().guildId();
    }
}
