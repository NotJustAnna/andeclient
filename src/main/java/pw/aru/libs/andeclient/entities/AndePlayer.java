package pw.aru.libs.andeclient.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pw.aru.lib.eventpipes.api.EventConsumer;
import pw.aru.lib.eventpipes.api.EventSubscription;
import pw.aru.libs.andeclient.entities.player.PlayerControls;
import pw.aru.libs.andeclient.entities.player.PlayerFilter;
import pw.aru.libs.andeclient.events.AndeClientEvent;
import pw.aru.libs.andeclient.events.AndePlayerEvent;
import pw.aru.libs.andeclient.events.EventType;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A player connected to an andesite node.
 */
@SuppressWarnings("unused")
public interface AndePlayer {
    /**
     * Returns the parent AndeClient of this player.
     *
     * @return an AndeClient instance.
     */
    @CheckReturnValue
    @Nonnull
    AndeClient client();

    /**
     * Returns the state of the AndePlayer.
     *
     * @return one of possible states.
     */
    @CheckReturnValue
    @Nonnull
    EntityState state();

    /**
     * Returns the andesite node this player is connected to.
     * @return an andesite node.
     */
    @CheckReturnValue
    @Nonnull
    AndesiteNode connectedNode();

    /**
     * Returns the controller fot this player.
     * @return the player controller.
     */
    @CheckReturnValue
    @Nonnull
    PlayerControls controls();

    /**
     * Returns the guild id of this player.
     * @return the guild id.
     */
    @CheckReturnValue
    @Nonnegative
    long guildId();

    /**
     * Returns the server last time.
     * @return last server time.
     */
    @CheckReturnValue
    @Nonnegative
    long serverTime();

    /**
     * Returns the current playing track, if any
     *
     * @return an audio track
     */
    @CheckReturnValue
    @Nullable
    AudioTrack playingTrack();

    /**
     * Returns the player current position.
     * @return the position.
     */
    @CheckReturnValue
    @Nonnegative
    long position();

    /**
     * Returns the player current volume.
     * @return the volume.
     */
    @CheckReturnValue
    @Nonnegative
    int volume();

    /**
     * Returns the player current pause state.
     * @return true if the player is paused. false otherwise.
     */
    @CheckReturnValue
    boolean paused();

    /**
     * Returns the current filters active, if any,
     *
     * @return collection of active player filters.
     */
    @CheckReturnValue
    Collection<? extends PlayerFilter> filters();

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