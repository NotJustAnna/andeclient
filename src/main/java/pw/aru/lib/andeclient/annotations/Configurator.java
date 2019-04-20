package pw.aru.lib.andeclient.annotations;

import org.immutables.value.Value;

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
