package pw.aru.lib.andeclient.entities;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;

/**
 * An implementation meant to tweak the process of calculating the best node available.
 *
 * @see pw.aru.lib.andeclient.internal.DefaultLoadBalancer
 */
public interface LoadBalancer {
    @CheckReturnValue
    @Nonnegative
    int playerPenalty(AndesiteNode.Stats stats);

    @CheckReturnValue
    @Nonnegative
    int cpuPenalty(AndesiteNode.Stats stats);

    @CheckReturnValue
    @Nonnegative
    int deficitFramePenalty(AndesiteNode.Stats stats);

    @CheckReturnValue
    @Nonnegative
    int nullFramePenalty(AndesiteNode.Stats stats);

    @CheckReturnValue
    @Nonnegative
    default int totalPenalty(int playerPenalty, int cpuPenalty, int deficitFramePenalty, int nullFramePenalty) {
        return playerPenalty + cpuPenalty + deficitFramePenalty + nullFramePenalty;
    }

    @CheckReturnValue
    @Nonnegative
    default int totalPenalty(AndesiteNode.Stats stats) {
        return totalPenalty(playerPenalty(stats), cpuPenalty(stats), deficitFramePenalty(stats), nullFramePenalty(stats));
    }
}
