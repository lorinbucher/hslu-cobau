package ch.hslu.cobau.minij.asm;

import ch.hslu.cobau.minij.EnhancedConsoleErrorListener;
import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.constants.FalseConstant;
import ch.hslu.cobau.minij.ast.constants.IntegerConstant;
import ch.hslu.cobau.minij.ast.constants.StringConstant;
import ch.hslu.cobau.minij.ast.constants.TrueConstant;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.entity.Function;
import ch.hslu.cobau.minij.ast.expression.*;
import ch.hslu.cobau.minij.ast.statement.*;
import ch.hslu.cobau.minij.ast.type.*;
import ch.hslu.cobau.minij.semantic.*;


import java.io.PrintStream;
import java.util.List;
import java.util.Stack;

/**
 * Implements the asm code generation for the MiniJ language.
 */

public class AsmGenerator extends BaseAstVisitor {


    private final EnhancedConsoleErrorListener errorListener;
    private final SymbolTable symbolTable;
    private final Stack<Type> returnStack = new Stack<>();
    private final Stack<Type> typeStack = new Stack<>();
    PrintStream ps =new PrintStream(System.out);
    /**
     * Creates an instance of the semantic analyzer for the MiniJ language.
     *
     * @param errorListener The error listener.
     * @param symbolTable   The symbol table.
     */
    public AsmGenerator(EnhancedConsoleErrorListener errorListener, SymbolTable symbolTable) {
        this.errorListener = errorListener;
        this.symbolTable = symbolTable;

       // things needed at top of output: TODO: Create an output Buffer instead
        ps.println ("DEFAULT REL");

        ps.println("section .data");


        ps.println("section .text");
        ps.println ("extern writeInt");
        ps.println ("extern writeChar");
        ps.println ("extern _exit");
        ps.println("global _start");
        ps.println( "extern readInt");
        ps.println("extern readChar");

       // ps.println("section .code");
        ps.println("_start:");


    }

    @Override
    public void visit(Function function) {
        super.visit(function);
        // function prologue



        if (function.getIdentifier().equals("main")) {
            //what to do with main function, under consideration, that it is semantically correct
            ps.println("main:");
            ps.println("push    rbp");                      //Save the base pointer
            ps.println("mov     rbp, rsp");                 //Set up the stack frame


            ps.println(".return:");
            ps.println("mov rsp, rbp");
            ps.println("pop rbp");
            ps.println("ret");



            if (!(function.getReturnType() instanceof IntegerType)) { //not sure if this is needed
                // main has return type integer
                // when main is done, program should be done...
                ps.println("call _exit");




            }
        } else { //what to do with other functions

            ps.println(function.getIdentifier() +":");
            ps.println("push    rbp");                      //Save the base pointer
            ps.println("mov     rbp, rsp");                 //Set up the stack frame
            // safe registers?, parameter handling,
            if (returnStack.empty() && !(function.getReturnType() instanceof VoidType)) {
                errorListener.semanticError("function '" + function.getIdentifier() + "' must return a value");
            }
        }

        while (!returnStack.empty()) {
            if (!function.getReturnType().equals(returnStack.pop())) {
                errorListener.semanticError("function '" + function.getIdentifier() + "' return type mismatch");
                returnStack.clear();
            }
            // Return code to go back to where the program was before


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
            //create the return values and go back to initial state

            //put return value or a memory address into register and then return

            //returnStack.push(typeStack.pop());
        } else {
            //returnStack.push(new VoidType());
            ps.println(" .return:");
            ps.println(" mov rsp, rbp");
            ps.println("pop rbp");
            ps.println("ret");
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
        ps.println(";this is in the call Statement");

        System.out.println("call " + callStatement);
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
        String functionName = callExpression.getIdentifier();
        ps.println(";this is in the call Expression");
        ps.println("call " + functionName);
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
