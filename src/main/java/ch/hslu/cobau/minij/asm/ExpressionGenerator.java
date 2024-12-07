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
    public void visit(BinaryExpression binaryExpression) {
        // generate code for both operands
        binaryExpression.getRight().accept(this);
        code.append("    mov rbx, rax\n");
        binaryExpression.getLeft().accept(this);

        // pop operands into registers and perform operation
//        code.append("    pop rbx\n"); // Right operand
//        code.append("    pop rax\n"); // Left operand

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
            default:
                break;
        }

        // push the result back onto stack
//        code.append("    push    rax");
    }

    @Override
    public void visit(CallExpression callExpression) {
        List<Expression> parameters = callExpression.getParameters();

        // save first 6 parameters in register
        for (int i = 0; i < parameters.size() && i < 6; i++) {
            parameters.get(i).accept(this);
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
            code.append("    push rax\n");
        }

        // call function
        code.append("    call ");
        code.append(callExpression.getIdentifier());
        code.append("\n");

        // clean up parameters in stack
        code.append("    pop rdi\n".repeat(Math.max(0, parameters.size() - 6)));
    }

    @Override
    public void visit(VariableAccess variable) {
        if (localsMap.containsKey(variable.getIdentifier())) {
            code.append("    mov rax, [rbp-");
            code.append(8 * localsMap.get(variable.getIdentifier()));
            code.append("]\n");
        } else {
            code.append("    mov rax, [");
            code.append(variable.getIdentifier());
            code.append("]\n");
        }
    }

    @Override
    public void visit(FalseConstant falseConstant) {
        falseConstant.visitChildren(this);
        code.append("    mov rax, 0\n");
    }

    @Override
    public void visit(IntegerConstant integerConstant) {
        integerConstant.visitChildren(this);
        code.append("    mov rax, ");
        code.append(integerConstant.getValue());
        code.append("\n");
    }

    @Override
    public void visit(TrueConstant trueConstant) {
        trueConstant.visitChildren(this);
        code.append("    mov rax, 1\n");
    }
}
