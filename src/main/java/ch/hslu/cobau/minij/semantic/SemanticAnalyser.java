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
    private final Stack<Type> returnStack = new Stack<>();
    private final Stack<Type> typeStack = new Stack<>();

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
            if (!(function.getReturnType() instanceof IntegerType)) {
                errorListener.semanticError("main function must have return type integer");
            }
        } else {
            if (returnStack.empty() && !(function.getReturnType() instanceof VoidType)) {
                errorListener.semanticError("function '" + function.getIdentifier() + "' must return a value");
            }
        }

        while (!returnStack.empty()) {
            if (!function.getReturnType().equals(returnStack.pop())) {
                errorListener.semanticError("function '" + function.getIdentifier() + "' return type mismatch");
                returnStack.clear();
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
        if (returnStatement.getExpression() != null) {
            returnStack.push(typeStack.pop());
        } else {
            returnStack.push(new VoidType());
        }
    }

    @Override
    public void visit(AssignmentStatement assignment) {
        super.visit(assignment);
        Type right = typeStack.pop();
        Type left = typeStack.pop();
        if (!(right instanceof InvalidType) && !(left instanceof InvalidType)) {
            if (!right.equals(left)) {
                errorListener.semanticError("type mismatch '" + left + "' -> '" + right + "'");
            }
        }
    }

    @Override
    public void visit(CallStatement callStatement) {
        super.visit(callStatement);
        typeStack.pop();
    }

    @Override
    public void visit(IfStatement ifStatement) {
        super.visit(ifStatement);

        Type type = typeStack.pop();
        if (!(type instanceof InvalidType)) {
            if (!(type instanceof BooleanType)) {
                errorListener.semanticError(new BooleanType() + " type required but " + type + " provided");
            }
        }
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        super.visit(whileStatement);

        Type type = typeStack.pop();
        if (!(type instanceof InvalidType)) {
            if (!(type instanceof BooleanType)) {
                errorListener.semanticError(new BooleanType() + " type required but " + type + " provided");
            }
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        super.visit(unaryExpression);

        Type type = typeStack.pop();
        if (!(type instanceof InvalidType)) {
            if (unaryExpression.getUnaryOperator() == UnaryOperator.NOT) {
                if (!(type instanceof BooleanType)) {
                    errorListener.semanticError(UnaryOperator.NOT + " requires boolean type");
                    type = new InvalidType();
                }
            } else {
                if (!(type instanceof IntegerType)) {
                    errorListener.semanticError(unaryExpression.getUnaryOperator() + " requires integer type");
                    type = new InvalidType();
                }
            }
        }
        typeStack.push(type);
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        super.visit(binaryExpression);

        Type right = typeStack.pop();
        Type left = typeStack.pop();
        if (!(right instanceof InvalidType) && !(left instanceof InvalidType)) {
            if (right.equals(left)) {
                BinaryOperator binaryOp = binaryExpression.getBinaryOperator();
                switch (binaryOp) {
                    case PLUS:
                        if (!(left instanceof IntegerType || left instanceof StringType)) {
                            errorListener.semanticError(binaryOp + " requires integer or string type");
                            typeStack.push(new InvalidType());
                        } else {
                            typeStack.push(left);
                        }
                        break;
                    case MINUS, TIMES, DIV, MOD:
                        if (!(left instanceof IntegerType)) {
                            errorListener.semanticError(binaryOp + " requires integer type");
                            typeStack.push(new InvalidType());
                        } else {
                            typeStack.push(left);
                        }
                        break;
                    case EQUAL, UNEQUAL:
                        if (!(left instanceof IntegerType || left instanceof StringType || left instanceof BooleanType)) {
                            errorListener.semanticError(binaryOp + " requires integer, string or boolean type");
                            typeStack.push(new InvalidType());
                        } else {
                            typeStack.push(new BooleanType());
                        }
                        break;
                    case LESSER, LESSER_EQ, GREATER, GREATER_EQ:
                        if (!(left instanceof IntegerType || left instanceof StringType)) {
                            errorListener.semanticError(binaryOp + " requires integer or string type");
                            typeStack.push(new InvalidType());
                        } else {
                            typeStack.push(new BooleanType());
                        }
                        break;
                    case AND, OR:
                        if (!(left instanceof BooleanType)) {
                            errorListener.semanticError(binaryOp + " requires boolean type");
                            typeStack.push(new InvalidType());
                        } else {
                            typeStack.push(left);
                        }
                        break;
                }
            } else {
                errorListener.semanticError("type mismatch '" + left + "' -> '" + right + "'");
                typeStack.push(new InvalidType());
            }
        } else {
            typeStack.push(new InvalidType());
        }
    }

    @Override
    public void visit(CallExpression callExpression) {
        super.visit(callExpression);

        FunctionSymbol symbol = symbolTable.getFunction(callExpression.getIdentifier());
        if (symbol == null) {
            errorListener.semanticError("function '" + callExpression.getIdentifier() + "' not found");
            typeStack.push(new InvalidType());
            return;
        }

        if (symbol.paramTypes().size() != callExpression.getParameters().size()) {
            errorListener.semanticError("function '" + callExpression.getIdentifier() + "' expects "
                    + symbol.paramTypes().size() + " parameters but "
                    + callExpression.getParameters().size() + " were given");
        }

        for (int i = 0; i < callExpression.getParameters().size(); i++) {
            Type type = typeStack.pop();
            if (!(type instanceof InvalidType) && symbol.paramTypes().size() > i) {
                if (!type.equals(symbol.paramTypes().get(i))) {
                    errorListener.semanticError("function parameter type mismatch");
                }
            }
        }

        typeStack.push(symbol.returnType());
    }

    @Override
    public void visit(VariableAccess variable) {
        super.visit(variable);

        SymbolTable.Scope scope = symbolTable.getScope(variable);
        if (scope == null) {
            errorListener.semanticError("scope for '" + variable.getIdentifier() + "' not found");
            typeStack.push(new InvalidType());
            return;
        }

        VariableSymbol symbol = scope.getSymbol(variable.getIdentifier());
        if (symbol != null) {
            typeStack.push(symbol.type());
        } else {
            errorListener.semanticError("variable '" + variable.getIdentifier() + "' not found");
            typeStack.push(new InvalidType());
        }
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        super.visit(arrayAccess);

        Type accessType = typeStack.pop();
        Type variableType = typeStack.pop();
        if (accessType instanceof InvalidType || variableType instanceof InvalidType) {
            typeStack.push(new InvalidType());
            return;
        }

        if (!(accessType instanceof IntegerType)) {
            errorListener.semanticError("array access requires integer type");
            typeStack.push(new InvalidType());
            return;
        }

        if (variableType instanceof ArrayType type) {
            typeStack.push(type.getType());
        } else {
            errorListener.semanticError("variable is not a an array type");
            typeStack.push(new InvalidType());
        }
    }

    @Override
    public void visit(FieldAccess fieldAccess) {
        super.visit(fieldAccess);

        if (typeStack.peek() instanceof InvalidType) {
            return;
        }

        if (typeStack.pop() instanceof RecordType type) {
            StructSymbol symbol = symbolTable.getStruct(type.getIdentifier());
            if (symbol != null && symbol.fields().containsKey(fieldAccess.getField())) {
                typeStack.push(symbol.fields().get(fieldAccess.getField()));
                return;
            }
        }

        errorListener.semanticError("field '" + fieldAccess.getField() + "' not found");
        typeStack.push(new InvalidType());
    }

    @Override
    public void visit(FalseConstant falseConstant) {
        super.visit(falseConstant);
        typeStack.push(new BooleanType());
    }

    @Override
    public void visit(IntegerConstant integerConstant) {
        super.visit(integerConstant);
        typeStack.push(new IntegerType());
    }

    @Override
    public void visit(StringConstant stringConstant) {
        super.visit(stringConstant);
        typeStack.push(new StringType());
    }

    @Override
    public void visit(TrueConstant trueConstant) {
        super.visit(trueConstant);
        typeStack.push(new BooleanType());
    }
}
