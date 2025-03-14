package ch.hslu.cobau.minij;

import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Extends the error listener of ANTLR to report semantic errors.
 */
public class EnhancedConsoleErrorListener extends ConsoleErrorListener {

    private boolean hasErrors;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
        hasErrors = true;
    }

    /**
     * Prints a semantic error message to stderr.
     *
     * @param message The semantic error message.
     */
    public void semanticError(String message) {
        System.err.println(message);
        hasErrors = true;
    }

    /**
     * Returns true if any error was reported.
     *
     * @return True if an error occurred, false if not.
     */
    public boolean hasErrors() {
        return hasErrors;
    }
}
