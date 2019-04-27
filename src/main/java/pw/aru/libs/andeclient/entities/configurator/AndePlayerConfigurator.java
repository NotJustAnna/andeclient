package pw.aru.libs.andeclient.entities.configurator;

import org.immutables.value.Value;
import pw.aru.libs.andeclient.annotations.Configurator;
import pw.aru.libs.andeclient.entities.AndeClient;
import pw.aru.libs.andeclient.entities.AndePlayer;
import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.internal.AndePlayerImpl;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A configurator for a new player.
 */
@Value.Modifiable
@Configurator
public abstract class AndePlayerConfigurator {
    public abstract AndeClient client();

    @Nonnegative
    public abstract long guildId();

    public abstract AndesiteNode andesiteNode();

    @Nonnull
    public AndePlayer create() {
        return new AndePlayerImpl(this);
    }
}
