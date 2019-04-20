package pw.aru.lib.andeclient.internal;

import com.sedmelluq.discord.lavaplayer.tools.OrderedExecutor;
import pw.aru.lib.andeclient.entities.AndeClient;
import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.entities.AndesiteNode;
import pw.aru.lib.andeclient.entities.LoadBalancer;
import pw.aru.lib.andeclient.entities.configurator.AndePlayerConfigurator;
import pw.aru.lib.andeclient.entities.configurator.AndesiteNodeConfigurator;
import pw.aru.lib.andeclient.entities.configurator.internal.ActualAndePlayerConfigurator;
import pw.aru.lib.andeclient.entities.configurator.internal.ActualAndesiteNodeConfigurator;
import pw.aru.lib.andeclient.events.AndeClientEvent;
import pw.aru.lib.eventpipes.EventPipes;
import pw.aru.lib.eventpipes.api.EventConsumer;
import pw.aru.lib.eventpipes.api.EventPipe;
import pw.aru.lib.eventpipes.api.EventSubscription;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.http.HttpClient;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AndeClientImpl implements AndeClient {
    private final long userId;
    private final LoadBalancer loadBalancer;
    public ScheduledExecutorService pingRunner = Executors.newSingleThreadScheduledExecutor();
    public OrderedExecutor nodeEventExecutor = new OrderedExecutor(Executors.newCachedThreadPool());
    public EventPipe<AndeClientEvent> events = EventPipes.newAsyncPipe();
    public HttpClient http = HttpClient.newHttpClient();
    public List<AndesiteNodeImpl> nodes = new CopyOnWriteArrayList<>();
    public List<AndePlayerImpl> players = new CopyOnWriteArrayList<>();

    public AndeClientImpl(long userId, LoadBalancer loadBalancer) {
        this.userId = userId;
        this.loadBalancer = loadBalancer;
    }

    //region class AndeClientImpl implements NodeManager

    @Nonnull
    @Override
    public AndesiteNodeConfigurator newNode() {
        return new ActualAndesiteNodeConfigurator().client(this);
    }

    @Nonnull
    @Override
    public List<AndesiteNode> nodes() {
        return Collections.unmodifiableList(nodes);
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

    @Override
    public void removeNode(@Nonnull AndesiteNode node) {

    }

    //endregion

    //region class AndeClientImpl implements PlayerManager

    @Nonnull
    @Override
    public AndePlayerConfigurator newPlayer() {
        return new ActualAndePlayerConfigurator().client(this);
    }

    @Nonnull
    @Override
    public List<AndePlayer> players() {
        return Collections.unmodifiableList(players);
    }

    @Nullable
    @Override
    public AndePlayer player(long guildId) {
        return null;
    }

    @Nonnull
    @Override
    public AndePlayer removePlayer(long guildId, boolean shouldDestroy) {
        return null;
    }

    //endregion

    //region class AndeClientImpl implements EventManager

    @Override
    public void handleVoiceStateUpdate(String sessionId, String voiceToken, String endpoint) {

    }

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
        events.close();
        //TODO
    }

    //endregion
}
