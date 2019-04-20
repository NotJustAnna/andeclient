package pw.aru.lib.andeclient.entities.client;

import pw.aru.lib.andeclient.events.AndeClientEvent;
import pw.aru.lib.andeclient.events.EventType;
import pw.aru.lib.eventpipes.api.EventConsumer;
import pw.aru.lib.eventpipes.api.EventSubscription;

/**
 * This interface is the part of the AndeClient responsible of managing the events.
 */
public interface EventManager {
    /**
     * Add an event consumer for all events with the given handler callback.
     *
     * @param consumer The consumer for events.
     * @return The Event subscription, so you can close it later.
     */
    EventSubscription<AndeClientEvent> on(EventConsumer<AndeClientEvent> consumer);

    /**
     * Add a consumer for the specified event type with the given handler callback.
     *
     * @param type     The type of event to listen on.
     * @param consumer The consumer for events.
     * @param <T>      The object type of event being listened on.
     * @return The Event subscription, so you can close it later.
     */
    @SuppressWarnings("unchecked")
    default <T extends AndeClientEvent> EventSubscription<AndeClientEvent> on(EventType<T> type, EventConsumer<T> consumer) {
        return on(event -> {
            if (event.type() == type) {
                consumer.onEvent((T) event);
            }
        });
    }
}
