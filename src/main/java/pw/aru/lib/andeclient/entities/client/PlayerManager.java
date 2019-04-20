package pw.aru.lib.andeclient.entities.client;

import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.entities.configurator.AndePlayerConfigurator;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This interface is the part of the AndeClient responsible of managing the music players.
 */
public interface PlayerManager {
    @Nonnull
    @CheckReturnValue
    AndePlayerConfigurator newPlayer();

    @Nonnull
    @CheckReturnValue
    AndePlayer newPlayer(@Nonnegative final long guildId);

    @Nonnull
    @CheckReturnValue
    List<AndePlayer> players();

    @Nullable
    @CheckReturnValue
    AndePlayer player(@Nonnegative final long guildId);

    @Nonnull
    AndePlayer removePlayer(@Nonnegative final long guildId, final boolean shouldDestroy);

    @Nonnull
    default AndePlayer removePlayer(@Nonnegative final long guildId) {
        return removePlayer(guildId, true);
    }
}
