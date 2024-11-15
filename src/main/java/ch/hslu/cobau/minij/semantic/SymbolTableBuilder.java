package ch.hslu.cobau.minij.semantic;

import ch.hslu.cobau.minij.EnhancedConsoleErrorListener;
import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.entity.Function;
import ch.hslu.cobau.minij.ast.entity.Struct;
import ch.hslu.cobau.minij.ast.entity.Unit;
import ch.hslu.cobau.minij.ast.expression.VariableAccess;
import ch.hslu.cobau.minij.ast.type.IntegerType;
import ch.hslu.cobau.minij.ast.type.RecordType;
import ch.hslu.cobau.minij.ast.type.Type;
import ch.hslu.cobau.minij.ast.type.VoidType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Visits the entry point of the program and adds the built-in functions.
     *
     * @param program The program.
     */
    @Override
    public void visit(Unit program) {
        currentScope = symbolTable.addScope(program, currentScope);
        addBuiltInFunctions();
        super.visit(program);
    }

    /**
     * Adds the function with its parameters and return type to the symbol table.
     *
     * @param function The function element in the AST.
     */
    @Override
    public void visit(Function function) {
        currentScope = symbolTable.addScope(function, currentScope);
        super.visit(function);
        currentScope = currentScope.getParent();

        List<Type> paramTypes = function.getFormalParameters().stream().map(Declaration::getType).toList().reversed();
        FunctionSymbol symbol = new FunctionSymbol(function.getIdentifier(), function.getReturnType(), paramTypes);
        if (!symbolTable.addFunction(function.getIdentifier(), symbol)) {
            errorListener.semanticError("symbol '" + symbol.identifier() + "' already declared");
        }
    }

    /**
     * Adds the struct with its type and fields to the symbol table.
     *
     * @param struct The struct element in the AST.
     */
    @Override
    public void visit(Struct struct) {
        currentScope = symbolTable.addScope(struct, currentScope);
        super.visit(struct);
        currentScope = currentScope.getParent();

        Map<String, Type> fields = struct.getDeclarations().stream()
                .collect(Collectors.toMap(
                        Declaration::getIdentifier,
                        Declaration::getType,
                        (type1, type2) -> type1
                ));
        StructSymbol symbol = new StructSymbol(struct.getIdentifier(), new RecordType(struct.getIdentifier()), fields);
        if (!symbolTable.addStruct(struct.getIdentifier(), symbol)) {
            errorListener.semanticError("symbol '" + symbol.identifier() + "' already declared");
        }
    }

    /**
     * Adds the variable with its type to the symbol table in the current scope.
     *
     * @param declaration The declaration element in the AST.
     */
    @Override
    public void visit(Declaration declaration) {
        super.visit(declaration);
        VariableSymbol symbol = new VariableSymbol(declaration.getIdentifier(), declaration.getType());
        if (!currentScope.addSymbol(symbol)) {
            errorListener.semanticError("symbol '" + symbol.identifier() + "' already declared");
        }
    }

    /**
     * Adds a scope in the symbol table for the variable access element in the AST.
     *
     * @param variable The variable access element in the AST.
     */
    @Override
    public void visit(VariableAccess variable) {
        super.visit(variable);
        symbolTable.addScope(variable, currentScope);
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
     * Adds the built-in functions.
     */
    private void addBuiltInFunctions() {
        symbolTable.addFunction("writeInt", new FunctionSymbol("writeInt", new VoidType(), List.of(new IntegerType())));
        symbolTable.addFunction("readInt", new FunctionSymbol("readInt", new IntegerType(), List.of()));
        symbolTable.addFunction("writeChar", new FunctionSymbol("writeChar", new VoidType(), List.of(new IntegerType())));
        symbolTable.addFunction("readChar", new FunctionSymbol("readChar", new IntegerType(), List.of()));
    }
}
