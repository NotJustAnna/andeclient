package pw.aru.lib.andeclient.entities.configurator;

import org.immutables.value.Value;
import pw.aru.lib.andeclient.annotations.Configurator;
import pw.aru.lib.andeclient.entities.AndeClient;
import pw.aru.lib.andeclient.entities.LoadBalancer;
import pw.aru.lib.andeclient.internal.AndeClientImpl;
import pw.aru.lib.andeclient.internal.DefaultLoadBalancer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@Value.Modifiable
@Configurator
public abstract class AndeClientConfigurator {
    @Nonnegative
    public abstract long userId();

    @Value.Default
    public LoadBalancer loadBalancer() {
        return DefaultLoadBalancer.INSTANCE;
    }

    @Nonnull
    public AndeClient create() {
        return new AndeClientImpl(userId(), loadBalancer());
    }
}
