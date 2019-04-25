package pw.aru.lib.andeclient.annotations;

import org.immutables.value.Value;

/**
 * {@link org.immutables.value.Value.Style} annotation for Configurators
 */
@Value.Style(
    packageGenerated = "*.internal",
    typeModifiable = "Actual*",
    get = "*",
    set = "*",
    create = "new",
    defaults = @Value.Immutable(copy = false)
)
public @interface Configurator {
}
