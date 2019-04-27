package pw.aru.libs.andeclient.exceptions;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pw.aru.libs.andeclient.entities.AndeClient;
import pw.aru.libs.andeclient.entities.AndePlayer;
import pw.aru.libs.andeclient.entities.AndesiteNode;

/**
 * An Exception that came from a TrackExceptionEvent.
 */
public class RemoteTrackException extends RuntimeException {
    private final AndeClient client;
    private final AudioTrack track;
    private final AndesiteNode node;
    private final AndePlayer player;
    private final String reason;

    public RemoteTrackException(final AndeClient client, final AndePlayer player, final AndesiteNode node, final AudioTrack track, final String reason) {
        super(reason);
        this.client = client;
        this.track = track;
        this.node = node;
        this.player = player;
        this.reason = reason;
    }

    /**
     * The AndeClient related to the exception.
     *
     * @return an AndeClient.
     */
    public AndeClient client() {
        return client;
    }

    /**
     * The player related to the exception.
     * @return a player.
     */
    public AndePlayer player() {
        return this.player;
    }

    /**
     * The node related to the exception.
     * @return a node.
     */
    public AndesiteNode node() {
        return this.node;
    }

    /**
     * The audio track related to the exception.
     * @return an audio track.
     */
    public AudioTrack track() {
        return this.track;
    }

    /**
     * The reason that made the TrackExceptionEvent happen
     * @return the reason string.
     */
    public String reason() {
        return this.reason;
    }
}
