package pw.aru.lib.andeclient.exceptions;

import javax.annotation.Nonnull;

public class LocalTrackException extends RuntimeException {
    public LocalTrackException(@Nonnull final Exception e) {
        super(e);
    }
}
