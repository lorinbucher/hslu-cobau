package ch.hslu.cobau.minij.asm;

import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.expression.VariableAccess;
import ch.hslu.cobau.minij.ast.statement.AssignmentStatement;
import ch.hslu.cobau.minij.ast.statement.CallStatement;
import ch.hslu.cobau.minij.ast.statement.ReturnStatement;
import ch.hslu.cobau.minij.ast.type.Type;

import java.util.Map;

public class StatementGenerator extends BaseAstVisitor {

    // generated assembly code
    private final StringBuilder code = new StringBuilder();

    // mapping from variables to stack position
    private final Map<String, Integer> localsMap;

    public StatementGenerator(Map<String, Integer> localsMap) {
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
    public void visit(Declaration declaration) {
        addLocal(declaration.getIdentifier());
        code.append("    mov qword [rbp-");
        code.append(8 * localsMap.get(declaration.getIdentifier()));
        code.append("], 0\n");
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
            code.append("], rcx\n");
        }
    }

    @Override
    public void visit(CallStatement callStatement) {
        ExpressionGenerator expressionGenerator = new ExpressionGenerator(localsMap);
        callStatement.getCallExpression().accept(expressionGenerator);
        code.append(expressionGenerator.getCode());
    }

    /**
     * Helper function to add a local variable to the stack.
     *
     * @param identifier Identifier of the variable.
     */
    private void addLocal(String identifier) {
        int position = localsMap.size() + 1;
        if (!localsMap.containsKey(identifier)) {
            localsMap.put(identifier, position);
        }
    }
}
