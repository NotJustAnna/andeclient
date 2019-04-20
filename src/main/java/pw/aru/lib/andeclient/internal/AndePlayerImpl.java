package pw.aru.lib.andeclient.internal;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pw.aru.lib.andeclient.entities.*;
import pw.aru.lib.andeclient.events.player.internal.PostedNewPlayerEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletionStage;

public class AndePlayerImpl implements AndePlayer {
    private final AndeClientImpl client;
    private final AndesiteNode node;
    private final long guildId;
    private final String sessionId;
    private final String voiceToken;
    private final String endpoint;

    public AudioTrack playingTrack;
    private PlayerState state = PlayerState.CONFIGURED;

    public AndePlayerImpl(AndeClient client, AndesiteNode node, long guildId, String sessionId, String voiceToken, String endpoint) {
        this.client = (AndeClientImpl) client;
        this.node = node;
        this.guildId = guildId;
        this.sessionId = sessionId;
        this.voiceToken = voiceToken;
        this.endpoint = endpoint;

        this.client.events.publish(PostedNewPlayerEvent.of(this));
        init();
    }

    private void init() {
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

    @Nullable
    @Override
    public AudioTrack playingTrack() {
        return null;
    }

    @Override
    public long guildId() {
        return guildId;
    }

    @Override
    public long timestamp() {
        return 0;
    }

    @Override
    public long position() {
        return 0;
    }

    @Override
    public int volume() {
        return 0;
    }

    @Override
    public boolean paused() {
        return false;
    }

    @Nonnull
    @Override
    public CompletionStage<AudioLoadResult> loadTracksAsync(@Nonnull String identifier) {
        return null;
    }

    @Override
    public void play(@Nonnull String trackData, long startTime, long endTime, boolean noReplace) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void seek(long position) {

    }

    @Override
    public void volume(int volume) {

    }
}
