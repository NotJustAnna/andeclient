package pw.aru.libs.andeclient.internal;

import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.entities.LoadBalancer;

/**
 * The default load balancer strategy used by AndeClient.
 */
public class DefaultLoadBalancer implements LoadBalancer {
    public static final LoadBalancer INSTANCE = new DefaultLoadBalancer();

    private DefaultLoadBalancer() {}

    @Override
    public int playerPenalty(AndesiteNode.Stats stats) {
        return stats.playingPlayers();
    }

    @Override
    public int cpuPenalty(AndesiteNode.Stats stats) {
        return (int) Math.pow(1.05d, 100 * stats.systemLoad()) * 10 - 10;
    }

    @Override
    public int deficitFramePenalty(AndesiteNode.Stats stats) {
        return (int) (Math.pow(1.03d, 500f * ((float) stats.deficitFrames() / 3000f)) * 600 - 600);
    }

    @Override
    public int nullFramePenalty(AndesiteNode.Stats stats) {
        return ((int) (Math.pow(1.03d, 500f * ((float) stats.nulledFrames() / 3000f)) * 300 - 300)) * 2;
    }
}
