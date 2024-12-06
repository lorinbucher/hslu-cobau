package ch.hslu.cobau.minij.asm;

import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.constants.FalseConstant;
import ch.hslu.cobau.minij.ast.constants.IntegerConstant;
import ch.hslu.cobau.minij.ast.constants.TrueConstant;
import ch.hslu.cobau.minij.ast.expression.CallExpression;
import ch.hslu.cobau.minij.ast.expression.Expression;

import java.util.List;

public class ExpressionGenerator extends BaseAstVisitor {

    private final static String[] PARAMETER_REGISTERS = new String[]{"rdi", "rsi", "rdx", "rcx", "r8", "r9"};

    // generated assembly code
    private final StringBuilder code = new StringBuilder();

    /**
     * Returns the generated assembly code.
     *
     * @return Generated assembly code.
     */
    public String getCode() {
        return code.toString();
    }

    @Override
    public void visit(CallExpression callExpression) {
        List<Expression> expressions = callExpression.getParameters();

        // save first 6 parameters in register
        for (int i = 0; i < expressions.size() && i < 6; i++) {
            code.append("    mov ").append(PARAMETER_REGISTERS[i]).append(", ");
            expressions.get(i).accept(this);
            code.append("\n");
        }

        // save additional parameters in stack in reverse order
        for (int i = expressions.size() - 1; i >= 6; i--) {
            code.append("    mov rax, ");
            expressions.get(i).accept(this);
            code.append("\n");
            code.append("    push rax\n");
        }

        // call function
        code.append("    call ").append(callExpression.getIdentifier()).append("\n");

        // clean up parameters in stack
        code.append("    pop rdi\n".repeat(Math.max(0, expressions.size() - 6)));
    }

    @Override
    public void visit(FalseConstant falseConstant) {
        falseConstant.visitChildren(this);
        code.append("0");
    }

    @Override
    public void visit(IntegerConstant integerConstant) {
        integerConstant.visitChildren(this);
        code.append(integerConstant.getValue());
    }

    @Override
    public void visit(TrueConstant trueConstant) {
        trueConstant.visitChildren(this);
        code.append("1");
    }
}
