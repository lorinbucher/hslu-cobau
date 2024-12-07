package ch.hslu.cobau.minij.asm;

import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.entity.Function;
import ch.hslu.cobau.minij.ast.entity.Unit;

import java.util.HashMap;
import java.util.Map;


/**
 * Implements the Assembly code generation for the MiniJ language.
 */
public class ProgramGenerator extends BaseAstVisitor {

    private final static String[] PARAMETER_REGISTERS = new String[]{"rdi", "rsi", "rdx", "rcx", "r8", "r9"};

    // generated assembly code
    private final StringBuilder code = new StringBuilder();

    // temporary storage for generated assembly code fragments
    private final StringBuilder codeFragments = new StringBuilder();

    // temporary storage for generated assembly code of global variables
    private final StringBuilder globalVariables = new StringBuilder();

    /**
     * Returns the generated assembly code.
     *
     * @return Generated assembly code.
     */
    public String getCode() {
        return code.toString();
    }

    @Override
    public void visit(Unit program) {
        program.visitChildren(this);

        code.append("""
                DEFAULT REL
                extern readChar
                extern readInt
                extern writeChar
                extern writeInt
                extern _exit
                global _start
                """);

        code.append("section .data\n");
        code.append("ALIGN 8\n");
        code.append(globalVariables);

        code.append("section .text\n");
        code.append(codeFragments);
    }

    @Override
    public void visit(Function function) {
        Map<String, Integer> localsMap = new HashMap<>();
        StatementGenerator statementGenerator;
        String functionName = function.getIdentifier();
        String epilogue;
        if (functionName.equals("main")) {
            statementGenerator = new StatementGenerator(localsMap, true);
            functionName = "_start";
            epilogue = """
                        ; exit program
                        mov  rdi, rax
                        call _exit
                    """;
        } else {
            statementGenerator = new StatementGenerator(localsMap);
            epilogue = """
                        ; epilogue
                        mov rax, 0
                        mov rsp, rbp
                        pop rbp
                        ret
                    """;
        }

        // save parameters as local variables
        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < function.getFormalParameters().size(); i++) {
            Declaration declaration = function.getFormalParameters().get(i);
            if (i < 6) {
                // save parameters from registers
                localsMap.put(declaration.getIdentifier(), localsMap.size() + 1);
                parameters.append("    mov [rbp-");
                parameters.append(8 * localsMap.get(declaration.getIdentifier()));
                parameters.append("], ");
                parameters.append(PARAMETER_REGISTERS[i]);
                parameters.append("\n");
            } else {
                // add variables already on the stack with positive offset
                localsMap.put(declaration.getIdentifier(), -(i + 2 - 6));
            }
        }

        // generate code for function body
        function.getStatements().forEach(statement -> statement.accept(statementGenerator));

        int stackSize = localsMap.size() * 8;
        stackSize += stackSize % 16; // align to 16 bytes
        String prologue = functionName + ":\n" +
                "    ; prologue\n" +
                "    push rbp\n" +
                "    mov  rbp, rsp\n" +
                "    sub  rsp, " + stackSize + "\n";

        codeFragments.append(prologue);
        codeFragments.append(parameters);
        codeFragments.append(statementGenerator.getCode());
        codeFragments.append(epilogue);
    }

    @Override
    public void visit(Declaration declaration) {
        globalVariables.append(declaration.getIdentifier());
        globalVariables.append(" dq 0\n");
    }
}
