package pw.aru.lib.andeclient.annotations;

import org.immutables.value.Value;

@Value.Style(
    packageGenerated = "*.internal",
    typeImmutable = "Posted*",
    get = "*",
    defaults = @Value.Immutable(copy = false)
)
public @interface Event {
}
