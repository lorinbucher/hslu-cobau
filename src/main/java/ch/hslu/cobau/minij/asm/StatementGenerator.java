package ch.hslu.cobau.minij.asm;

import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.statement.CallStatement;

public class StatementGenerator extends BaseAstVisitor {

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
    public void visit(CallStatement callStatement) {
        ExpressionGenerator expressionGenerator = new ExpressionGenerator();
        callStatement.getCallExpression().accept(expressionGenerator);
        code.append(expressionGenerator.getCode());
    }
}
