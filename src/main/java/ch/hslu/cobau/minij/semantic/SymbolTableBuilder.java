package ch.hslu.cobau.minij.semantic;

import ch.hslu.cobau.minij.EnhancedConsoleErrorListener;
import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.entity.Function;
import ch.hslu.cobau.minij.ast.entity.Struct;
import ch.hslu.cobau.minij.ast.entity.Unit;
import ch.hslu.cobau.minij.ast.type.VoidType;

/**
 * Builds the symbol table for the MiniJ language.
 */
public class SymbolTableBuilder extends BaseAstVisitor {

    private final EnhancedConsoleErrorListener errorListener;
    private final SymbolTable symbolTable = new SymbolTable();
    private SymbolTable.Scope currentScope = null;

    /**
     * Creates an instance of the symbol table builder for the MiniJ language.
     *
     * @param errorListener The error listener.
     */
    public SymbolTableBuilder(EnhancedConsoleErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    @Override
    public void visit(Unit program) {
        currentScope = symbolTable.addScope(program, currentScope);
        super.visit(program);
    }

    @Override
    public void visit(Declaration declaration) {
        Symbol symbol = new Symbol(declaration.getIdentifier(), SymbolEntity.DECLARATION, declaration.getType());
        addSymbol(symbol);
        currentScope = symbolTable.addScope(declaration, currentScope);
        super.visit(declaration);
        currentScope = currentScope.getParent();
    }

    @Override
    public void visit(Function function) {
        Symbol symbol = new Symbol(function.getIdentifier(), SymbolEntity.FUNCTION, function.getReturnType());
        addSymbol(symbol);
        currentScope = symbolTable.addScope(function, currentScope);
        super.visit(function);
        currentScope = currentScope.getParent();
    }

    @Override
    public void visit(Struct struct) {
        Symbol symbol = new Symbol(struct.getIdentifier(), SymbolEntity.STRUCT, new VoidType());
        addSymbol(symbol);
        currentScope = symbolTable.addScope(struct, currentScope);
        super.visit(struct);
        currentScope = currentScope.getParent();
    }

    /**
     * Returns the symbol table.
     *
     * @return The symbol table.
     */
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Adds a symbol to the current scope of the symbol table.
     *
     * @param symbol The symbol to add.
     */
    private void addSymbol(Symbol symbol) {
        if (!currentScope.addSymbol(symbol)) {
            errorListener.semanticError("declaration: symbol '" + symbol.identifier() + "' already declared");
        }
    }
}
