package pw.aru.libs.andeclient.entities.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pw.aru.libs.andeclient.entities.AndePlayer;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletionStage;

/**
 * Control interface of an AndePlayer.
 * Sends control payloads to the player's andesite node.
 */
@SuppressWarnings("UnusedReturnValue")
public interface PlayerControls {
    /**
     * Gets the player
     *
     * @return the player which is affected by this interface
     */
    @Nonnull
    AndePlayer player();

    /**
     * Creates a new Play payload. Call {@link Play#submit()} to send the payload to the player.
     *
     * @return a configurable Play payload.
     */
    @Nonnull
    @CheckReturnValue
    Play play();

    /**
     * Creates a new Pause payload. Call {@link Payload#submit()} to send the payload to the player.
     *
     * @return a configurable Pause payload.
     */
    @Nonnull
    @CheckReturnValue
    Payload<Void> pause();

    /**
     * Creates a new Resume payload. Call {@link Payload#submit()} to send the payload to the player.
     *
     * @return a configurable Resume payload.
     */
    @Nonnull
    @CheckReturnValue
    Payload<Void> resume();

    /**
     * Creates a new Volume payload. Call {@link Payload#submit()} to send the payload to the player.
     *
     * @param volume the volume to be set on the player.
     * @return a configurable Volume payload.
     */
    @Nonnull
    @CheckReturnValue
    Payload<Void> volume(int volume);

    /**
     * Creates a new Mixer payload. Call {@link Mixer#submit()} to send the payload to the player.
     *
     * @return a configurable Mixer payload.
     * @implNote MIXER IS NOT IMPLEMENTED YET.
     */
    @Nonnull
    @CheckReturnValue
    Mixer mixer();

    /**
     * Creates a new Filters payload. Call {@link Payload#submit()} to send the payload to the player.
     *
     * @param filters the filters to be updated on the player.
     * @return a configurable Filters payload.
     */
    @Nonnull
    @CheckReturnValue
    Payload<Void> filters(PlayerFilter... filters);

    /**
     * Creates a new Seek payload. Call {@link Payload#submit()} to send the payload to the player.
     *
     * @return a configurable Seek payload.
     */
    @Nonnull
    @CheckReturnValue
    Payload<Void> seek(long position);

    /**
     * Creates a new Stop payload. Call {@link Payload#submit()} to send the payload to the player.
     *
     * @return a configurable Filters payload.
     */
    @Nonnull
    @CheckReturnValue
    Payload<Void> stop();

    /**
     * A payload which can be sent to the player.
     *
     * @param <T> the return type of the {@link CompletionStage} on {@link Payload#submit()}.
     */
    interface Payload<T> {
        /**
         * Sends the payload to the player, as well as a follow-up validation that the payload was received.
         * @return a {@link CompletionStage} that completes once the node ensures that the server received the payload.
         */
        @Nonnull
        CompletionStage<T> submit();
    }

    interface Play extends Payload<Void> {
        @Nonnull
        @CheckReturnValue
        Play track(@Nonnull String trackString);

        @Nonnull
        @CheckReturnValue
        Play track(@Nonnull AudioTrack track);

        @Nonnull
        @CheckReturnValue
        Play start(@Nullable Long timestamp);

        @Nonnull
        @CheckReturnValue
        Play end(@Nullable Long timestamp);

        @Nonnull
        @CheckReturnValue
        Play noReplace();

        @Nonnull
        @CheckReturnValue
        Play replacing();

        @Nonnull
        @CheckReturnValue
        Play pause(@Nullable Boolean isPaused);

        @Nonnull
        @CheckReturnValue
        Play volume(@Nullable Integer volume);
    }

    interface Mixer extends Payload<Void> {
        @Nonnull
        @CheckReturnValue
        Mixer enable();

        @Nonnull
        @CheckReturnValue
        Mixer disable();
    }
}
