package pw.aru.libs.andeclient.entities.configurator;

import org.immutables.value.Value;
import pw.aru.libs.andeclient.annotations.Configurator;
import pw.aru.libs.andeclient.entities.AndeClient;
import pw.aru.libs.andeclient.entities.LoadBalancer;
import pw.aru.libs.andeclient.internal.AndeClientImpl;
import pw.aru.libs.andeclient.internal.DefaultLoadBalancer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.net.http.HttpClient;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A configurator for a new AndeClient instance.
 */
@Value.Modifiable
@Configurator
public abstract class AndeClientConfigurator {
    @Nonnegative
    public abstract long userId();

    @Value.Default
    public LoadBalancer loadBalancer() {
        return DefaultLoadBalancer.INSTANCE;
    }

    @Value.Default
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Value.Default
    public ScheduledExecutorService executor() {
        return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Nonnull
    public AndeClient create() {
        return new AndeClientImpl(this);
    }
}
