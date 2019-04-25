package pw.aru.lib.andeclient.entities;

import org.immutables.value.Value;
import org.json.JSONObject;
import pw.aru.lib.andeclient.annotations.SimpleData;
import pw.aru.lib.andeclient.events.AndeClientEvent;
import pw.aru.lib.andeclient.events.AndesiteNodeEvent;
import pw.aru.lib.andeclient.events.EventType;
import pw.aru.lib.eventpipes.api.EventConsumer;
import pw.aru.lib.eventpipes.api.EventSubscription;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletionStage;

@SuppressWarnings("unused")
public interface AndesiteNode {
    @Nonnull
    @CheckReturnValue
    AndeClient client();

    @CheckReturnValue
    boolean available();

    @Nonnull
    @CheckReturnValue
    Info nodeInfo();

    @Nonnull
    @CheckReturnValue
    CompletionStage<Stats> stats();

    @Nonnull
    @CheckReturnValue
    CompletionStage<AudioLoadResult> loadTracksAsync(String identifier);


    /**
     * Add an event consumer for all events of this player with the given handler callback.
     *
     * @param consumer The consumer for events.
     * @return The Event subscription, so you can close it later.
     */
    EventSubscription<AndeClientEvent> on(EventConsumer<AndesiteNodeEvent> consumer);

    /**
     * Add a consumer for the specified event type of this player with the given handler callback.
     *
     * @param type     The type of event to listen on.
     * @param consumer The consumer for events.
     * @param <T>      The object type of event being listened on.
     * @return The Event subscription, so you can close it later.
     */
    @SuppressWarnings("unchecked")
    default <T extends AndesiteNodeEvent> EventSubscription<AndeClientEvent> on(EventType<T> type, EventConsumer<T> consumer) {
        return on(event -> {
            if (event.type() == type) {
                consumer.onEvent((T) event);
            }
        });
    }

    void destroy();

    @Value.Immutable
    @SimpleData
    interface Info {
        String version();

        String versionMajor();

        String versionMinor();

        String versionRevision();

        long versionBuild();

        String versionCommit();

        String nodeRegion();

        String nodeId();

        List<String> enabledSources();

        List<String> loadedPlugins();
    }

    @Value.Immutable
    @SimpleData
    interface Stats {
        AndesiteNode node();

        JSONObject raw();

        int players();

        int playingPlayers();

        long uptime();

        double systemLoad();

        double andesiteLoad();

        long sentFrames();

        long nulledFrames();

        long deficitFrames();
    }
}
