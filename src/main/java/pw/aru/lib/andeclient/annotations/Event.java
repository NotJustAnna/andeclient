package pw.aru.lib.andeclient.annotations;

import org.immutables.value.Value;

/**
 * {@link org.immutables.value.Value.Style} annotation for Events
 */
@Value.Style(
    packageGenerated = "*.internal",
    typeImmutable = "Posted*",
    get = "*",
    defaults = @Value.Immutable(copy = false)
)
public @interface Event {
}
