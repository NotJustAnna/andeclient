package pw.aru.lib.andeclient.internal;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.aru.lib.andeclient.entities.AndeClient;
import pw.aru.lib.andeclient.entities.PlayerControls;
import pw.aru.lib.andeclient.entities.PlayerFilter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerControlsImpl implements PlayerControls {
    private final AndePlayerImpl player;
    private final AndeClientImpl client;
    private final AndesiteNodeImpl node;

    PlayerControlsImpl(AndePlayerImpl player, AndeClientImpl client, AndesiteNodeImpl node) {
        this.player = player;
        this.client = client;
        this.node = node;
    }

    @Nonnull
    @Override
    public AndeClient client() {
        return client;
    }

    @Override
    public Play play() {
        return new PlayAction();
    }

    @Override
    public Action pause(boolean isPaused) {
        return new SimpleAction("pause", new JSONObject().put("pause", isPaused));
    }

    @Override
    public Action volume(int volume) {
        return new SimpleAction("volume", new JSONObject().put("volume", volume));
    }

    @Override
    public Mixer mixer() {
        return new MixerAction();
    }

    @Override
    public Action filters(PlayerFilter... filters) {
        return new FiltersAction(filters);
    }

    @Override
    public Action seek(long position) {
        return new SimpleAction("seek", new JSONObject().put("position", position));
    }

    private abstract class AbstractAction implements Action {
        private final String op;

        AbstractAction(String op) {
            this.op = op;
        }

        protected abstract JSONObject createPayload();

        @Override
        public PlayerControls execute() {
            node.handleOutcoming(
                createPayload()
                    .put("op", op)
                    .put("guildId", player.guildId())
            );
            return PlayerControlsImpl.this;
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
        PlayAction() {
            super("play");
        }

        @Nonnull
        @Override
        public Play track(String trackString) {
            return this;
        }

        @Nonnull
        @Override
        public Play track(AudioTrack track) {
            return this;
        }

        @Nonnull
        @Override
        public Play start(long timestamp) {
            return this;
        }

        @Nonnull
        @Override
        public Play end(long timestamp) {
            return this;
        }

        @Nonnull
        @Override
        public Play noReplace() {
            return this;
        }

        @Nonnull
        @Override
        public Play replacing() {
            return this;
        }

        @Nonnull
        @Override
        public Play pause(boolean isPaused) {
            return this;
        }

        @Nonnull
        @Override
        public Play volume(@Nullable Integer volume) {
            return this;
        }

        @Override
        protected JSONObject createPayload() {
            return new JSONObject();
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
