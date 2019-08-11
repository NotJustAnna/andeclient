package pw.aru.libs.andeclient.internal;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONObject;
import pw.aru.libs.andeclient.entities.AndePlayer;
import pw.aru.libs.andeclient.entities.EntityState;
import pw.aru.libs.andeclient.entities.player.PlayerControls;
import pw.aru.libs.andeclient.entities.player.PlayerFilter;
import pw.aru.libs.andeclient.util.AudioTrackUtil;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class PlayerControlsImpl implements PlayerControls {
    private final AndePlayerImpl player;

    PlayerControlsImpl(AndePlayerImpl player) {
        this.player = player;
    }

    @Nonnull
    @Override
    public AndePlayer player() {
        return player;
    }

    @Nonnull
    @Override
    public Play play() {
        return new PlayPayload();
    }

    @Nonnull
    @Override
    public Payload<Void> pause() {
        return new SimplePayload("pause", new JSONObject().put("pause", true));
    }

    @Nonnull
    @Override
    public Payload<Void> resume() {
        return new SimplePayload("pause", new JSONObject().put("pause", false));
    }

    @Nonnull
    @Override
    public Payload<Void> volume(int volume) {
        return new SimplePayload("volume", new JSONObject().put("volume", volume));
    }

    @Nonnull
    @Override
    public Mixer mixer() {
        return new MixerPayload();
    }

    @Nonnull
    @Override
    public Payload<Void> filters(PlayerFilter... filters) {
        return new FiltersPayload(filters);
    }

    @Nonnull
    @Override
    public Payload<Void> seek(long position) {
        return new SimplePayload("seek", new JSONObject().put("position", position));
    }

    @Nonnull
    @Override
    public Payload<Void> stop() {
        return new EmptyPayload("stop");
    }

    private abstract class AbstractPayload<T> implements Payload<T> {
        private final String op;

        AbstractPayload(String op) {
            this.op = op;
        }

        protected abstract JSONObject createPayload();

        protected JSONObject createPingPayload() {
            return new JSONObject();
        }

        @Nonnull
        @Override
        public PlayerControls execute() {
            submit().toCompletableFuture().join();
            return PlayerControlsImpl.this;
        }

        @Nonnull
        @Override
        public CompletionStage<T> submit() {
            if (player.state == EntityState.DESTROYED) {
                throw new IllegalStateException("Destroyed AndePlayer, please create a new one with AndeClient#newPlayer.");
            }

            player.node.handleOutgoing(
                createPayload()
                    .put("op", op)
                    .put("guildId", Long.toString(player.guildId()))
            );

            var randomUUID = UUID.randomUUID().toString();

            var stage = player.node.pongRelay.first(data -> Objects.equals(data.optString("__andeclient_controls_uuid", null), randomUUID))
                .thenApply(this::map);

            player.node.handleOutgoing(
                createPingPayload()
                    .put("op", "ping")
                    .put("__andeclient_controls_uuid", randomUUID)
            );

            return stage;
        }

        protected abstract T map(JSONObject data);
    }

    private abstract class VoidPayload extends AbstractPayload<Void> {
        VoidPayload(String op) {
            super(op);
        }

        @Override
        protected Void map(JSONObject data) {
            return null;
        }
    }

    private class EmptyPayload extends VoidPayload {
        EmptyPayload(String op) {
            super(op);
        }

        @Override
        protected JSONObject createPayload() {
            return new JSONObject();
        }
    }

    private class SimplePayload extends VoidPayload {
        private final JSONObject payload;

        SimplePayload(String op, JSONObject payload) {
            super(op);
            this.payload = payload;
        }

        @Override
        protected JSONObject createPayload() {
            return payload;
        }
    }

    private class PlayPayload extends VoidPayload implements Play {
        private String trackString;
        private Long start;
        private Long end;
        private boolean noReplace;
        private Boolean pause;
        private Integer volume;

        PlayPayload() {
            super("play");
        }

        @Nonnull
        @Override
        public Play track(@Nonnull String trackString) {
            this.trackString = trackString;
            return this;
        }

        @Nonnull
        @Override
        public Play track(@Nonnull AudioTrack track) {
            return track(AudioTrackUtil.fromTrack(track));
        }

        @Nonnull
        @Override
        public Play start(Long timestamp) {
            this.start = timestamp;
            return this;
        }

        @Nonnull
        @Override
        public Play end(Long timestamp) {
            this.end = timestamp;
            return this;
        }

        @Nonnull
        @Override
        public Play noReplace() {
            this.noReplace = true;
            return this;
        }

        @Nonnull
        @Override
        public Play replacing() {
            this.noReplace = false;
            return this;
        }

        @Nonnull
        @Override
        public Play pause(Boolean isPaused) {
            this.pause = isPaused;
            return this;
        }

        @Nonnull
        @Override
        public Play volume(Integer volume) {
            this.volume = volume;
            return this;
        }

        @Override
        protected JSONObject createPayload() {
            if (trackString == null) {
                throw new IllegalStateException("track must not be null!");
            }

            final var json = new JSONObject()
                .put("track", trackString)
                .put("noReplace", noReplace);

            if (start != null) json.put("start", start);
            if (end != null) json.put("end", end);
            if (pause != null) json.put("pause", pause);
            if (volume != null) json.put("volume", volume);

            return json;
        }
    }

    private class MixerPayload extends VoidPayload implements Mixer {
        MixerPayload() {
            super("mixer");
        }

        @Nonnull
        @Override
        public Mixer enable() {
            return this;
        }

        @Nonnull
        @Override
        public Mixer disable() {
            return this;
        }

        @Override
        protected JSONObject createPayload() {
            return new JSONObject();
        }
    }

    private class FiltersPayload extends VoidPayload {
        private final PlayerFilter[] filters;

        FiltersPayload(PlayerFilter[] filters) {
            super("filters");
            this.filters = filters;
        }

        @Override
        protected JSONObject createPayload() {
            final var json = new JSONObject();

            for (var filter : filters) {
                var entry = filter.updatePayload();
                json.put(entry.getKey(), entry.getValue());
            }

            return json;
        }
    }
}
