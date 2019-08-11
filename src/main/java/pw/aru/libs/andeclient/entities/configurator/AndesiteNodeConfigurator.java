package pw.aru.libs.andeclient.entities.configurator;

import org.immutables.value.Value;
import pw.aru.libs.andeclient.annotations.Configurator;
import pw.aru.libs.andeclient.entities.AndeClient;
import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.entities.configurator.internal.ActualAndesiteNodeConfigurator;
import pw.aru.libs.andeclient.internal.AndesiteNodeImpl;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A configurator for a new andesite node.
 */
@Value.Modifiable
@Configurator
public abstract class AndesiteNodeConfigurator implements AndesiteNode.ConnectInfo {
    public abstract AndeClient client();

    @Nonnull
    @Value.Default
    public String host() {
        return "localhost";
    }

    @Nonnegative
    @Value.Default
    public int port() {
        return 5000;
    }

    @Nullable
    @Value.Default
    public String password() {
        return null;
    }

    @Value.Default
    public String relativePath() {
        return "";
    }

    @Nonnegative
    @Value.Default
    public int timeout() {
        return 2000;
    }

    @Nonnull
    public AndesiteNode create() {
        return new AndesiteNodeImpl(this);
    }

    @Nonnull
    @Override
    public ActualAndesiteNodeConfigurator asConfigurator() {
        return new ActualAndesiteNodeConfigurator().from(this);
    }
}
