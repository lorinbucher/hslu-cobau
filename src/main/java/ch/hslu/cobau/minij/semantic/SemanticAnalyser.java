package ch.hslu.cobau.minij.semantic;

import ch.hslu.cobau.minij.EnhancedConsoleErrorListener;
import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.constants.*;
import ch.hslu.cobau.minij.ast.entity.*;
import ch.hslu.cobau.minij.ast.expression.*;
import ch.hslu.cobau.minij.ast.statement.*;
import ch.hslu.cobau.minij.ast.type.*;

import java.util.Stack;

/**
 * Implements the semantic analysis for the MiniJ language.
 */
public class SemanticAnalyser extends BaseAstVisitor {

    private final EnhancedConsoleErrorListener errorListener;
    private final SymbolTable symbolTable;
    private final Stack<Type> tyeStack = new Stack<>();

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
                errorListener.semanticError("main function must not have any parameters");
            }
            if (!function.getReturnType().getClass().equals(IntegerType.class)) {
                errorListener.semanticError("main function must have return type integer");
            }
        }

        // TODO (lorin): improve after type checking implementation
        for (Statement statement : function.getStatements()) {
            if (statement instanceof ReturnStatement returnStatement) {
                if (function.getReturnType() instanceof VoidType && returnStatement.getExpression() != null) {
                    errorListener.semanticError("function '" + function.getIdentifier() + "' must not return a value");
                }
                if (!(function.getReturnType() instanceof VoidType) && returnStatement.getExpression() == null) {
                    errorListener.semanticError("function '" + function.getIdentifier() + "' must return a value");
                }
            }
        }
    }

    @Override
    public void visit(Declaration declaration) {
        super.visit(declaration);

        // NOTE (lorin): void is technically not defined as keyword in the language, but checking explicitly anyway
        if (declaration.getType().equals(new RecordType("void"))) {
            errorListener.semanticError("type of '" + declaration.getIdentifier() + "' must not be void");
        }

        if (declaration.getType() instanceof RecordType type) {
            if (!symbolTable.hasStruct(type.getIdentifier())) {
                errorListener.semanticError("struct type '" + type.getIdentifier() + "' not found");
            }
        }
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        super.visit(returnStatement);
    }

    @Override
    public void visit(AssignmentStatement assignment) {
        super.visit(assignment);
        Type right = tyeStack.pop();
        Type left = tyeStack.pop();
        if (right.getClass() != InvalidType.class || left.getClass() != InvalidType.class) {
            if (right.getClass() == left.getClass()) {
                tyeStack.push(right);
                return;
            } else {
                errorListener.semanticError("type mismatch '" + left + "' -> '" + right + "'");
            }
        }
        tyeStack.push(new InvalidType());
    }

    @Override
    public void visit(DeclarationStatement declarationStatement) {
        super.visit(declarationStatement);
    }

    @Override
    public void visit(CallStatement callStatement) {
        super.visit(callStatement);
    }

    @Override
    public void visit(IfStatement ifStatement) {
        super.visit(ifStatement);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        super.visit(whileStatement);
    }

    @Override
    public void visit(Block block) {
        super.visit(block);
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        super.visit(unaryExpression);
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        super.visit(binaryExpression);
    }

    @Override
    public void visit(CallExpression callExpression) {
        super.visit(callExpression);

        FunctionSymbol symbol = symbolTable.getFunction(callExpression.getIdentifier());
        if (symbol == null) {
            errorListener.semanticError("function '" + callExpression.getIdentifier() + "' not found");
            tyeStack.push(new InvalidType());
            return;
        }

        if (symbol.paramTypes().size() != callExpression.getParameters().size()) {
            errorListener.semanticError("function '" + callExpression.getIdentifier() + "' expects "
                    + symbol.paramTypes().size() + " parameters but "
                    + callExpression.getParameters().size() + " were given");
        }

        tyeStack.push(symbol.returnType());
    }

    @Override
    public void visit(VariableAccess variable) {
        super.visit(variable);

        SymbolTable.Scope scope = symbolTable.getScope(variable);
        if (scope == null) {
            errorListener.semanticError("scope for '" + variable.getIdentifier() + "' not found");
            tyeStack.push(new InvalidType());
            return;
        }

        VariableSymbol symbol = scope.getSymbol(variable.getIdentifier());
        if (symbol != null) {
            tyeStack.push(symbol.type());
        } else {
            errorListener.semanticError("variable '" + variable.getIdentifier() + "' not found");
            tyeStack.push(new InvalidType());
        }
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        super.visit(arrayAccess);
    }

    @Override
    public void visit(FieldAccess fieldAccess) {
        super.visit(fieldAccess);

        SymbolTable.Scope scope = symbolTable.getScope(fieldAccess);
        if (scope == null) {
            errorListener.semanticError("scope for '" + fieldAccess.getField() + "' not found");
            tyeStack.push(new InvalidType());
            return;
        }

        VariableAccess variable = (VariableAccess) fieldAccess.getBase();
        VariableSymbol declaration = scope.getSymbol(variable.getIdentifier());
        if (declaration != null && declaration.type() instanceof RecordType type) {
            StructSymbol struct = symbolTable.getStruct(type.getIdentifier());
            if (struct != null && struct.fields().containsKey(fieldAccess.getField())) {
                tyeStack.push(struct.fields().get(fieldAccess.getField()));
                return;
            }
            errorListener.semanticError("field '" + fieldAccess.getField() + "' not found");
        } else {
            errorListener.semanticError("variable '" + variable.getIdentifier() + "' is not a struct");
        }
        tyeStack.push(new InvalidType());
    }

    @Override
    public void visit(FalseConstant falseConstant) {
        super.visit(falseConstant);
        tyeStack.push(new BooleanType());
    }

    @Override
    public void visit(IntegerConstant integerConstant) {
        super.visit(integerConstant);
        tyeStack.push(new IntegerType());
    }

    @Override
    public void visit(StringConstant stringConstant) {
        super.visit(stringConstant);
        tyeStack.push(new StringType());
    }

    @Override
    public void visit(TrueConstant trueConstant) {
        super.visit(trueConstant);
        tyeStack.push(new BooleanType());
    }
}
