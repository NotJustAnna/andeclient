package pw.aru.lib.andeclient.exceptions;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class HttpTrackException extends RuntimeException {
    private final int statusCode;
    private final String statusMessage;

    public HttpTrackException(@Nonnull final String message, @Nonnegative final int statusCode, @Nonnull final String statusMessage) {
        super(message);
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public int statusCode() {
        return this.statusCode;
    }

    public String statusMessage() {
        return this.statusMessage;
    }
}
