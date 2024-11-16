package ch.hslu.cobau.minij.semantic;

import ch.hslu.cobau.minij.EnhancedConsoleErrorListener;
import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.constants.*;
import ch.hslu.cobau.minij.ast.entity.*;
import ch.hslu.cobau.minij.ast.expression.*;
import ch.hslu.cobau.minij.ast.statement.*;
import ch.hslu.cobau.minij.ast.type.*;

import java.util.List;
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
                errorListener.semanticError("struct type '" + type.getIdentifier() + "' was not found");
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
                errorListener.semanticError("type '" + right + "' cannot be assigned to type '" + left + "'");
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
        if (!(type instanceof InvalidType) && !(type instanceof BooleanType)) {
            errorListener.semanticError("if statement expects 'boolean' type but '" + type + "' was provided");
        }
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        super.visit(whileStatement);

        Type type = typeStack.pop();
        if (!(type instanceof InvalidType) && !(type instanceof BooleanType)) {
            errorListener.semanticError("while statement expects 'boolean' type but '" + type + "' was provided");
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        super.visit(unaryExpression);

        if (typeStack.peek() instanceof InvalidType) {
            return;
        }

        Type type = typeStack.pop();
        if (unaryExpression.getUnaryOperator() == UnaryOperator.NOT) {
            operatorTypeCheck(UnaryOperator.NOT.name(), type, type, List.of(BooleanType.class));
        } else {
            operatorTypeCheck(unaryExpression.getUnaryOperator().name(), type, type, List.of(IntegerType.class));
        }
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        super.visit(binaryExpression);

        Type right = typeStack.pop();
        Type left = typeStack.pop();
        if (right instanceof InvalidType || left instanceof InvalidType) {
            typeStack.push(new InvalidType());
            return;
        }

        if (right.equals(left)) {
            BinaryOperator binaryOp = binaryExpression.getBinaryOperator();
            switch (binaryOp) {
                case PLUS:
                    operatorTypeCheck(binaryOp.name(), left, left, List.of(IntegerType.class, StringType.class));
                    break;
                case MINUS, TIMES, DIV, MOD:
                    operatorTypeCheck(binaryOp.name(), left, left, List.of(IntegerType.class));
                    break;
                case EQUAL, UNEQUAL:
                    operatorTypeCheck(binaryOp.name(), left, new BooleanType(), List.of(
                            IntegerType.class, StringType.class, BooleanType.class));
                    break;
                case LESSER, LESSER_EQ, GREATER, GREATER_EQ:
                    operatorTypeCheck(binaryOp.name(), left, new BooleanType(), List.of(
                            IntegerType.class, StringType.class));
                    break;
                case AND, OR:
                    operatorTypeCheck(binaryOp.name(), left, left, List.of(BooleanType.class));
                    break;
            }
        } else {
            typeError("cannot perform binary operation on types '" + left + "' and '" + right + "'");
        }
    }

    @Override
    public void visit(CallExpression callExpression) {
        super.visit(callExpression);

        FunctionSymbol symbol = symbolTable.getFunction(callExpression.getIdentifier());
        if (symbol == null) {
            typeError("function '" + callExpression.getIdentifier() + "' was not found");
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
                    errorListener.semanticError("function '" + callExpression.getIdentifier()
                            + "' parameter expects type '" + symbol.paramTypes().get(i)
                            + "' but '" + type + "' was provided");
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
            typeError("scope for '" + variable.getIdentifier() + "' was not found");
            return;
        }

        VariableSymbol symbol = scope.getSymbol(variable.getIdentifier());
        if (symbol != null) {
            typeStack.push(symbol.type());
        } else {
            typeError("variable '" + variable.getIdentifier() + "' was not found");
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
            typeError("array access expects integer type but '" + accessType + "' was provided");
            return;
        }

        if (variableType instanceof ArrayType type) {
            typeStack.push(type.getType());
        } else {
            typeError("array type expected but '" + variableType + "' was provided");
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

        typeError("field '" + fieldAccess.getField() + "' was not found");
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

    /**
     * Checks the type for an operator.
     *
     * @param operator     The operator name.
     * @param type         The type of the expression.
     * @param result       The result of the operation.
     * @param allowedTypes The allowed types.
     */
    private void operatorTypeCheck(String operator, Type type, Type result, List<Class<?>> allowedTypes) {
        if (allowedTypes.contains(type.getClass())) {
            typeStack.push(result);
        } else {
            typeError("operator '" + operator + "' is not allowed for type '" + type + "'");
        }
    }

    /**
     * Reports a type error and suppresses later type errors by pushing an invalid type to the type stack.
     *
     * @param message The error message.
     */
    private void typeError(String message) {
        errorListener.semanticError(message);
        typeStack.push(new InvalidType());
    }
}
