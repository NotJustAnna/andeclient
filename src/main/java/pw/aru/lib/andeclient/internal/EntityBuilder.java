package pw.aru.lib.andeclient.internal;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.aru.lib.andeclient.entities.AndesiteNode;
import pw.aru.lib.andeclient.entities.AudioLoadResult;
import pw.aru.lib.andeclient.entities.internal.*;
import pw.aru.lib.andeclient.util.AudioTrackUtil;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EntityBuilder {
    private static final Logger logger = LoggerFactory.getLogger(EntityBuilder.class);

    public static AndesiteNode.Info nodeInfo(JSONObject json) {
        return ActualInfo.builder()
            .version(json.getString("version"))
            .versionMajor(json.getString("version-major"))
            .versionMinor(json.getString("version-minor"))
            .versionRevision(json.getString("version-revision"))
            .versionCommit(json.getString("version-commit"))
            .versionBuild(json.getLong("version-build"))
            .nodeRegion(json.getString("node-region"))
            .nodeId(json.getString("node-id"))
            .enabledSources(toStringList(json.getJSONArray("enabled-sources")))
            .loadedPlugins(toStringList(json.getJSONArray("loaded-plugins")))
            .build();
    }

    private static List<String> toStringList(JSONArray array) {
        return StreamSupport.stream(array.spliterator(), false)
            .map(Object::toString)
            .collect(Collectors.toUnmodifiableList());
    }

    public static AndesiteNode.Stats nodeStats(JSONObject json) {
        final var jsonCpu = json.getJSONObject("cpu");
        final var jsonRam = json.getJSONObject("memory");
        final var jsonFrames = Objects.requireNonNullElse(json.optJSONObject("frameStats"), new JSONObject());

        return ActualStats.builder()
            .players(json.getInt("players"))
            .playingPlayers(json.getInt("playingPlayers"))
            .uptime(json.getLong("uptime"))
            .cpuCores(jsonCpu.getInt("cores"))
            .systemLoad(jsonCpu.getDouble("systemLoad"))
            .andesiteLoad(jsonCpu.getDouble("systemLoad")) //TODO
            .usedMemory(jsonRam.getInt("used"))
            .allocatedMemory(jsonRam.getInt("allocated"))
            .freeMemory(jsonRam.getInt("free"))
            .reservableMemory(jsonRam.getInt("reservable"))
            .sentFrames(jsonFrames.optLong("sent", 0))
            .nulledFrames(jsonFrames.optLong("nulled", 0))
            .deficitFrames(jsonFrames.optLong("deficit", 0))
            .build();
    }

    public static AudioLoadResult audioLoadResult(JSONObject json) {
        switch (json.getString("loadType")) {
            case "TRACK_LOADED": {
                return ActualTrack.builder()
                    .loadedTrack(
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
                final var selected = info.optInt("selected", -1);

                return ActualPlaylist.builder()
                    .searchResults(json.getString("loadType").equals("SEARCH_RESULTS"))
                    .tracks(tracks)
                    .playlistName(name)
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
}
