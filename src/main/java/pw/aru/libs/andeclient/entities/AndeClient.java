package pw.aru.libs.andeclient.entities;

import pw.aru.libs.andeclient.entities.client.EventManager;
import pw.aru.libs.andeclient.entities.client.NodeManager;
import pw.aru.libs.andeclient.entities.client.PlayerManager;
import pw.aru.libs.andeclient.entities.configurator.internal.ActualAndeClientConfigurator;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;

/**
 * The core of AndeClient. Acts as the manager of all nodes and players, and event manager.
 */
@SuppressWarnings("unused")
public interface AndeClient extends NodeManager, PlayerManager, EventManager {
    /**
     * Creates a new AndeClient.
     *
     * @param userId the bot's user id.
     * @return a AndeClient configurator.
     */
    static ActualAndeClientConfigurator andeClient(@Nonnegative long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("user id cannot be smaller than or equal to 0");
        }

        return new ActualAndeClientConfigurator().userId(userId);
    }

    /**
     * The bot's user id.
     *
     * @return the long representing the bot id.
     */
    @Nonnegative
    @CheckReturnValue
    long userId();

    /**
     * Shutdowns this AndeClient, destroying all players and nodes and freeing all used resources.
     */
    void shutdown();
}
