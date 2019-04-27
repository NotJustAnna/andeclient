package pw.aru.lib.andeclient.util;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.aru.lib.andeclient.entities.AndesiteNode;
import pw.aru.lib.andeclient.entities.AudioLoadResult;
import pw.aru.lib.andeclient.entities.internal.*;
import pw.aru.lib.andeclient.entities.player.PlayerFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AndesiteUtil {
    private static final Logger logger = LoggerFactory.getLogger(AndesiteUtil.class);

    public static AndesiteNode.Info nodeInfo(JSONObject json) {
        return ActualInfo.builder()
            .version(json.getString("version"))
            .versionMajor(json.getString("versionMajor"))
            .versionMinor(json.getString("versionMinor"))
            .versionRevision(json.getString("versionRevision"))
            .versionCommit(json.getString("versionCommit"))
            .versionBuild(json.getLong("versionBuild"))
            .nodeRegion(json.getString("nodeRegion"))
            .nodeId(json.getString("nodeId"))
            .enabledSources(toStringList(json.getJSONArray("enabledSources")))
            .loadedPlugins(toStringList(json.getJSONArray("loadedPlugins")))
            .build();
    }

    public static AndesiteNode.Stats nodeStats(JSONObject json) {
        final var jsonPlayers = json.getJSONObject("players");
        final var jsonCpu = json.optJSONObject("cpu");
        final var jsonFrames = json.optJSONObject("frameStats");

        return ActualStats.builder()
            .players(jsonPlayers.getInt("total"))
            .playingPlayers(jsonPlayers.getInt("playing"))
            .uptime(json.getJSONObject("runtime").getLong("uptime"))
            .systemLoad(jsonCpu == null ? 0 : jsonCpu.getDouble("system"))
            .andesiteLoad(jsonCpu == null ? 0 : jsonCpu.getDouble("andesite"))
            .sentFrames(jsonFrames.optLong("sent", 0))
            .nulledFrames(jsonFrames.optLong("nulled", 0))
            .deficitFrames(jsonFrames.optLong("deficit", 0))
            .build();
    }

    public static AudioLoadResult audioLoadResult(JSONObject json) {
        switch (json.getString("loadType")) {
            case "TRACK_LOADED": {
                return ActualTrack.builder()
                    .track(
                        AudioTrackUtil.fromString(json.getJSONArray("tracks").getJSONObject(0).getString("track"))
                    )
                    .build();
            }
            case "PLAYLIST_LOADED":
            case "SEARCH_RESULTS": {
                var tracks = StreamSupport.stream(json.getJSONArray("tracks").spliterator(), false)
                    .filter(track -> track instanceof JSONObject)
                    .map(track -> (JSONObject) track)
                    .map(jsonTrack -> AudioTrackUtil.fromString(jsonTrack.getString("track")))
                    .collect(Collectors.toUnmodifiableList());

                final var info = json.getJSONObject("playlistInfo");
                final var name = info.getString("name");
                final var selected = info.optInt("selectedTrack", -1);

                return ActualPlaylist.builder()
                    .searchResults(json.getString("loadType").equals("SEARCH_RESULTS"))
                    .tracks(tracks)
                    .playlistName(name)
                    .selectedIndex(selected)
                    .selectedTrack(selected < 0 ? null : tracks.get(selected))
                    .build();
            }
            case "LOAD_FAILED": {
                return ActualFailed.builder()
                    .cause(json.getJSONObject("cause").getString("message"))
                    .severity(FriendlyException.Severity.valueOf(json.getString("severity")))
                    .build();
            }
            case "NO_MATCHES": {
                return AudioLoadResult.NO_MATCHES;
            }
            default: {
                logger.warn("unknown loadType {} | raw json is {}", json.getString("loadType"), json);
                return AudioLoadResult.UNKNOWN;
            }
        }
    }

    private static List<String> toStringList(JSONArray array) {
        return StreamSupport.stream(array.spliterator(), false)
            .map(Object::toString)
            .collect(Collectors.toUnmodifiableList());
    }

    public static List<PlayerFilter> playerFilters(JSONObject jsonFilters) {
        var filters = new ArrayList<PlayerFilter>();

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

        return List.copyOf(filters);
    }
}
