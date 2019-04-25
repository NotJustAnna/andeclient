package pw.aru.lib.andeclient.entities;

import pw.aru.lib.andeclient.entities.player.PlayerControls;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public interface AndePlayer {
    @CheckReturnValue
    @Nonnull
    AndesiteNode connectedNode();

    @CheckReturnValue
    @Nonnull
    AndeClient client();

    @CheckReturnValue
    @Nonnull
    PlayerControls controls();

    @CheckReturnValue
    @Nonnegative
    long guildId();

    @CheckReturnValue
    @Nonnegative
    long serverTime();

    @CheckReturnValue
    @Nonnegative
    long position();

    @CheckReturnValue
    @Nonnegative
    int volume();

    @CheckReturnValue
    boolean isPaused();

    void handleVoiceServerUpdate(String sessionId, String voiceToken, String endpoint);

    void destroy();
}