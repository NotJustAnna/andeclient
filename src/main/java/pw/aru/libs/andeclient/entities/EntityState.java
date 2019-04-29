package pw.aru.libs.andeclient.entities;

/**
 * Represents the state of entities which can be destroyed.
 */
public enum EntityState {
    /**
     * The entity has been instantiated but it's doing a major operation which might substantially affects it's features.
     */
    CONFIGURING,
    /**
     * The entity is available for complete usage.
     */
    AVAILABLE,
    /**
     * The entity was destroyed and mustn't be used any further.
     */
    DESTROYED
}
