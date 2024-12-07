package ch.hslu.cobau.minij.asm;

import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.entity.Function;
import ch.hslu.cobau.minij.ast.entity.Unit;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Implements the Assembly code generation for the MiniJ language.
 */
public class ProgramGenerator extends BaseAstVisitor {

    // generated assembly code
    private String code;

    // temporary storage for generated assembly code fragments
    private final Stack<String> codeFragments = new Stack<>();

    /**
     * Returns the generated assembly code.
     *
     * @return Generated assembly code.
     */
    public String getCode() {
        return code;
    }

    @Override
    public void visit(Unit program) {
        program.visitChildren(this);

        code = """
                DEFAULT REL
                extern readChar
                extern readInt
                extern writeChar
                extern writeInt
                extern _exit
                global _start
                section .text
                """;

        StringBuilder fragments = new StringBuilder();
        while (!codeFragments.isEmpty()) {
            fragments.insert(0, codeFragments.pop());
        }

        code += fragments;
    }

    @Override
    public void visit(Function function) {
        String functionName = function.getIdentifier();
        String epilogue;
        if (functionName.equals("main")) {
            functionName = "_start";
            epilogue = """
                        ; exit program
                        mov  rdi, 0
                        call _exit
                    """;
        } else {
            epilogue = """
                        ; epilogue
                        mov rsp, rbp
                        pop rbp
                        ret
                    """;
        }

        Map<String, Integer> localsMap = new HashMap<>();
        StatementGenerator statementGenerator = new StatementGenerator(localsMap);
        function.getFormalParameters().forEach(parameter -> parameter.accept(statementGenerator));
        function.getStatements().forEach(statement -> statement.accept(statementGenerator));

        int stackSize = localsMap.size() * 8;
        stackSize += stackSize % 16; // align to 16 bytes
        String prologue = functionName + ":\n" +
                "    ; prologue\n" +
                "    push rbp\n" +
                "    mov  rbp, rsp\n" +
                "    sub  rsp, " + stackSize + "\n";

        codeFragments.push(prologue);
        codeFragments.push(statementGenerator.getCode());
        codeFragments.push(epilogue);
    }
}
