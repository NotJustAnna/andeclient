package pw.aru.lib.andeclient.entities.configurator;

import org.immutables.value.Value;
import pw.aru.lib.andeclient.annotations.Configurator;
import pw.aru.lib.andeclient.entities.AndeClient;
import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.entities.AndesiteNode;
import pw.aru.lib.andeclient.internal.AndePlayerImpl;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@Value.Modifiable
@Configurator
public abstract class AndePlayerConfigurator {
    public abstract AndeClient client();

    @Nonnegative
    public abstract long guildId();

    @Nonnull
    public abstract AndesiteNode andesiteNode();

    @Nonnull
    public AndePlayer create() {
        return new AndePlayerImpl(client(), andesiteNode(), guildId());
    }
}
