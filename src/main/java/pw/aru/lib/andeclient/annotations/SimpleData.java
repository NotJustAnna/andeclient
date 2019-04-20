package pw.aru.lib.andeclient.annotations;

import org.immutables.value.Value;

@Value.Style(
    packageGenerated = "*.internal",
    typeModifiable = "Actual*",
    typeImmutable = "Actual*",
    get = "*",
    set = "*",
    add = "plus*",
    addAll = "plus*",
    create = "new",
    defaults = @Value.Immutable(copy = false)
)
public @interface SimpleData {
}
