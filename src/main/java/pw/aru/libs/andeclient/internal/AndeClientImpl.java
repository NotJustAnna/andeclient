package pw.aru.libs.andeclient.internal;

import pw.aru.lib.eventpipes.EventPipes;
import pw.aru.lib.eventpipes.api.EventConsumer;
import pw.aru.lib.eventpipes.api.EventPipe;
import pw.aru.lib.eventpipes.api.EventSubscription;
import pw.aru.libs.andeclient.entities.AndeClient;
import pw.aru.libs.andeclient.entities.AndePlayer;
import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.entities.LoadBalancer;
import pw.aru.libs.andeclient.entities.configurator.AndeClientConfigurator;
import pw.aru.libs.andeclient.entities.configurator.internal.ActualAndePlayerConfigurator;
import pw.aru.libs.andeclient.entities.configurator.internal.ActualAndesiteNodeConfigurator;
import pw.aru.libs.andeclient.events.AndeClientEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AndeClientImpl implements AndeClient {
    private final long userId;
    private final LoadBalancer loadBalancer;
    final ScheduledExecutorService executor;
    final HttpClient httpClient;
    final EventPipe<AndeClientEvent> events;
    final List<AndesiteNodeImpl> nodes = new CopyOnWriteArrayList<>();
    final Map<Long, AndePlayerImpl> players = new ConcurrentHashMap<>();

    public AndeClientImpl(AndeClientConfigurator configurator) {
        this.userId = configurator.userId();
        this.httpClient = configurator.httpClient();
        this.loadBalancer = configurator.loadBalancer();
        this.executor = configurator.executor();
        this.events = EventPipes.newAsyncPipe(this.executor);
    }

    //region class AndeClientImpl implements NodeManager

    @Nonnull
    @Override
    public ActualAndesiteNodeConfigurator newNode() {
        return new ActualAndesiteNodeConfigurator().client(this);
    }

    @Nonnull
    @Override
    public List<AndesiteNode> nodes() {
        return List.copyOf(nodes);
    }

    @Nonnull
    @Override
    public AndesiteNode bestNode() {
        if (nodes.isEmpty()) {
            throw new IllegalStateException("no nodes!");
        }
        if (nodes.size() == 1) {
            return nodes.get(0);
        }

        final var stats = nodes.stream()
            .map(AndesiteNode::stats)
            .map(CompletionStage::toCompletableFuture)
            .map(CompletableFuture::join)
            .collect(Collectors.toUnmodifiableList());

        int bestPenalty = Integer.MAX_VALUE;
        AndesiteNode bestNode = null;

        for (var stat : stats) {
            var penalty = loadBalancer.totalPenalty(stat);
            if (penalty < bestPenalty) {
                bestPenalty = penalty;
                bestNode = stat.node();
            }
        }

        if (bestNode == null) {
            throw new IllegalStateException("no nodes!");
        }

        return bestNode;
    }

    //endregion

    //region class AndeClientImpl implements PlayerManager

    @Nonnull
    @Override
    public ActualAndePlayerConfigurator newPlayer() {
        return new ActualAndePlayerConfigurator().client(this);
    }

    @Nonnull
    @Override
    public AndePlayer newPlayer(long guildId) {
        final var player = players.get(guildId);

        if (player != null) {
            return player;
        }

        return new ActualAndePlayerConfigurator()
            .client(this)
            .guildId(guildId)
            .andesiteNode(bestNode())
            .create();
    }

    @Nonnull
    @Override
    public List<AndePlayer> players() {
        return List.copyOf(players.values());
    }

    @Nullable
    @Override
    public AndePlayer player(long guildId) {
        return players.get(guildId);
    }

    //endregion

    //region class AndeClientImpl implements EventManager

    @Override
    public EventSubscription<AndeClientEvent> on(EventConsumer<AndeClientEvent> consumer) {
        return events.subscribe(consumer);
    }

    //endregion

    //region class AndeClientImpl implements AndeClient

    @Override
    public long userId() {
        return userId;
    }

    @Override
    public void shutdown() {
        nodes.forEach(AndesiteNodeImpl::destroy);
        events.close();
        executor.shutdown();
    }

    @Override
    public String toString() {
        return "AndeClient(userId=" + userId + ", nodes=" + nodes.size() + ", players=" + players.size() + ")";
    }

    //endregion
}
