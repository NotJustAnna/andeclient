package pw.aru.lib.andeclient.internal;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.aru.lib.andeclient.entities.AndeClient;
import pw.aru.lib.andeclient.entities.AndesiteNode;
import pw.aru.lib.andeclient.events.node.internal.PostedNewNodeEvent;
import pw.aru.lib.andeclient.events.node.internal.PostedNodeConnectedEvent;
import pw.aru.lib.andeclient.events.player.internal.PostedWebSocketClosedEvent;
import pw.aru.lib.andeclient.events.track.internal.PostedTrackEndEvent;
import pw.aru.lib.andeclient.events.track.internal.PostedTrackExceptionEvent;
import pw.aru.lib.andeclient.events.track.internal.PostedTrackStartEvent;
import pw.aru.lib.andeclient.events.track.internal.PostedTrackStuckEvent;
import pw.aru.lib.andeclient.exceptions.RemoteTrackException;
import pw.aru.lib.andeclient.util.AudioTrackUtil;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AndesiteNodeImpl implements AndesiteNode, WebSocket.Listener {
    private static final Logger logger = LoggerFactory.getLogger(AndesiteNodeImpl.class);

    // Access to shared
    private final AndeClientImpl client;

    // Dumb info to store
    private final String host;
    private final int port;
    private final String password;
    private final String relativePath;
    private final Queue<CompletableFuture<Stats>> awaitingStats = new LinkedBlockingQueue<>();
    private final StringBuilder buffer = new StringBuilder();
    // access to websocket
    private WebSocket websocket;
    // info we get from node
    private boolean available;
    private Info info;
    private String connectionId;
    private ByteBuffer pingBuffer = ByteBuffer.allocate(4);

    public AndesiteNodeImpl(AndeClient andeclient, String host, int port, String password, String relativePath) {
        this.client = (AndeClientImpl) andeclient;

        this.host = host;
        this.port = port;
        this.password = password;
        this.relativePath = relativePath;

        this.available = false;
        client.nodes.add(this);
        client.events.publish(PostedNewNodeEvent.of(this));
        initWS();
    }

    @Nonnull
    @Override
    public AndeClient client() {
        return client;
    }

    @Nonnull
    @Override
    public Info nodeInfo() {
        return info;
    }

    @Override
    public boolean available() {
        return available;
    }

    @Override
    public void closeConnection() {
        // TODO
    }

    @Override
    public CompletionStage<Stats> stats() {
        var stats = new CompletableFuture<Stats>();
        awaitingStats.add(stats);
        handleOutcoming(new JSONObject().put("op", "get-stats"));
        return stats;
    }

    private void handleOutcoming(JSONObject json) {
        websocket.sendText(json.toString(), true);
    }

    private void handleIncoming(JSONObject json) {
        switch (json.getString("op")) {
            case "connection-id": {
                logger.trace("received connection-id from andesite, caching value");
                this.connectionId = json.getString("id");
                break;
            }
            case "metadata": {
                logger.trace("received metadata from andesite, updating info");
                this.info = EntityBuilder.nodeInfo(json.getJSONObject("data"));
                break;
            }
            case "event": {
                switch (json.getString("type")) {
                    case "TrackStartEvent": {
                        logger.trace("received event TrackStartEvent, publishing it");
                        final var player = playerFromEvent(json);
                        final var track = AudioTrackUtil.fromString(json.getString("track"));
                        player.playingTrack = track;

                        client.events.publish(
                            PostedTrackStartEvent.builder()
                                .player(player)
                                .track(track)
                                .build()
                        );
                        break;
                    }
                    case "TrackEndEvent": {
                        logger.trace("received event TrackEndEvent, publishing it");
                        final var player = playerFromEvent(json);
                        final var track = AudioTrackUtil.fromString(json.getString("track"));
                        player.playingTrack = null;

                        client.events.publish(
                            PostedTrackEndEvent.builder()
                                .player(player)
                                .track(track)
                                .reason(AudioTrackEndReason.valueOf(json.getString("reason")))
                                .build()
                        );
                        break;
                    }
                    case "TrackExceptionEvent": {
                        logger.trace("received event TrackExceptionEvent, publishing it");
                        final var player = playerFromEvent(json);
                        final var track = AudioTrackUtil.fromString(json.getString("track"));

                        client.events.publish(
                            PostedTrackExceptionEvent.builder()
                                .player(player)
                                .track(track)
                                .exception(new RemoteTrackException(client, player, this, track, json.getString("error")))
                                .build()
                        );
                        break;
                    }
                    case "TrackStuckEvent": {
                        logger.trace("received event TrackStuckEvent, publishing it");
                        final var player = playerFromEvent(json);
                        final var track = AudioTrackUtil.fromString(json.getString("track"));

                        client.events.publish(
                            PostedTrackStuckEvent.builder()
                                .player(player)
                                .track(track)
                                .thresholdMs(json.getInt("thresholdMs"))
                                .build()
                        );
                        break;
                    }
                    case "WebSocketClosedEvent": {
                        logger.trace("received event WebSocketClosedEvent, publishing it");
                        final var player = playerFromEvent(json);
                        player.playingTrack = null;

                        client.events.publish(
                            PostedWebSocketClosedEvent.builder()
                                .player(player)
                                .reason(json.getString("reason"))
                                .closeCode(json.getInt("code"))
                                .byRemote(json.getBoolean("byRemote"))
                                .build()
                        );
                        break;
                    }
                    default: {
                        logger.warn("received event of unknown type | raw json is {}", json);
                        break;
                    }
                }
                break;
            }
            case "player-update": {
                //TODO
                break;
            }
            case "pong": {
                logger.trace("received pong from andesite");
                break;
            }
            case "stats": {
                logger.trace("received stats from andesite, publishing to futures");

                var stats = EntityBuilder.nodeStats(json.getJSONObject("stats"));
                while (!awaitingStats.isEmpty()) {
                    var future = awaitingStats.poll();
                    if (future == null) break;
                    future.complete(stats);
                }
                break;
            }
            default: {
                logger.warn("received unknown op | raw json is {}", json);
                break;
            }
        }
    }

    private void initWS() {
        var builder = client.http.newWebSocketBuilder();
        builder.header("User-Id", String.valueOf(client.userId()));

        if (password != null) {
            builder.header("Authorization", password);
        }

        builder.buildAsync(URI.create(String.format("ws://%s:%d%s", host, port, relativePath != null ? "/" + relativePath : "")), this);
    }

    @Override
    public void onOpen(WebSocket ws) {
        this.websocket = ws;
        this.available = true;

        client.events.publish(PostedNodeConnectedEvent.of(this));

        ws.request(1);

        client.pingRunner.scheduleAtFixedRate(this::doPing, 10, 10, TimeUnit.SECONDS);
    }

    private void doPing() {
        pingBuffer.asCharBuffer().position(0).append("poke").flip();
        websocket.sendPing(pingBuffer);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        buffer.append(data);

        if (last) {
            var json = new JSONObject(buffer.toString());
            buffer.setLength(0);
            handleIncoming(json);
        }
        return CompletableFuture.completedStage(data);
    }

    @Nonnull
    private AndePlayerImpl playerFromEvent(@Nonnull final JSONObject json) {
        final var guildId = json.getString("guildId");
        var player = client.players.stream()
            .filter(it -> it.guildId() == Long.parseUnsignedLong(guildId))
            .findFirst()
            .orElse(null);

        if (player == null) {
            logger.warn("unknown player for guild id: {}", guildId);
            throw new IllegalStateException("unknown player | guild id: " + guildId);
        }
        return player;
    }
}
