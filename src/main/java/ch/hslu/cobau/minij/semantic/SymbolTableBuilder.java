package ch.hslu.cobau.minij.semantic;

import ch.hslu.cobau.minij.EnhancedConsoleErrorListener;
import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.constants.*;
import ch.hslu.cobau.minij.ast.entity.*;
import ch.hslu.cobau.minij.ast.expression.*;
import ch.hslu.cobau.minij.ast.statement.*;
import ch.hslu.cobau.minij.ast.type.*;

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

    @Override
    public void visit(Unit program) {
        currentScope = symbolTable.addScope(program, currentScope);
        addBuiltInFunctions();
        super.visit(program);
    }

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

    @Override
    public void visit(Declaration declaration) {
        VariableSymbol symbol = new VariableSymbol(declaration.getIdentifier(), declaration.getType());
        if (!currentScope.addSymbol(symbol)) {
            errorListener.semanticError("symbol '" + symbol.identifier() + "' already declared");
        }
        currentScope = symbolTable.addScope(declaration, currentScope);
        super.visit(declaration);
        currentScope = currentScope.getParent();
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        symbolTable.addScope(returnStatement, currentScope);
        super.visit(returnStatement);
    }

    @Override
    public void visit(AssignmentStatement assignment) {
        symbolTable.addScope(assignment, currentScope);
        super.visit(assignment);
    }

    @Override
    public void visit(DeclarationStatement declarationStatement) {
        symbolTable.addScope(declarationStatement, currentScope);
        super.visit(declarationStatement);
    }

    @Override
    public void visit(CallStatement callStatement) {
        symbolTable.addScope(callStatement, currentScope);
        super.visit(callStatement);
    }

    @Override
    public void visit(IfStatement ifStatement) {
        symbolTable.addScope(ifStatement, currentScope);
        super.visit(ifStatement);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        symbolTable.addScope(whileStatement, currentScope);
        super.visit(whileStatement);
    }

    @Override
    public void visit(Block block) {
        symbolTable.addScope(block, currentScope);
        super.visit(block);
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        symbolTable.addScope(unaryExpression, currentScope);
        super.visit(unaryExpression);
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        symbolTable.addScope(binaryExpression, currentScope);
        super.visit(binaryExpression);
    }

    @Override
    public void visit(CallExpression callExpression) {
        symbolTable.addScope(callExpression, currentScope);
        super.visit(callExpression);
    }

    @Override
    public void visit(VariableAccess variable) {
        symbolTable.addScope(variable, currentScope);
        super.visit(variable);
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        symbolTable.addScope(arrayAccess, currentScope);
        super.visit(arrayAccess);
    }

    @Override
    public void visit(FieldAccess fieldAccess) {
        symbolTable.addScope(fieldAccess, currentScope);
        super.visit(fieldAccess);
    }

    @Override
    public void visit(FalseConstant falseConstant) {
        symbolTable.addScope(falseConstant, currentScope);
        super.visit(falseConstant);
    }

    @Override
    public void visit(IntegerConstant integerConstant) {
        symbolTable.addScope(integerConstant, currentScope);
        super.visit(integerConstant);
    }

    @Override
    public void visit(StringConstant stringConstant) {
        symbolTable.addScope(stringConstant, currentScope);
        super.visit(stringConstant);
    }

    @Override
    public void visit(TrueConstant trueConstant) {
        symbolTable.addScope(trueConstant, currentScope);
        super.visit(trueConstant);
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
