package pw.aru.lib.andeclient.internal;

import org.json.JSONArray;
import org.json.JSONObject;
import pw.aru.lib.andeclient.entities.AndesiteNode;
import pw.aru.lib.andeclient.entities.internal.ActualInfo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EntityBuilder {
/*
    public static AndesiteNode.Info nodeInfoFromHeaders(HttpHeaders headers) {
        return ActualInfo.builder()
            .version(
                headers.firstValue("Andesite-Version").orElse("")
            )
            .versionMajor(
                headers.firstValue("Andesite-Version-Major").orElse("")
            )
            .versionMinor(
                headers.firstValue("Andesite-Version-Minor").orElse("")
            )
            .versionRevision(
                headers.firstValue("Andesite-Version-Revision").orElse("")
            )
            .versionCommit(
                headers.firstValue("Andesite-Version-Commit").orElse("")
            )
            .versionBuild(
                headers.firstValueAsLong("Andesite-Version-Build").orElse(0)
            )
            .nodeRegion(
                headers.firstValue("Andesite-Node-Region").orElse("")
            )
            .nodeId(
                headers.firstValue("Andesite-Node-Id").orElse("")
            )
            .plusEnabledSources(
                headers.firstValue("Andesite-Enabled-Sources")
                    .orElse("").split(",")
            )
            .plusLoadedPlugins(
                headers.firstValue("Andesite-Loaded-Plugins")
                    .orElse("").split(",")
            )
            .build();
    }
*/

    public static AndesiteNode.Info nodeInfo(JSONObject json) {
        return ActualInfo.builder()
            .version(
                json.getString("version")
            )
            .versionMajor(
                json.getString("version-major")
            )
            .versionMinor(
                json.getString("version-minor")
            )
            .versionRevision(
                json.getString("version-revision")
            )
            .versionCommit(
                json.getString("version-commit")
            )
            .versionBuild(
                json.getLong("version-build")
            )
            .nodeRegion(
                json.getString("node-region")
            )
            .nodeId(
                json.getString("node-id")
            )
            .enabledSources(
                toStringList(json.getJSONArray("enabled-sources"))
            )
            .loadedPlugins(
                toStringList(json.getJSONArray("loaded-plugins"))
            )
            .build();

    }

    private static List<String> toStringList(JSONArray array) {
        return StreamSupport.stream(array.spliterator(), false)
            .map(Object::toString)
            .collect(Collectors.toUnmodifiableList());
    }

    public static AndesiteNode.Stats nodeStats(JSONObject stats) {
        return null;
    }
}
