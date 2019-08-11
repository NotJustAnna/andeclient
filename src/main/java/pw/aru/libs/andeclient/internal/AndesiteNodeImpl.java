package pw.aru.libs.andeclient.internal;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.aru.libs.andeclient.entities.AndeClient;
import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.entities.AudioLoadResult;
import pw.aru.libs.andeclient.entities.EntityState;
import pw.aru.libs.andeclient.entities.configurator.AndesiteNodeConfigurator;
import pw.aru.libs.andeclient.events.AndeClientEvent;
import pw.aru.libs.andeclient.events.AndesiteNodeEvent;
import pw.aru.libs.andeclient.events.EventType;
import pw.aru.libs.andeclient.events.node.NodeDisconnectedEvent.Reason;
import pw.aru.libs.andeclient.events.node.internal.PostedNewNodeEvent;
import pw.aru.libs.andeclient.events.node.internal.PostedNodeConnectedEvent;
import pw.aru.libs.andeclient.events.node.internal.PostedNodeDisconnectedEvent;
import pw.aru.libs.andeclient.events.node.internal.PostedNodeStatsEvent;
import pw.aru.libs.andeclient.events.player.internal.PostedWebSocketClosedEvent;
import pw.aru.libs.andeclient.events.track.internal.PostedTrackEndEvent;
import pw.aru.libs.andeclient.events.track.internal.PostedTrackExceptionEvent;
import pw.aru.libs.andeclient.events.track.internal.PostedTrackStartEvent;
import pw.aru.libs.andeclient.events.track.internal.PostedTrackStuckEvent;
import pw.aru.libs.andeclient.exceptions.RemoteTrackException;
import pw.aru.libs.andeclient.util.AndesiteUtil;
import pw.aru.libs.andeclient.util.AudioTrackUtil;
import pw.aru.libs.eventpipes.EventPipes;
import pw.aru.libs.eventpipes.api.EventConsumer;
import pw.aru.libs.eventpipes.api.EventPipe;
import pw.aru.libs.eventpipes.api.EventSubscription;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;

public class AndesiteNodeImpl implements AndesiteNode {
    private static final Logger logger = LoggerFactory.getLogger(AndesiteNodeImpl.class);

    // node objects
    final AndeClientImpl client;
    final EventPipe<JsonObject> pongRelay = EventPipes.newAsyncPipe();
    final Map<Long, AndePlayerImpl> children = new ConcurrentHashMap<>();
    // creation info
    private final String host;
    private final int timeout;
    private NodeWebSocket ws;
    private EntityState state = EntityState.CONFIGURING;
    private Info info;
    private Stats lastStats;
    private final int port;
    private final String password;
    private final String relativePath;
    private String connectionId;
    private ScheduledFuture<?> statsCacheTask;

    public AndesiteNodeImpl(AndesiteNodeConfigurator configurator) {
        this.client = (AndeClientImpl) configurator.client();
        this.host = configurator.host();
        this.port = configurator.port();
        this.password = configurator.password();
        this.relativePath = configurator.relativePath();
        this.timeout = configurator.timeout();

        this.client.nodes.add(this);
        this.client.events.publish(PostedNewNodeEvent.of(this));
        this.ws = new NodeWebSocket(this, client.httpClient, nodeUri(), Long.toString(client.userId()), password, null, timeout);
    }

    @Nonnull
    @Override
    public AndeClient client() {
        return client;
    }

    @Nonnull
    @Override
    public EntityState state() {
        return state;
    }

