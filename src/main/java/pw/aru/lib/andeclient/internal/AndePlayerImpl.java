package pw.aru.lib.andeclient.internal;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pw.aru.lib.andeclient.entities.*;
import pw.aru.lib.andeclient.entities.configurator.AndePlayerConfigurator;
import pw.aru.lib.andeclient.events.player.internal.PostedNewPlayerEvent;

import javax.annotation.Nonnull;

public class AndePlayerImpl implements AndePlayer {
    private final AndeClientImpl client;
    private final AndesiteNodeImpl node;
    private final long guildId;

    public AudioTrack playingTrack;
    private PlayerState state = PlayerState.CONFIGURED;

    public AndePlayerImpl(AndePlayerConfigurator configurator) {
        this.client = (AndeClientImpl) configurator.client();
        this.node = (AndesiteNodeImpl) configurator.andesiteNode();
        this.guildId = configurator.guildId();

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
    public PlayerControls controls() {
        return new PlayerControlsImpl(this, client, node);
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
        //TODO
    }
}
