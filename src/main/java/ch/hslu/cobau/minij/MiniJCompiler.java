package ch.hslu.cobau.minij;

import ch.hslu.cobau.minij.ast.AstBuilder;
import ch.hslu.cobau.minij.ast.entity.Unit;
import ch.hslu.cobau.minij.semantic.symbol.SymbolTable;
import ch.hslu.cobau.minij.semantic.symbol.SymbolTableBuilder;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;

public class MiniJCompiler {

    public static void main(String[] args) throws IOException {
        // initialize lexer and parser
        CharStream charStream;
        if (args.length > 0) {
            charStream = CharStreams.fromFileName(args[0]);
        } else {
            charStream = CharStreams.fromStream(System.in);
        }

        MiniJLexer miniJLexer = new MiniJLexer(charStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(miniJLexer);
        MiniJParser miniJParser = new MiniJParser(commonTokenStream);

        EnhancedConsoleErrorListener errorListener = new EnhancedConsoleErrorListener();
        miniJParser.removeErrorListeners();
        miniJParser.addErrorListener(errorListener);

        // start parsing at outermost level (milestone 2)
        MiniJParser.UnitContext unitContext = miniJParser.unit();

        // semantic check (milestone 3)
        AstBuilder astBuilder = new AstBuilder();
        SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder();
        Unit program;

        try {
            astBuilder.visit(unitContext);
            program = astBuilder.getUnit();
            program.accept(symbolTableBuilder);
        } catch (NumberFormatException e) {
            errorListener.semanticError("number out of range: " + e.getMessage());
        } catch (IllegalStateException e) {
            errorListener.semanticError(e.getMessage());
        } finally {
            if (errorListener.hasErrors()) {
                System.exit(1);
            }
        }

        // run the semantic analysis
        SymbolTable symbolTable = symbolTableBuilder.getSymbolTable();
        // TODO (lorin): check existence, function call args, type, etc.

        // code generation (milestone 4)

        System.exit(errorListener.hasErrors() ? 1 : 0);
    }
}
