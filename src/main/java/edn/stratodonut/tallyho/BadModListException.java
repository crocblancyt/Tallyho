package edn.stratodonut.tallyho;

public class BadModListException extends RuntimeException {
    public BadModListException(String missing) {
        super("Tallyho is not supported on this mod list. (Missing " + missing + ") You will not receive support for this issue.");
    }
}
