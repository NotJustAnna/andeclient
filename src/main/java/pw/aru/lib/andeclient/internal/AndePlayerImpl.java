package pw.aru.lib.andeclient.internal;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.aru.lib.andeclient.entities.AndeClient;
import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.entities.AndesiteNode;
import pw.aru.lib.andeclient.entities.configurator.AndePlayerConfigurator;
import pw.aru.lib.andeclient.entities.player.PlayerControls;
import pw.aru.lib.andeclient.entities.player.PlayerFilter;
import pw.aru.lib.andeclient.events.AndeClientEvent;
import pw.aru.lib.andeclient.events.AndePlayerEvent;
import pw.aru.lib.andeclient.events.player.internal.*;
import pw.aru.lib.eventpipes.api.EventConsumer;
import pw.aru.lib.eventpipes.api.EventSubscription;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AndePlayerImpl implements AndePlayer {
    private static final Logger logger = LoggerFactory.getLogger(AndePlayerImpl.class);

    private final AndeClientImpl client;
    final AndesiteNodeImpl node;
    private final long guildId;

    AudioTrack playingTrack;
    private PlayerControlsImpl playerControls;
    private long lastTime;
    private long lastPosition;
    private int lastVolume;
    private boolean isPaused;
    private List<PlayerFilter> lastFilters;

    public AndePlayerImpl(AndePlayerConfigurator configurator) {
        this.client = (AndeClientImpl) configurator.client();
        this.node = (AndesiteNodeImpl) configurator.andesiteNode();
        this.guildId = configurator.guildId();

        if (this.client.players.containsKey(guildId)) {
            throw new IllegalStateException("there's already a player for that guild!");
        }

        this.node.children.put(guildId, this);
        this.client.players.put(guildId, this);
        this.client.events.publish(PostedNewPlayerEvent.of(this));
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
    public PlayerControls controls() {
        if (playerControls == null) playerControls = new PlayerControlsImpl(this);
        return playerControls;
    }

    @Override
    public long guildId() {
        return guildId;
    }

    @Override
    public long serverTime() {
        return lastTime;
    }

    @Nullable
    @Override
    public AudioTrack playingTrack() {
        return playingTrack;
    }

    @Override
    public long position() {
        return lastPosition;
    }

    @Override
    public int volume() {
        return lastVolume;
    }

    @Override
    public boolean paused() {
        return isPaused;
    }

    @Override
    public void handleVoiceServerUpdate(String sessionId, String voiceToken, String endpoint) {
        node.handleOutcoming(
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
        node.handleOutcoming(
            new JSONObject()
                .put("op", "destroy")
                .put("guildId", Long.toString(guildId))
        );

        node.children.remove(guildId);
        client.players.remove(guildId);
        client.events.publish(PostedPlayerRemovedEvent.of(this));
    }

    public void update(JSONObject state) {
        final var wasPaused = this.isPaused;

        this.lastTime = Long.parseLong(state.getString("time"));
        this.lastPosition = state.optInt("position", -1);
        this.isPaused = state.getBoolean("paused");
        this.lastVolume = state.getInt("volume");

        var filters = new ArrayList<PlayerFilter>();

        final var jsonFilters = state.getJSONObject("filters");
        for (var filter : jsonFilters.keySet()) {
            final var jsonFilter = jsonFilters.getJSONObject(filter);
            if (jsonFilter.getBoolean("enabled")) {
                switch (filter) {
                    case "equalizer": {
                        final var equalizer = PlayerFilter.equalizer();
                        final var bands = jsonFilter.getJSONArray("bands");
                        for (int band = 0; band < bands.length(); band++) {
                            final var gain = bands.getFloat(band);
                            if (gain != 0f) equalizer.withBand(band, gain);
                        }
                        filters.add(equalizer);
                        break;
                    }
                    case "karaoke": {
                        filters.add(
                            PlayerFilter.karaoke()
                                .level(jsonFilter.getFloat("level"))
                                .monoLevel(jsonFilter.getFloat("monoLevel"))
                                .filterBand(jsonFilter.getFloat("filterBand"))
                                .filterWidth(jsonFilter.getFloat("filterWidth"))
                                .create()
                        );
                        break;
                    }
                    case "timescale": {
                        filters.add(
                            PlayerFilter.timescale()
                                .speed(jsonFilter.getFloat("speed"))
                                .pitch(jsonFilter.getFloat("pitch"))
                                .rate(jsonFilter.getFloat("rate"))
                                .create()
                        );
                        break;
                    }
                    case "tremolo": {
                        filters.add(
                            PlayerFilter.tremolo()
                                .frequency(jsonFilter.getFloat("frequency"))
                                .depth(jsonFilter.getFloat("depth"))
                                .create()
                        );
                        break;
                    }
                    case "vibrato": {
                        filters.add(
                            PlayerFilter.vibrato()
                                .frequency(jsonFilter.getFloat("frequency"))
                                .depth(jsonFilter.getFloat("depth"))
                                .create()
                        );
                        break;
                    }
                    case "volume": {
                        filters.add(PlayerFilter.volume(jsonFilter.getFloat("volume")));
                        break;
                    }
                    default: {
                        logger.warn("couldn't parse unknown filter {} | raw json is {}", filter, jsonFilter);
                        break;
                    }
                }
            }
        }

        this.lastFilters = List.copyOf(filters);

        this.client.events.publish(
            PostedPlayerUpdateEvent.builder()
                .player(this)
                .timestamp(lastTime)
                .position(lastPosition)
                .volume(lastVolume)
                .filters(lastFilters)
                .build()
        );

        if (wasPaused && !isPaused) {
            this.client.events.publish(PostedPlayerResumeEvent.of(this));
        } else if (!wasPaused && isPaused) {
            this.client.events.publish(PostedPlayerPauseEvent.of(this));
        }
    }

    @Override
    public EventSubscription<AndeClientEvent> on(EventConsumer<AndePlayerEvent> consumer) {
        return client.on(event -> {
            if (event instanceof AndePlayerEvent && ((AndePlayerEvent) event).player() == this) {
                consumer.onEvent((AndePlayerEvent) event);
            }
        });
    }
}
