package pw.aru.lib.andeclient.entities;

import org.immutables.value.Value;
import org.json.JSONObject;
import pw.aru.lib.andeclient.annotations.SimpleData;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletionStage;

@SuppressWarnings("unused")
public interface AndesiteNode {
    @Nonnull
    @CheckReturnValue
    AndeClient client();

    @CheckReturnValue
    boolean available();

    void closeConnection();

    @Nonnull
    @CheckReturnValue
    Info nodeInfo();

    @Nonnull
    @CheckReturnValue
    CompletionStage<Stats> stats();

    @Nonnull
    @CheckReturnValue
    CompletionStage<AudioLoadResult> loadTracksAsync(String identifier);

    @Value.Immutable
    @SimpleData
    interface Info {
        String version();

        String versionMajor();

        String versionMinor();

        String versionRevision();

        long versionBuild();

        String versionCommit();

        String nodeRegion();

        String nodeId();

        List<String> enabledSources();

        List<String> loadedPlugins();
    }

    @Value.Immutable
    @SimpleData
    interface Stats {
        AndesiteNode node();

        JSONObject raw();

        int players();

        int playingPlayers();

        long uptime();

        long freeMemory();

        long allocatedMemory();

        long usedMemory();

        long reservableMemory();

        int cpuCores();

        double systemLoad();

        double lavalinkLoad();

        long sentFrames();

        long nulledFrames();

        long deficitFrames();
    }
}
