package ch.hslu.cobau.minij.asm;

import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.constants.FalseConstant;
import ch.hslu.cobau.minij.ast.constants.IntegerConstant;
import ch.hslu.cobau.minij.ast.constants.TrueConstant;
import ch.hslu.cobau.minij.ast.expression.*;

import java.util.List;
import java.util.Map;

public class ExpressionGenerator extends BaseAstVisitor {

    private final static String[] PARAMETER_REGISTERS = new String[]{"rdi", "rsi", "rdx", "rcx", "r8", "r9"};

    // generated assembly code
    private final StringBuilder code = new StringBuilder();

    // mapping from variables to stack position
    private final Map<String, Integer> localsMap;

    public ExpressionGenerator(Map<String, Integer> localsMap) {
        this.localsMap = localsMap;
    }

    /**
     * Returns the generated assembly code.
     *
     * @return Generated assembly code.
     */
    public String getCode() {
        return code.toString();
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        // generate code for the operand
        switch (unaryExpression.getUnaryOperator()) {
            case MINUS, NOT:
                unaryExpression.getExpression().accept(this);
                break;
            default:
                ExpressionMemoryGenerator expressionMemoryGenerator = new ExpressionMemoryGenerator(localsMap);
                unaryExpression.getExpression().accept(expressionMemoryGenerator);
                code.append(expressionMemoryGenerator.getCode());
                break;
        }

        // load operand from stack
        code.append("    pop rax\n");

        switch (unaryExpression.getUnaryOperator()) {
            case MINUS:
                code.append("    neg rax\n");
                break;
            case NOT:
                code.append("    xor rax, 1\n");
                break;
            case PRE_INCREMENT:
                code.append("    inc qword [rax]\n");
                code.append("    mov rax, [rax]\n");
                break;
            case PRE_DECREMENT:
                code.append("    dec qword [rax]\n");
                code.append("    mov rax, [rax]\n");
                break;
            case POST_INCREMENT:
                code.append("    mov rbx, [rax]\n");
                code.append("    inc qword [rax]\n");
                code.append("    mov rax, rbx\n");
                break;
            case POST_DECREMENT:
                code.append("    mov rbx, [rax]\n");
                code.append("    dec qword [rax]\n");
                code.append("    mov rax, rbx\n");
                break;
            default:
                break;
        }

        // push the result back onto stack
        code.append("    push rax\n");
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        BinaryOperator operator = binaryExpression.getBinaryOperator();
        if (operator != BinaryOperator.AND && operator != BinaryOperator.OR) {
            // generate code for both operands
            binaryExpression.getLeft().accept(this);
            binaryExpression.getRight().accept(this);

            // load operands from stack
            code.append("    pop rbx\n"); // right operand
            code.append("    pop rax\n"); // left operand
        }

        String falseLabel = ".false_" + binaryExpression.hashCode();
        String trueLabel = ".true_" + binaryExpression.hashCode();
        String endLabel = ".end_" + binaryExpression.hashCode();
        switch (binaryExpression.getBinaryOperator()) {
            case PLUS:
                code.append("    add rax, rbx\n");
                break;
            case MINUS:
                code.append("    sub rax, rbx\n");
                break;
            case TIMES:
                code.append("    imul rax, rbx\n");
                break;
            case DIV:
                code.append("    xor rdx, rdx\n");
                code.append("    idiv rbx\n");
                break;
            case MOD:
                code.append("    xor rdx, rdx\n");
                code.append("    idiv rbx\n");
                code.append("    mov rax, rdx\n");
                break;
            case EQUAL:
                code.append("    cmp rax, rbx\n");
                code.append("    sete dl\n");
                code.append("    movsx rax, dl\n");
                break;
            case UNEQUAL:
                code.append("    cmp rax, rbx\n");
                code.append("    setne dl\n");
                code.append("    movsx rax, dl\n");
                break;
            case LESSER:
                code.append("    cmp rax, rbx\n");
                code.append("    setl dl\n");
                code.append("    movsx rax, dl\n");
                break;
            case LESSER_EQ:
                code.append("    cmp rax, rbx\n");
                code.append("    setle dl\n");
                code.append("    movsx rax, dl\n");
                break;
            case GREATER:
                code.append("    cmp rax, rbx\n");
                code.append("    setg dl\n");
                code.append("    movsx rax, dl\n");
                break;
            case GREATER_EQ:
                code.append("    cmp rax, rbx\n");
                code.append("    setge dl\n");
                code.append("    movsx rax, dl\n");
                break;
            case AND:
                binaryExpression.getLeft().accept(this);
                code.append("    pop rax\n");

                code.append("    cmp rax, 0\n");
                code.append("    je ").append(falseLabel).append("\n");

                binaryExpression.getRight().accept(this);
                code.append("    pop rbx\n");

                code.append("    cmp rbx, 0\n");
                code.append("    je ").append(falseLabel).append("\n");
                code.append("    mov rax, 1\n");
                code.append("    jmp ").append(endLabel).append("\n");

                code.append(falseLabel).append(":\n");
                code.append("    mov rax, 0\n");
                code.append(endLabel).append(":\n");
                break;
            case OR:
                binaryExpression.getLeft().accept(this);
                code.append("    pop rax\n");

                code.append("    cmp rax, 0\n");
                code.append("    jne ").append(trueLabel).append("\n");

                binaryExpression.getRight().accept(this);
                code.append("    pop rbx\n");

                code.append("    cmp rbx, 0\n");
                code.append("    jne ").append(trueLabel).append("\n");
                code.append("    mov rax, 0\n");
                code.append("    jmp ").append(endLabel).append("\n");

                code.append(trueLabel).append(":\n");
                code.append("    mov rax, 1\n");
                code.append(endLabel).append(":\n");
                break;
            default:
                break;
        }

        // push the result back onto stack
        code.append("    push rax\n");
    }

