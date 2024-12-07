package ch.hslu.cobau.minij.asm;

import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.constants.FalseConstant;
import ch.hslu.cobau.minij.ast.constants.IntegerConstant;
import ch.hslu.cobau.minij.ast.constants.TrueConstant;
import ch.hslu.cobau.minij.ast.expression.*;

import java.util.List;
import java.util.Map;

/**
 * Implements the Assembly code generation for the memory expressions of the MiniJ language.
 */
public class ExpressionMemoryGenerator extends BaseAstVisitor {

    // generated assembly code
    private final StringBuilder code = new StringBuilder();

    // mapping from variables to stack position
    private final Map<String, Integer> localsMap;

    public ExpressionMemoryGenerator(Map<String, Integer> localsMap) {
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
    public void visit(VariableAccess variable) {
        if (localsMap.containsKey(variable.getIdentifier())) {
            code.append("    lea rax, [rbp-");
            code.append(8 * localsMap.get(variable.getIdentifier()));
            code.append("]\n");
            code.append("    push rax\n");
        } else {
            code.append("    lea rax, [");
            code.append(variable.getIdentifier());
            code.append("]\n");
            code.append("    push rax\n");
        }
    }
}
