package ch.hslu.cobau.minij.asm;

import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.expression.VariableAccess;
import ch.hslu.cobau.minij.ast.statement.*;

import java.util.Map;

public class StatementGenerator extends BaseAstVisitor {

    // generated assembly code
    private final StringBuilder code = new StringBuilder();

    private final boolean isMain;

    // mapping from variables to stack position
    private final Map<String, Integer> localsMap;

    public StatementGenerator(Map<String, Integer> localsMap) {
        this.localsMap = localsMap;
        this.isMain = false;
    }

    public StatementGenerator(Map<String, Integer> localsMap, boolean isMain) {
        this.localsMap = localsMap;
        this.isMain = isMain;
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
    public void visit(Declaration declaration) {
        localsMap.put(declaration.getIdentifier(), localsMap.size() + 1);
        code.append("    mov qword [rbp-");
        code.append(8 * localsMap.get(declaration.getIdentifier()));
        code.append("], 0\n");
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        if (returnStatement.getExpression() != null) {
            ExpressionGenerator expressionGenerator = new ExpressionGenerator(localsMap);
            returnStatement.getExpression().accept(expressionGenerator);
            code.append(expressionGenerator.getCode());
        }
        if (!isMain) {
            code.append("    mov rsp, rbp\n");
            code.append("    pop rbp\n");
            code.append("    ret\n");
        }
    }

    @Override
    public void visit(AssignmentStatement assignment) {
        // get the value (right) of the assignment
        ExpressionGenerator expressionGenerator = new ExpressionGenerator(localsMap);
        assignment.getRight().accept(expressionGenerator);
        code.append(expressionGenerator.getCode());

        // assign the value (right) to the variable (left)
        VariableAccess variable = (VariableAccess) assignment.getLeft();
        if (localsMap.containsKey(variable.getIdentifier())) {
            code.append("    mov [rbp-");
            code.append(8 * localsMap.get(variable.getIdentifier()));
            code.append("], rax\n");
        } else {
            code.append("    mov [");
            code.append(variable.getIdentifier());
            code.append("], rax\n");
        }
    }

    @Override
    public void visit(CallStatement callStatement) {
        ExpressionGenerator expressionGenerator = new ExpressionGenerator(localsMap);
        callStatement.getCallExpression().accept(expressionGenerator);
        code.append(expressionGenerator.getCode());
    }

    @Override
    public void visit(IfStatement ifStatement) {
        // generate unique labels for branching
        String elseLabel = "else_" + System.identityHashCode(ifStatement);
        String endLabel = "end_if_" + System.identityHashCode(ifStatement);

        // evaluate the condition expression
        ExpressionGenerator expressionGenerator = new ExpressionGenerator(localsMap);
        ifStatement.getExpression().accept(expressionGenerator);
        code.append(expressionGenerator.getCode());

        // assuming the result of the expression is in register rax
        code.append("    cmp rax, 0\n");
        code.append("    je ").append(elseLabel).append("\n");

        // generate code for the "if" block
        for (Statement statement : ifStatement.getStatements()) {
            statement.accept(this);
        }
        code.append("    jmp ").append(endLabel).append("\n");

        // generate code for the "else" block
        code.append(elseLabel).append(":\n");
        if (ifStatement.getElseBlock() != null) {
            ifStatement.getElseBlock().accept(this);
        }

        // end label for the "if-else" statement
        code.append(endLabel).append(":\n");
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        String labelStart = "loop_" + System.identityHashCode(whileStatement);
        String labelEnd = "end_loop_" + System.identityHashCode(whileStatement);

        // start of loop
        code.append(labelStart).append(":").append("\n");

        // evaluate the condition expression
        ExpressionGenerator expressionGenerator = new ExpressionGenerator(localsMap);
        whileStatement.getExpression().accept(expressionGenerator);
        code.append(expressionGenerator.getCode());

        code.append("    cmp rax, 0\n");
        code.append("    je ").append(labelEnd).append("\n");

        // generate code for the "while" body
        for (Statement statement : whileStatement.getStatements()) {
            statement.accept(this);
        }

        // jump back to start
        code.append("    jmp ").append(labelStart).append("\n");

        // end of loop
        code.append(labelEnd).append(":").append("\n");
    }
}
