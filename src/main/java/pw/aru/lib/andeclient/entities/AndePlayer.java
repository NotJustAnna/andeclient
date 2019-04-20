package pw.aru.lib.andeclient.entities;

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
    PlayerState state();

    @CheckReturnValue
    @Nonnegative
    long guildId();

    void handleVoiceServerUpdate(String sessionId, String voiceToken, String endpoint);

    void destroy();
}