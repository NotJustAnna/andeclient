package pw.aru.libs.andeclient.entities.client;

import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.entities.configurator.internal.ActualAndesiteNodeConfigurator;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * This interface is the part of the AndeClient responsible of managing the andesite nodes.
 */
public interface NodeManager {
    /**
     * Creates a configurator which will add a node to this manager after connected.
     *
     * @return a node configurator bound to the AndeClient.
     */
    @CheckReturnValue
    @Nonnull
    ActualAndesiteNodeConfigurator newNode();

    /**
     * Retrieves all nodes from the client.
     *
     * @return a read-only list of the current nodes.
     */
    @Nonnull
    @CheckReturnValue
    List<AndesiteNode> nodes();

    /**
     * Calculates the node with the less overhead currently.
     *
     * @return the best node according to the load balancing.
     */
    @Nonnull
    @CheckReturnValue
    AndesiteNode bestNode();
}
