package pw.aru.lib.andeclient.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.immutables.value.Value;
import pw.aru.lib.andeclient.annotations.SimpleData;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Value.Immutable
@SimpleData
public interface AudioLoadResult {
    @CheckReturnValue
    @Nonnull
    List<AudioTrack> tracks();

    @CheckReturnValue
    @Nullable
    AudioTrack first();

    @CheckReturnValue
    boolean playlist();

    @CheckReturnValue
    @Nonnull
    LoadType type();

    @CheckReturnValue
    @Nullable
    String playlistName();

    @CheckReturnValue
    @Nullable
    @Nonnegative
    Integer selectedTrack();
}