    @Nonnull
    @Override
    public String host() {
        return host;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public String relativePath() {
        return relativePath;
    }

    @Nonnull
    @Override
    public Info nodeInfo() {
        return info;
    }

    @Nonnull
    @Override
    public CompletionStage<Stats> stats() {
        var future = new CompletableFuture<Stats>();
        var subscription = on(EventType.NODE_STATS_EVENT, e -> future.complete(e.stats()));
        future.thenRun(subscription::close);
        return future;
    }

    @Nonnull
    @Override
    public CompletionStage<AudioLoadResult> loadTracksAsync(String identifier) {
        final var uri = URI.create(String.format("http://%s:%d/%s?identifier=%s",
            host, port, relativePath != null ? relativePath + "/loadtracks" : "loadtracks",
            URLEncoder.encode(identifier, StandardCharsets.UTF_8)
        ));
        final var builder = HttpRequest.newBuilder()
            .GET()
            .uri(uri);

        if (password != null) {
            builder.header("Authorization", password);
        }

        return client.httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
            .thenApply(it -> {
                try {
                    return AndesiteUtil.audioLoadResult(JsonParser.object().from(it.body()));
                } catch (JsonParserException e) {
                    throw new IllegalStateException(e);
                }
            });
    }

    @Override
    public EventSubscription<AndeClientEvent> on(EventConsumer<AndesiteNodeEvent> consumer) {
        return client.on(event -> {
            if (event instanceof AndesiteNodeEvent && ((AndesiteNodeEvent) event).node() == this) {
                consumer.onEvent((AndesiteNodeEvent) event);
            }
        });
    }

    void handleOpen() {
        state = EntityState.AVAILABLE;
        statsCacheTask = client.executor.scheduleAtFixedRate(this::cacheStats, 10, 10, TimeUnit.SECONDS);

        //setup reconnect
        handleOutgoing(
            JsonObject.builder()
                .value("op", "event-buffer")
                .value("timeout", timeout)
                .done()
        );

        client.events.publish(PostedNodeConnectedEvent.of(this));
    }

    void handleTimeout() {
        if (state == EntityState.DESTROYED) {
            return;
        }

        logger.warn("Connection to node timed out, reconnecting...");

        state = EntityState.CONFIGURING;
        client.events.publish(PostedNodeDisconnectedEvent.of(this, Reason.TIMED_OUT));
        reconnect();
    }

    void handleClose() {
        if (state == EntityState.DESTROYED) {
            return;
        }

        logger.warn("Connection to node closed by server, reconnecting...");

        state = EntityState.CONFIGURING;
        client.events.publish(PostedNodeDisconnectedEvent.of(this, Reason.CLOSED_BY_SERVER));
        reconnect();
    }

    void handleError() {
        if (state == EntityState.DESTROYED) {
            return;
        }

        logger.warn("Connection to node errored, reconnecting...");

        state = EntityState.CONFIGURING;
        client.events.publish(PostedNodeDisconnectedEvent.of(this, Reason.WEBSOCKET_ERROR));
        reconnect();
    }

    void handleIncoming(JsonObject json) {
        logger.trace("received incoming json from andesite | json is {}", json);
        try {
            switch (json.getString("op")) {
                case "connection-id": {
                    logger.trace("received connection-id from andesite, caching value");
                    this.connectionId = json.getString("id");
                    return;
                }
                case "metadata": {
                    logger.trace("received metadata from andesite, updating info");
                    this.info = AndesiteUtil.nodeInfo(json.getObject("data"));
                    return;
                }
                case "event": {
                    switch (json.getString("type")) {
                        case "TrackStartEvent": {
                            logger.trace("received event TrackStartEvent, publishing it");
                            final var player = playerFromEvent(json);
                            if (player == null) {
                                logger.trace("player not on AndeClient, dropping update");
                                return;
                            }

                            final var track = AudioTrackUtil.fromString(json.getString("track"));
                            player.playingTrack = track;

                            client.events.publish(
                                PostedTrackStartEvent.builder()
                                    .player(player)
                                    .track(track)
                                    .build()
                            );
                            return;
                        }
                        case "TrackEndEvent": {
                            logger.trace("received event TrackEndEvent, publishing it");
                            final var player = playerFromEvent(json);
                            if (player == null) {
                                logger.trace("player not on AndeClient, dropping update");
                                return;
                            }

                            final var track = AudioTrackUtil.fromString(json.getString("track"));
                            player.playingTrack = null;

                            client.events.publish(
                                PostedTrackEndEvent.builder()
                                    .player(player)
                                    .track(track)
                                    .reason(AudioTrackEndReason.valueOf(json.getString("reason")))
                                    .build()
                            );
                            return;
                        }
                        case "TrackExceptionEvent": {
                            logger.trace("received event TrackExceptionEvent, publishing it");
                            final var player = playerFromEvent(json);
                            if (player == null) {
                                logger.trace("player not on AndeClient, dropping update");
                                return;
                            }

                            final var track = AudioTrackUtil.fromString(json.getString("track"));

                            client.events.publish(
                                PostedTrackExceptionEvent.builder()
                                    .player(player)
                                    .track(track)
                                    .exception(new RemoteTrackException(client, player, this, track, json.getString("error")))
                                    .build()
                            );
                            return;
                        }
                        case "TrackStuckEvent": {
                            logger.trace("received event TrackStuckEvent, publishing it");
                            final var player = playerFromEvent(json);
                            if (player == null) {
                                logger.trace("player not on AndeClient, dropping update");
                                return;
                            }

                            final var track = AudioTrackUtil.fromString(json.getString("track"));

                            client.events.publish(
                                PostedTrackStuckEvent.builder()
                                    .player(player)
                                    .track(track)
                                    .thresholdMs(json.getInt("thresholdMs"))
                                    .build()
                            );
                            return;
                        }
                        case "WebSocketClosedEvent": {
                            logger.trace("received event WebSocketClosedEvent, publishing it");
                            final var player = playerFromEvent(json);
                            if (player == null) {
                                logger.trace("player not on AndeClient, dropping event");
                                return;
                            }
                            player.playingTrack = null;

                            client.events.publish(
                                PostedWebSocketClosedEvent.builder()
                                    .player(player)
                                    .reason(json.getString("reason"))
                                    .closeCode(json.getInt("code"))
                                    .byRemote(json.getBoolean("byRemote"))
                                    .build()
                            );
                            return;
                        }
                        default: {
                            logger.warn("received event of unknown type | raw json is {}", json);
                            return;
                        }
                    }
                }
                case "player-update": {
                    logger.trace("received player update, sending to player");
                    final var player = playerFromEvent(json);
                    if (player == null) {
                        logger.trace("player not on AndeClient, dropping update");
                        return;
                    }
                    player.update(json.getObject("state"));
                    return;
                }
                case "pong": {
                    logger.trace("received pong from andesite, publishing it to the relay");
                    json.remove("op");
                    pongRelay.publish(json);
                    return;
                }
                case "stats": {
                    logger.trace("received stats from andesite, publishing it");

                    var stats = AndesiteUtil.nodeStats(this, json.getObject("stats"));
                    client.events.publish(PostedNodeStatsEvent.of(stats));
                    lastStats = stats;
                    return;
                }
                default: {
                    logger.warn("Received unknown op | raw json is {}", json);
                }
            }
        } catch (Exception e) {
            logger.error("Errored while handling json " + json, e);
        }
    }

    void handleOutgoing(JsonObject json) {
        if (state == EntityState.DESTROYED) {
            return;
        }
        logger.trace("sending outgoing json to andesite | json is {}", json);
        ws.send(json);
    }

    @Nullable
    private AndePlayerImpl playerFromEvent(@Nonnull final JsonObject json) {
        return client.players.get(Long.parseLong(json.getString("guildId")));
    }

    @Override
    public void destroy() {
        if (state == EntityState.DESTROYED) {
            return;
        }

        if (ws == null) {
            throw new IllegalStateException("AndesiteNode's websocket is null, it is either already closed or trying to connect to the node.");
        }

        logger.trace("received destroy call, destroying websocket and cleaning up...");
        ws.close();
        exitCleanup();
    }

    private URI nodeUri() {
        return URI.create(String.format("ws://%s:%d/%s", host, port, relativePath != null ? relativePath + "/websocket" : "websocket"));
    }

    private void cacheStats() {
        handleOutgoing(
            JsonObject.builder()
                .value("op", "get-stats")
                .done()
        );
    }

    private void reconnect() {
        ws.destroy();
        ws = new NodeWebSocket(this, client.httpClient, nodeUri(), Long.toString(client.userId()), password, connectionId, timeout);
    }

    private void exitCleanup() {
        statsCacheTask.cancel(true);
    }

    @Override
    public String toString() {
        return "AndesiteNode(" + String.format("%s:%d", host, port) + ")";
    }

}
