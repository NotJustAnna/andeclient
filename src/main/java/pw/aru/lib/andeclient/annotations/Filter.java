package pw.aru.lib.andeclient.annotations;

import org.immutables.value.Value;

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