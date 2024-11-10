package ch.hslu.cobau.minij.semantic;

import ch.hslu.cobau.minij.EnhancedConsoleErrorListener;
import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.entity.Function;
import ch.hslu.cobau.minij.ast.type.IntegerType;
import ch.hslu.cobau.minij.ast.type.RecordType;

/**
 * Implements the semantic analysis for the MiniJ language.
 */
public class SemanticAnalyser extends BaseAstVisitor {

    private final EnhancedConsoleErrorListener errorListener;
    private final SymbolTable symbolTable;

    /**
     * Creates an instance of the semantic analyzer for the MiniJ language.
     *
     * @param errorListener The error listener.
     * @param symbolTable   The symbol table.
     */
    public SemanticAnalyser(EnhancedConsoleErrorListener errorListener, SymbolTable symbolTable) {
        this.errorListener = errorListener;
        this.symbolTable = symbolTable;
    }

    @Override
    public void visit(Function function) {
        super.visit(function);

        if (function.getIdentifier().equals("main")) {
            if (!function.getFormalParameters().isEmpty()) {
                errorListener.semanticError("main: function must not have any parameters");
            }
            if (!function.getReturnType().getClass().equals(IntegerType.class)) {
                errorListener.semanticError("main: function must have return type integer");
            }
        }
    }

    @Override
    public void visit(Declaration declaration) {
        declaration.visitChildren(this);

        // NOTE (lorin): void is technically not defined as keyword in the language, but checking explicitly anyway
        if (declaration.getType().equals(new RecordType("void"))) {
            errorListener.semanticError("declaration: '" + declaration.getIdentifier() + "' must not be of type void");
        }
    }
}
