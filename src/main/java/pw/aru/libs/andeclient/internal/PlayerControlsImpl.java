package pw.aru.libs.andeclient.internal;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.aru.libs.andeclient.entities.AndePlayer;
import pw.aru.libs.andeclient.entities.EntityState;
import pw.aru.libs.andeclient.entities.player.PlayerControls;
import pw.aru.libs.andeclient.entities.player.PlayerFilter;
import pw.aru.libs.andeclient.util.AudioTrackUtil;

import javax.annotation.Nonnull;

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
        return new PlayAction();
    }

    @Nonnull
    @Override
    public Action pause() {
        return new SimpleAction("pause", new JSONObject().put("pause", true));
    }

    @Nonnull
    @Override
    public Action resume() {
        return new SimpleAction("pause", new JSONObject().put("pause", false));
    }

    @Nonnull
    @Override
    public Action volume(int volume) {
        return new SimpleAction("volume", new JSONObject().put("volume", volume));
    }

    @Nonnull
    @Override
    public Mixer mixer() {
        return new MixerAction();
    }

    @Nonnull
    @Override
    public Action filters(PlayerFilter... filters) {
        return new FiltersAction(filters);
    }

    @Nonnull
    @Override
    public Action seek(long position) {
        return new SimpleAction("seek", new JSONObject().put("position", position));
    }

    @Nonnull
    @Override
    public Action stop() {
        return new EmptyAction("stop");
    }

    private abstract class AbstractAction implements Action {
        private final String op;

        AbstractAction(String op) {
            this.op = op;
        }

        protected abstract JSONObject createPayload();

        @Nonnull
        @Override
        public PlayerControls execute() {
            if (player.state == EntityState.DESTROYED) {
                throw new IllegalStateException("Destroyed AndePlayer, please create a new one with AndeClient#newPlayer.");
            }

            player.node.handleOutgoing(
                createPayload()
                    .put("op", op)
                    .put("guildId", Long.toString(player.guildId()))
            );
            return PlayerControlsImpl.this;
        }
    }

    private class EmptyAction extends AbstractAction {
        EmptyAction(String op) {
            super(op);
        }

        @Override
        protected JSONObject createPayload() {
            return new JSONObject();
        }
    }

    private class SimpleAction extends AbstractAction {
        private final JSONObject payload;

        SimpleAction(String op, JSONObject payload) {
            super(op);
            this.payload = payload;
        }

        @Override
        protected JSONObject createPayload() {
            return payload;
        }
    }

    private class PlayAction extends AbstractAction implements Play {
        private String trackString;
        private Long start;
        private Long end;
        private boolean noReplace;
        private Boolean pause;
        private Integer volume;

        PlayAction() {
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

    private class MixerAction extends AbstractAction implements Mixer {
        MixerAction() {
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

    private class FiltersAction extends AbstractAction {
        private final PlayerFilter[] filters;

        FiltersAction(PlayerFilter[] filters) {
            super("filters");
            this.filters = filters;
        }

        @Override
        protected JSONObject createPayload() {
            final var json = new JSONObject();

            for (PlayerFilter filter : filters) {
                if (filter instanceof PlayerFilter.Equalizer) {
                    final var bands = ((PlayerFilter.Equalizer) filter).bands();
                    final var array = new JSONArray();
                    for (int band = 0; band < bands.length; band++) {
                        final var gain = bands[band];
                        if (gain != null) {
                            array.put(new JSONObject().put("band", band).put("gain", gain));
                        }
                    }
                    if (!array.isEmpty()) {
                        json.put("equalizer", new JSONObject().put("bands", array));
                    }
                } else if (filter instanceof PlayerFilter.Karaoke) {
                    final var karaoke = (PlayerFilter.Karaoke) filter;

                    json.put(
                        "karaoke", new JSONObject()
                            .put("level", karaoke.level())
                            .put("monoLevel", karaoke.monoLevel())
                            .put("filterBand", karaoke.filterBand())
                            .put("filterWidth", karaoke.filterWidth())
                    );
                } else if (filter instanceof PlayerFilter.Timescale) {
                    final var timescale = (PlayerFilter.Timescale) filter;

                    json.put(
                        "timescale", new JSONObject()
                            .put("speed", timescale.speed())
                            .put("pitch", timescale.pitch())
                            .put("rate", timescale.rate())
                    );
                } else if (filter instanceof PlayerFilter.Tremolo) {
                    final var tremolo = (PlayerFilter.Tremolo) filter;

                    json.put(
                        "tremolo", new JSONObject()
                            .put("frequency", tremolo.frequency())
                            .put("depth", tremolo.depth())
                    );
                } else if (filter instanceof PlayerFilter.Vibrato) {
                    final var vibrato = (PlayerFilter.Vibrato) filter;

                    json.put(
                        "vibrato", new JSONObject()
                            .put("frequency", vibrato.frequency())
                            .put("depth", vibrato.depth())
                    );
                } else if (filter instanceof PlayerFilter.Volume) {
                    json.put("volume", new JSONObject().put("volume", ((PlayerFilter.Volume) filter).value()));
                }
            }

            return json;
        }
    }
}
