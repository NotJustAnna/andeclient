package pw.aru.lib.andeclient.internal;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pw.aru.lib.andeclient.entities.AndeClient;
import pw.aru.lib.andeclient.entities.AndePlayer;
import pw.aru.lib.andeclient.entities.AndesiteNode;
import pw.aru.lib.andeclient.entities.PlayerState;
import pw.aru.lib.andeclient.events.player.internal.PostedNewPlayerEvent;

import javax.annotation.Nonnull;

public class AndePlayerImpl implements AndePlayer {
    private final AndeClientImpl client;
    private final AndesiteNodeImpl node;
    private final long guildId;

    public AudioTrack playingTrack;
    private PlayerState state = PlayerState.CONFIGURED;

    public AndePlayerImpl(AndeClient client, AndesiteNode node, long guildId) {
        this.client = (AndeClientImpl) client;
        this.node = (AndesiteNodeImpl) node;
        this.guildId = guildId;

        if (this.client.players.containsKey(guildId)) {
            throw new IllegalStateException("there's already a player for that guild!");
        }

        this.client.players.put(guildId, this);
        this.client.events.publish(PostedNewPlayerEvent.of(this));
    }

    @Nonnull
    @Override
    public AndesiteNode connectedNode() {
        return node;
    }

    @Nonnull
    @Override
    public AndeClient client() {
        return client;
    }

    @Nonnull
    @Override
    public PlayerState state() {
        return state;
    }

    @Override
    public long guildId() {
        return guildId;
    }

    @Override
    public void handleVoiceServerUpdate(String sessionId, String voiceToken, String endpoint) {
        node.sendVSU(guildId, sessionId, voiceToken, endpoint);
    }

    @Override
    public void destroy() {

    }
}
