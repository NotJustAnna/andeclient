package pw.aru.lib.andeclient.entities.configurator;

import org.immutables.value.Value;
import pw.aru.lib.andeclient.annotations.Configurator;
import pw.aru.lib.andeclient.entities.AndeClient;
import pw.aru.lib.andeclient.entities.AndesiteNode;
import pw.aru.lib.andeclient.internal.AndesiteNodeImpl;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A configurator for a new andesite node.
 */
@Value.Modifiable
@Configurator
public abstract class AndesiteNodeConfigurator {
    public abstract AndeClient client();

    @Nullable
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

    @Nonnull
    public AndesiteNode create() {
        return new AndesiteNodeImpl(this);
    }
}
