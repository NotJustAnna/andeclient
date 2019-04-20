package pw.aru.lib.andeclient.entities;

import pw.aru.lib.andeclient.entities.player.PlayerControls;
import pw.aru.lib.andeclient.entities.player.PlayerInfo;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

@SuppressWarnings("unused")
public interface AndePlayer extends PlayerInfo, PlayerControls {
    @CheckReturnValue
    @Nonnull
    AndesiteNode connectedNode();

    @CheckReturnValue
    @Nonnull
    AndeClient client();

    @CheckReturnValue
    @Nonnull
    PlayerState state();

    @CheckReturnValue
    @Nonnegative
    long guildId();

    @Nonnull
    @CheckReturnValue
    CompletionStage<AudioLoadResult> loadTracksAsync(@Nonnull final String identifier);

    void destroy();
}