package pw.aru.lib.andeclient.annotations;

import org.immutables.value.Value;

/**
 * {@link org.immutables.value.Value.Style} annotation for Simple Data
 */
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
