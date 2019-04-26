package pw.aru.lib.andeclient.entities.client;

import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.entities.configurator.internal.ActualAndePlayerConfigurator;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This interface is the part of the AndeClient responsible of managing the music players.
 */
public interface PlayerManager {
    /**
     * Creates a configurator which will add a player to this manager after configurated.
     *
     * @return a player configurator bound to the AndeClient.
     */
    @Nonnull
    @CheckReturnValue
    ActualAndePlayerConfigurator newPlayer();

    /**
     * Gets or creates a player with the best node of the node manager.
     *
     * @param guildId the player's guild id.
     * @return an existing player, if it already exists, or a newly-created one.
     */
    @Nonnull
    @CheckReturnValue
    AndePlayer newPlayer(@Nonnegative final long guildId);

    /**
     * Retrieves all players from the client.
     *
     * @return a read-only list of the current players.
     */
    @Nonnull
    @CheckReturnValue
    List<AndePlayer> players();

    /**
     * Retrieves a player by it's guild id.
     * @param guildId the player's guild id.
     * @return a player, if it exists, otherwise null.
     */
    @Nullable
    @CheckReturnValue
    AndePlayer player(@Nonnegative final long guildId);
}
