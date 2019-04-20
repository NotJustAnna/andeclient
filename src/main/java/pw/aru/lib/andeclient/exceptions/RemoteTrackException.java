package pw.aru.lib.andeclient.exceptions;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pw.aru.lib.andeclient.entities.AndeClient;
import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.entities.AndesiteNode;

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

    public AndeClient client() {
        return client;
    }

    public AndePlayer player() {
        return this.player;
    }

    public AndesiteNode node() {
        return this.node;
    }

    public AudioTrack track() {
        return this.track;
    }

    public String reason() {
        return this.reason;
    }
}
