package pw.aru.libs.andeclient.internal;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONObject;
import pw.aru.libs.andeclient.entities.AndeClient;
import pw.aru.libs.andeclient.entities.AndePlayer;
import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.entities.EntityState;
import pw.aru.libs.andeclient.entities.configurator.AndePlayerConfigurator;
import pw.aru.libs.andeclient.entities.player.PlayerControls;
import pw.aru.libs.andeclient.entities.player.PlayerFilter;
import pw.aru.libs.andeclient.events.AndeClientEvent;
import pw.aru.libs.andeclient.events.AndePlayerEvent;
import pw.aru.libs.andeclient.events.player.internal.PostedNewPlayerEvent;
import pw.aru.libs.andeclient.events.player.internal.PostedPlayerRemovedEvent;
import pw.aru.libs.andeclient.events.player.internal.PostedPlayerUpdateEvent;
import pw.aru.libs.andeclient.events.player.update.internal.PostedPlayerFilterUpdateEvent;
import pw.aru.libs.andeclient.events.player.update.internal.PostedPlayerPauseUpdateEvent;
import pw.aru.libs.andeclient.util.AndesiteUtil;
import pw.aru.libs.eventpipes.api.EventConsumer;
import pw.aru.libs.eventpipes.api.EventSubscription;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class AndePlayerImpl implements AndePlayer {

    private final AndeClientImpl client;
    final AndesiteNodeImpl node;
    private final long guildId;

    AudioTrack playingTrack;
    private PlayerControlsImpl playerControls;
    private long time;
    private long position;
    private int volume;
    private boolean isPaused;
    private Collection<? extends PlayerFilter> filters = Set.of();
    EntityState state;

    public AndePlayerImpl(AndePlayerConfigurator configurator) {
        this.state = EntityState.CONFIGURING;
        this.client = (AndeClientImpl) configurator.client();
        this.node = (AndesiteNodeImpl) configurator.andesiteNode();
        this.guildId = configurator.guildId();

        if (this.client.players.containsKey(guildId)) {
            this.state = EntityState.DESTROYED;
            throw new IllegalStateException("There's already a player for that guild!");
        }

        if (this.node.state() == EntityState.DESTROYED) {
            this.state = EntityState.DESTROYED;
            throw new IllegalStateException("The AndesiteNode provided is already destroyed. please create a new AndePlayer with a valid AndesiteNode.");
        }

        this.node.children.put(guildId, this);
        this.client.players.put(guildId, this);
        this.client.events.publish(PostedNewPlayerEvent.of(this));
        this.state = EntityState.AVAILABLE;
    }

    @Nonnull
    @Override
    public AndesiteNode connectedNode() {
        return node;
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
    public PlayerControls controls() {
        if (state == EntityState.DESTROYED) {
            throw new IllegalStateException("Destroyed AndePlayer, please create a new one with AndeClient#newPlayer.");
        }
        if (playerControls == null) {
            playerControls = new PlayerControlsImpl(this);
        }
        return playerControls;
    }

    @Override
    public long guildId() {
        return guildId;
    }

    @Override
    public long serverTime() {
        return time;
    }

    @Nullable
    @Override
    public AudioTrack playingTrack() {
        return playingTrack;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public int volume() {
        return volume;
    }

    @Override
    public boolean paused() {
        return isPaused;
    }

    @Override
    public Collection<? extends PlayerFilter> filters() {
        return filters;
    }

    @Override
    public void handleVoiceServerUpdate(String sessionId, String voiceToken, String endpoint) {
        if (state == EntityState.DESTROYED) {
            throw new IllegalStateException("Destroyed AndePlayer, please create a new one with AndeClient#newPlayer.");
        }

        node.handleOutgoing(
            new JSONObject()
                .put("op", "voice-server-update")
                .put("guildId", Long.toString(guildId))
                .put("sessionId", sessionId)
                .put("event", new JSONObject()
                    .put("endpoint", endpoint)
                    .put("token", voiceToken)
                )
        );
    }

    @Override
    public void destroy() {
        if (state == EntityState.DESTROYED) {
            return;
        }

        node.handleOutgoing(
            new JSONObject()
                .put("op", "destroy")
                .put("guildId", Long.toString(guildId))
        );

        state = EntityState.DESTROYED;
        playerControls = null;
        node.children.remove(guildId);
        client.players.remove(guildId);
        client.events.publish(PostedPlayerRemovedEvent.of(this));
    }

    void update(JSONObject json) {
        if (this.state == EntityState.DESTROYED) {
            return;
        }

        final var lastTime = time;
        final var lastPosition = position;
        final var lastVolume = volume;
        final var wasPaused = isPaused;
        final var lastFilters = filters;

        final var newTime = Long.parseLong(json.getString("time"));
        final long newPosition = json.optInt("position", -1);
        final var newVolume = json.getInt("volume");
        final var newPaused = json.getBoolean("paused");
        final Collection<? extends PlayerFilter> newFilters = AndesiteUtil.playerFilters(json.getJSONObject("filters"));

        this.time = newTime;
        this.position = newPosition;
        this.volume = newVolume;
        this.isPaused = newPaused;
        this.filters = newFilters;

        this.client.events.publish(
            PostedPlayerUpdateEvent.builder()
                .player(this)
                .timestamp(newTime)
                .position(newPosition)
                .volume(newVolume)
                .filters(newFilters)
                .oldTimestamp(lastTime)
                .oldPosition(lastPosition)
                .build()
        );

        // handle pause update
        if (wasPaused != newPaused) {
            this.client.events.publish(
                PostedPlayerPauseUpdateEvent.of(this, newPaused)
            );
        }

        // handle volume update
        if (lastVolume != newVolume) {
            this.client.events.publish(
                PostedPlayerPauseUpdateEvent.of(this, newPaused)
            );
        }

        if (!Objects.equals(lastFilters, newFilters)) {
            this.client.events.publish(
                PostedPlayerFilterUpdateEvent.builder()
                    .player(this)
                    .filters(newFilters)
                    .oldFilters(lastFilters)
                    .build()
            );
        }
    }

    @Override
    public EventSubscription<AndeClientEvent> on(EventConsumer<AndePlayerEvent> consumer) {
        return client.on(event -> {
            if (state != EntityState.DESTROYED && event instanceof AndePlayerEvent && ((AndePlayerEvent) event).player() == this) {
                consumer.onEvent((AndePlayerEvent) event);
            }
        });
    }

    @Override
    public String toString() {
        return "AndePlayer(guildId=" + guildId + ", node=" + node + ")";
    }
}
