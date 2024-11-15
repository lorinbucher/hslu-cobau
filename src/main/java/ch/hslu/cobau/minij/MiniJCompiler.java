package ch.hslu.cobau.minij;

import ch.hslu.cobau.minij.ast.AstBuilder;
import ch.hslu.cobau.minij.ast.entity.Unit;
import ch.hslu.cobau.minij.semantic.SemanticAnalyser;
import ch.hslu.cobau.minij.semantic.SymbolTable;
import ch.hslu.cobau.minij.semantic.SymbolTableBuilder;
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
        if (errorListener.hasErrors()) {
            System.exit(1);
        }

        // semantic check (milestone 3)
        AstBuilder astBuilder = new AstBuilder();
        try {
            astBuilder.visit(unitContext);
        } catch (NumberFormatException e) {
            errorListener.semanticError("number out of range: " + e.getMessage());
            System.exit(1);
        }

        // build the symbol table
        Unit program = astBuilder.getUnit();
        SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder(errorListener);
        program.accept(symbolTableBuilder);
        SymbolTable symbolTable = symbolTableBuilder.getSymbolTable();

        // run the semantic analysis
        SemanticAnalyser semanticAnalyser = new SemanticAnalyser(errorListener, symbolTable);
        program.accept(semanticAnalyser);

        // code generation (milestone 4)

        System.exit(errorListener.hasErrors() ? 1 : 0);
    }
}
