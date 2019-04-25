package pw.aru.lib.andeclient.entities;

import pw.aru.lib.andeclient.entities.player.PlayerControls;
import pw.aru.lib.andeclient.events.AndeClientEvent;
import pw.aru.lib.andeclient.events.AndePlayerEvent;
import pw.aru.lib.andeclient.events.EventType;
import pw.aru.lib.eventpipes.api.EventConsumer;
import pw.aru.lib.eventpipes.api.EventSubscription;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A player connected to an andesite node. Can play music
 */
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

    /**
     * Handles a "voice server update" sent by Discord.
     *
     * @param sessionId  discord's voice session id, provided by your Discord library.
     * @param voiceToken discord's voice token, provided by your Discord library.
     * @param endpoint   discord's voice endpoint, provided by your Discord library.
     */
    void handleVoiceServerUpdate(String sessionId, String voiceToken, String endpoint);

    /**
     * Destroys the player.
     */
    void destroy();

    /**
     * Add an event consumer for all events of this player with the given handler callback.
     *
     * @param consumer The consumer for events.
     * @return The Event subscription, so you can close it later.
     */
    EventSubscription<AndeClientEvent> on(EventConsumer<AndePlayerEvent> consumer);

    /**
     * Add a consumer for the specified event type of this player with the given handler callback.
     *
     * @param type     The type of event to listen on.
     * @param consumer The consumer for events.
     * @param <T>      The object type of event being listened on.
     * @return The Event subscription, so you can close it later.
     */
    @SuppressWarnings("unchecked")
    default <T extends AndePlayerEvent> EventSubscription<AndeClientEvent> on(EventType<T> type, EventConsumer<T> consumer) {
        return on(event -> {
            if (event.type() == type) {
                consumer.onEvent((T) event);
            }
        });
    }
}