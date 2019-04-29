package pw.aru.libs.andeclient.internal;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.aru.lib.eventpipes.api.EventConsumer;
import pw.aru.lib.eventpipes.api.EventSubscription;
import pw.aru.libs.andeclient.entities.AndeClient;
import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.entities.AudioLoadResult;
import pw.aru.libs.andeclient.entities.EntityState;
import pw.aru.libs.andeclient.entities.configurator.AndesiteNodeConfigurator;
import pw.aru.libs.andeclient.events.AndeClientEvent;
import pw.aru.libs.andeclient.events.AndesiteNodeEvent;
import pw.aru.libs.andeclient.events.node.internal.PostedNewNodeEvent;
import pw.aru.libs.andeclient.events.node.internal.PostedNodeConnectedEvent;
import pw.aru.libs.andeclient.events.node.internal.PostedNodeRemovedEvent;
import pw.aru.libs.andeclient.events.player.internal.PostedPlayerRemovedEvent;
import pw.aru.libs.andeclient.events.player.internal.PostedWebSocketClosedEvent;
import pw.aru.libs.andeclient.events.track.internal.PostedTrackEndEvent;
import pw.aru.libs.andeclient.events.track.internal.PostedTrackExceptionEvent;
import pw.aru.libs.andeclient.events.track.internal.PostedTrackStartEvent;
import pw.aru.libs.andeclient.events.track.internal.PostedTrackStuckEvent;
import pw.aru.libs.andeclient.exceptions.RemoteTrackException;
import pw.aru.libs.andeclient.util.AndesiteUtil;
import pw.aru.libs.andeclient.util.AudioTrackUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public class AndesiteNodeImpl implements AndesiteNode, WebSocket.Listener {
    private static final Logger logger = LoggerFactory.getLogger(AndesiteNodeImpl.class);

    // Access to shared
    private final AndeClientImpl client;
    final Map<Long, AndePlayerImpl> children = new ConcurrentHashMap<>();

    // access to websocket
    private WebSocket websocket;


    // Dumb info to store
    private final String host;
    private final int port;
    private final String password;
    private final String relativePath;
    //internal stuff
    private final Queue<JSONObject> outcomingQueue = new LinkedBlockingQueue<>();
    //private String connectionId;
    private Info info;
    private final Queue<CompletableFuture<Stats>> awaitingStats = new LinkedBlockingQueue<>();
    private final StringBuilder wsBuffer = new StringBuilder();
    private EntityState state = EntityState.CONFIGURING;
    private ByteBuffer pingBuffer = ByteBuffer.allocate(4);
    private ScheduledFuture<?> scheduledPing;

    public AndesiteNodeImpl(AndesiteNodeConfigurator configurator) {
        this.client = (AndeClientImpl) configurator.client();
        this.host = configurator.host();
        this.port = configurator.port();
        this.password = configurator.password();
        this.relativePath = configurator.relativePath();

        this.client.nodes.add(this);
        this.client.events.publish(PostedNewNodeEvent.of(this));
        initWS();
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

    @Override
    public void destroy() {
        if (state == EntityState.DESTROYED) {
            return;
        }

        if (websocket == null) {
            throw new IllegalStateException("websocket is null, it is either already closed or trying to connect to the node.");
        }

        logger.trace("received destroy call, destroying websocket and cleaning up...");
        websocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client shutting down").thenRun(this::exitCleanup);
    }

    @Nonnull
    @Override
    public CompletionStage<Stats> stats() {
        if (state == EntityState.DESTROYED) {
            throw new IllegalStateException("Destroyed AndesiteNode.");
        }

        var stats = new CompletableFuture<Stats>();
        awaitingStats.add(stats);
        handleOutcoming(new JSONObject().put("op", "get-stats"));
        return stats;
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
            .thenApply(it -> AndesiteUtil.audioLoadResult(new JSONObject(it.body())));
    }

    @Override
    public EventSubscription<AndeClientEvent> on(EventConsumer<AndesiteNodeEvent> consumer) {
        return client.on(event -> {
            if (event instanceof AndesiteNodeEvent && ((AndesiteNodeEvent) event).node() == this) {
                consumer.onEvent((AndesiteNodeEvent) event);
            }
        });
    }

    void handleOutcoming(JSONObject json) {
        if (state == EntityState.DESTROYED) {
            return;
        }
        if (websocket == null) {
            outcomingQueue.offer(json);
            logger.trace("queued outcoming json to send after websocket init | json is {}", json);
            return;
        }
        logger.trace("sending outcoming json to andesite | json is {}", json);
        websocket.sendText(json.toString(), true);
    }

    private void handleIncoming(JSONObject json) {
        logger.trace("received incoming json from andesite | json is {}", json);
        try {
            switch (json.getString("op")) {
                case "connection-id": {
                    logger.trace("received connection-id from andesite, ignoring value");
                    //logger.trace("received connection-id from andesite, caching value");
                    //this.connectionId = json.getString("id");
                    return;
                }
                case "metadata": {
                    logger.trace("received metadata from andesite, updating info");
                    this.info = AndesiteUtil.nodeInfo(json.getJSONObject("data"));
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
                    player.update(json.getJSONObject("state"));
                    return;
                }
                case "pong": {
                    logger.trace("received pong from andesite");
                    return;
                }
                case "stats": {
                    logger.trace("received stats from andesite, publishing to futures");

                    var stats = AndesiteUtil.nodeStats(json.getJSONObject("stats"));
                    while (!awaitingStats.isEmpty()) {
                        var future = awaitingStats.poll();
                        if (future == null) break;
                        future.complete(stats);
                    }
                    return;
                }
                default: {
                    logger.warn("received unknown op | raw json is {}", json);
                }
            }
        } catch (Exception e) {
            logger.error("errored while handling json " + json, e);
        }
    }

    private void initWS() {
        var builder = client.httpClient.newWebSocketBuilder()
            .header("User-Id", String.valueOf(client.userId()));

        if (password != null) {
            builder.header("Authorization", password);
        }

        final var uri = URI.create(String.format("ws://%s:%d/%s", host, port, relativePath != null ? relativePath + "/websocket" : "websocket"));

        builder.buildAsync(uri, this);
    }

    @Override
    public void onOpen(WebSocket ws) {
        this.websocket = ws;
        logger.trace("websocket ws://{}:{}/{} opened", host, port, relativePath != null ? relativePath + "/websocket" : "websocket");

        client.events.publish(PostedNodeConnectedEvent.of(this));

        ws.request(1);

        scheduledPing = client.executor.scheduleAtFixedRate(this::doPing, 10, 10, TimeUnit.SECONDS);
        state = EntityState.AVAILABLE;

        if (!outcomingQueue.isEmpty()) {
            logger.trace("sending all cached outcoming json after websocket opened");
            while (!outcomingQueue.isEmpty()) {
                handleOutcoming(outcomingQueue.poll());
            }
            logger.trace("cached json sent");
        }
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        logger.trace("received close from andesite, cleaning up");
        exitCleanup();
        return null;
    }

    private void doPing() {
        pingBuffer.asCharBuffer().position(0).append("poke").flip();
        websocket.sendPing(pingBuffer);
    }

    @Override
    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
        wsBuffer.append(data);

        if (last) {
            try {
                var json = new JSONObject(wsBuffer.toString());
                handleIncoming(json);
            } catch (Exception e) {
                logger.error("received payload is not json | raw is {}", wsBuffer.toString(), e);
            } finally {
                wsBuffer.setLength(0);
            }
        }

        ws.request(1);
        return CompletableFuture.completedStage(data);
    }

    private void exitCleanup() {
        this.state = EntityState.DESTROYED;
        this.websocket = null;
        this.pingBuffer = null;
        this.awaitingStats.clear();
        this.wsBuffer.setLength(0);
        scheduledPing.cancel(true);
        client.nodes.remove(this);
        client.events.publish(PostedNodeRemovedEvent.of(this));

        client.players.values().removeAll(children.values());
        for (AndePlayerImpl player : children.values()) {
            player.state = EntityState.DESTROYED;
            client.events.publish(PostedPlayerRemovedEvent.of(player));
        }
        children.clear();
    }

    @Nullable
    private AndePlayerImpl playerFromEvent(@Nonnull final JSONObject json) {
        return client.players.get(Long.parseLong(json.getString("guildId")));
    }
}
