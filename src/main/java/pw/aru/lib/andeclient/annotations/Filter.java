package pw.aru.lib.andeclient.annotations;

import org.immutables.value.Value;

/**
 * {@link org.immutables.value.Value.Style} annotation for Filters
 */
@Value.Style(
    packageGenerated = "*.internal",
    typeModifiable = "*Filter",
    typeImmutable = "*Filter",
    get = "*",
    set = "*",
    create = "new",
    build = "create",
    allParameters = true
)
public @interface Filter {}