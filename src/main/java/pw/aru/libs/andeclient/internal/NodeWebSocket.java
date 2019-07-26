package pw.aru.libs.andeclient.internal;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.*;

class NodeWebSocket implements WebSocket.Listener, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(NodeWebSocket.class);
    private final AndesiteNodeImpl node;
    private final URI uri;
    private CompletableFuture<WebSocket> websocket;
    private final StringBuilder wsBuffer = new StringBuilder();
    // pinging stuff
    private final int pingTimeout;
    private final ByteBuffer systemPingBuffer = ByteBuffer.allocate(8);
    int lastSystemPing = -1;
    int lastPayloadPing = -1;
    private boolean closed = false;
    private ScheduledFuture<?> systemPingTask;
    private CompletableFuture<Void> systemPingFuture;
    private ScheduledFuture<?> payloadPingTask;
    private CompletableFuture<Void> payloadPingFuture;

    NodeWebSocket(AndesiteNodeImpl node, HttpClient client, URI uri,
                  String userId, @Nullable String authentication, @Nullable String connectionId,
                  int timeout) {
        this.node = node;
        this.uri = uri;
        this.pingTimeout = timeout;
        this.websocket = new CompletableFuture<>();

        var builder = client.newWebSocketBuilder().header("User-Id", userId);
        if (authentication != null) {
            builder.header("Authorization", authentication);
        }
        if (connectionId != null) {
            builder.header("Andesite-Connection-Id", connectionId);
        }
        builder.buildAsync(uri, this);
    }

    @Override
    public void onOpen(WebSocket ws) {
        websocket.complete(ws);
        ws.request(1);

        try (MDCCloseable ignored = MDC.putCloseable("websocket_url", uri.toString())) {
            logger.info("Connected!");
        }

        node.handleOpen();

        systemPingTask = node.client.executor.scheduleWithFixedDelay(this::doSystemPing, 10, 10, TimeUnit.SECONDS);
        payloadPingTask = node.client.executor.scheduleWithFixedDelay(this::doPayloadPing, 10, 10, TimeUnit.SECONDS);
    }

    private void doSystemPing() {
        try {
            WebSocket ws = websocket.get(pingTimeout, TimeUnit.MILLISECONDS);
            systemPingBuffer.asLongBuffer().position(0).put(System.currentTimeMillis()).flip();
            systemPingFuture = new CompletableFuture<>();
            ws.sendPing(systemPingBuffer);

            systemPingFuture.get(pingTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            systemPingTask.cancel(false);
            payloadPingTask.cancel(false);
            node.handleTimeout();
        } catch (InterruptedException | ExecutionException e) {
            try (MDCCloseable ignored = MDC.putCloseable("websocket_url", uri.toString())) {
                logger.error("Error while pinging websocket.", e);
            }
        }
    }

    private void doPayloadPing() {
        try {
            WebSocket ws = websocket.get(pingTimeout, TimeUnit.MILLISECONDS);
            payloadPingFuture = new CompletableFuture<>();
            ws.sendText(
                new JSONObject()
                    .put("op", "ping")
                    .put("__nodewebsocket_payloadping", true)
                    .toString(),
                true
            );

            payloadPingFuture.get(pingTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            systemPingTask.cancel(false);
            payloadPingTask.cancel(false);
            node.handleTimeout();
        } catch (InterruptedException | ExecutionException e) {
            try (MDCCloseable ignored = MDC.putCloseable("websocket_url", uri.toString())) {
                logger.error("Error while pinging node.", e);
            }
        }
    }

    @Override
    public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
        if (!closed) {
            logger.error("Websocket closed unexplicably with code {} and reason '{}'.", statusCode, reason);
            closed = true;
            node.handleClose();
        }
        return null;
    }

    @Override
    public CompletionStage<?> onPong(WebSocket ws, ByteBuffer message) {
        systemPingFuture.complete(null);
        ws.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
        wsBuffer.append(data);

        if (last) {
            try {
                var json = new JSONObject(wsBuffer.toString());

                if (Objects.equals(json.optString("op", null), "pong") && json.optBoolean("__nodewebsocket_payloadping", false)) {
                    payloadPingFuture.complete(null);
                } else {
                    node.handleIncoming(json);
                }
            } catch (Exception e) {
                try (MDCCloseable ignored = MDC.putCloseable("websocket_url", uri.toString())) {
                    logger.error("Received payload that it's not valid json.", e);
                    logger.trace("Websocket Buffer: {}", wsBuffer.toString());
                }
            } finally {
                wsBuffer.setLength(0);
            }
        }

        ws.request(1);
        return null;
    }

    @Override
    public void onError(WebSocket ws, Throwable t) {
        try (MDCCloseable ignored = MDC.putCloseable("websocket_url", uri.toString())) {
            logger.error("Websocket errored", t);
        }
        node.handleError();
    }

    void send(JSONObject json) {
        websocket = websocket.thenComposeAsync(ws -> ws.sendText(json.toString(), true));
    }

    @Override
    public void close() {
        websocket.thenComposeAsync(ws -> ws.sendClose(1000, "Requested by client."));
        closed = true;
        closeResources();
    }

    void destroy() {
        closed = true;
        websocket.thenAcceptAsync(WebSocket::abort);
        closeResources();
    }

    private void closeResources() {
        if (systemPingTask != null) {
            systemPingTask.cancel(true);
        }
        if (payloadPingTask != null) {
            payloadPingTask.cancel(true);
        }
    }
}