    @Override
    public void visit(CallExpression callExpression) {
        List<Expression> parameters = callExpression.getParameters();

        // save first 6 parameters in register
        for (int i = 0; i < parameters.size() && i < 6; i++) {
            parameters.get(i).accept(this);
            code.append("    pop rax\n");
            code.append("    mov ");
            code.append(PARAMETER_REGISTERS[i]);
            code.append(", rax\n");
        }

        // add placeholder to stack if the number of parameters is odd
        if (parameters.size() % 2 != 0) {
            parameters.add(new IntegerConstant(0));
        }

        // save additional parameters in stack in reverse order
        for (int i = parameters.size() - 1; i >= 6; i--) {
            parameters.get(i).accept(this);
        }

        // call function
        code.append("    call ");
        code.append(callExpression.getIdentifier());
        code.append("\n");

        // clean up parameters in stack
        code.append("    pop rdi\n".repeat(Math.max(0, parameters.size() - 6)));

        // push return value to stack
        code.append("    push rax\n");
    }

    @Override
    public void visit(VariableAccess variable) {
        if (localsMap.containsKey(variable.getIdentifier())) {
            code.append("    push qword [rbp-");
            code.append(8 * localsMap.get(variable.getIdentifier()));
            code.append("]\n");
        } else {
            code.append("    push qword [");
            code.append(variable.getIdentifier());
            code.append("]\n");
        }
    }

    @Override
    public void visit(FalseConstant falseConstant) {
        falseConstant.visitChildren(this);
        code.append("    push 0\n");
    }

    @Override
    public void visit(IntegerConstant integerConstant) {
        integerConstant.visitChildren(this);
        code.append("    mov rax, ");
        code.append(integerConstant.getValue());
        code.append("\n");
        code.append("    push rax\n");
    }

    @Override
    public void visit(TrueConstant trueConstant) {
        trueConstant.visitChildren(this);
        code.append("    push 1\n");
    }
}
