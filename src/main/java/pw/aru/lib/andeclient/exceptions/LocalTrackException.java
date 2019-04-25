package pw.aru.lib.andeclient.exceptions;

/**
 * An exception that happened locally while trying to convert tracks back and forth.
 */
public class LocalTrackException extends RuntimeException {
    public LocalTrackException(Exception e) {
        super(e);
    }
}
