package pw.aru.libs.andeclient.events;

import pw.aru.libs.andeclient.entities.AndeClient;
import pw.aru.libs.andeclient.entities.AndePlayer;
import pw.aru.libs.andeclient.entities.AndesiteNode;

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
