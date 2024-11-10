package ch.hslu.cobau.minij.semantic.symbol;

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

    private final SymbolTable symbolTable = new SymbolTable();
    private SymbolTable.Scope currentScope = null;

    @Override
    public void visit(Unit program) {
        currentScope = symbolTable.addScope(program, currentScope);
        super.visit(program);
    }

    @Override
    public void visit(Declaration declaration) {
        Symbol symbol = new Symbol(declaration.getIdentifier(), SymbolEntity.DECLARATION, declaration.getType());
        currentScope.addSymbol(symbol);
        currentScope = symbolTable.addScope(declaration, currentScope);
        super.visit(declaration);
        currentScope = currentScope.getParent();
    }

    @Override
    public void visit(Function function) {
        Symbol symbol = new Symbol(function.getIdentifier(), SymbolEntity.FUNCTION, function.getReturnType());
        currentScope.addSymbol(symbol);
        currentScope = symbolTable.addScope(function, currentScope);
        super.visit(function);
        currentScope = currentScope.getParent();
    }

    @Override
    public void visit(Struct struct) {
        Symbol symbol = new Symbol(struct.getIdentifier(), SymbolEntity.STRUCT, new VoidType());
        currentScope.addSymbol(symbol);
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
}
