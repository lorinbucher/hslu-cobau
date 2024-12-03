package ch.hslu.cobau.minij.asm;

import ch.hslu.cobau.minij.EnhancedConsoleErrorListener;
import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.constants.*;
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
public class GeneratedAsm extends BaseAstVisitor {
    private final EnhancedConsoleErrorListener errorListener;
    private final SymbolTable symbolTable;
    private final Stack<Type> returnStack = new Stack<>();
    private final Stack<Type> typeStack = new Stack<>();
    private final PrintStream ps = new PrintStream(System.out);

    public GeneratedAsm(EnhancedConsoleErrorListener errorListener, SymbolTable symbolTable) {
        this.errorListener = errorListener;
        this.symbolTable = symbolTable;

        // Initial assembly code setup
        ps.println("DEFAULT REL");
        ps.println("section .data");
        ps.println("section .text");
        ps.println("extern writeInt");
        ps.println("extern writeChar");
        ps.println("extern _exit");
        ps.println("global _start");
        ps.println("extern readInt");
        ps.println("extern readChar");
        ps.println("_start:");
    }

    @Override
    public void visit(Function function) {
        // Generate function prologue
        ps.println(function.getIdentifier() + ":");
        ps.println("    push    rbp");
        ps.println("    mov     rbp, rsp");

        // Reserve space for local variables (if needed)
        int localVarSpace = function.getFormalParameters().size() * 8; // 8 bytes per parameter
        if (localVarSpace > 0) {
            ps.println("    sub     rsp, " + localVarSpace);
        }

        // Handle formal parameters
        List<Declaration> formalParameters = function.getFormalParameters();
        int stackOffset = 16; // Offset for first parameter on stack (after return address and old rbp)
        for (int i = 0; i < formalParameters.size(); i++) {
            Declaration param = formalParameters.get(i);


            stackOffset += 8;
        }
        ps.println("    mov     rax, [rbp+" + stackOffset + "]");
        ps.println("    mov     [rbp-" + (formalParameters.size() + 1) * 8 + "], rax");


        // Visit function body (statements)
        function.visitChildren(this);

        // Ensure all return statements match the return type
        while (!returnStack.isEmpty()) {
            Type returnType = returnStack.pop();
            if (!returnType.equals(function.getReturnType())) {
                errorListener.semanticError("Mismatched return type in function '" + function.getIdentifier() + "'");
            }
        }
        if (function.getIdentifier().equals("main")) {
            ps.println("ret");
            ps.println("call _exit");
        }
        // Function epilogue -> seperate return statement
        //ps.println("    mov     rsp, rbp");
        //ps.println("    pop     rbp");
        //ps.println("    ret");
    }


    @Override
    public void visit(ReturnStatement returnStatement) {
        super.visit(returnStatement);

        // If there's an expression, generate code for it
        if (returnStatement.getExpression() != null) {
            returnStatement.getExpression().accept(this);
            Type returnType = typeStack.pop();
            ps.println("    ; Place return value in rax");
            ps.println("    mov     rax, [rsp]");
            returnStack.push(returnType);
        }

        // Generate return instruction

        ps.println("    mov     rsp, rbp");
        ps.println("    pop     rbp");
        ps.println("    ret");


    }

    @Override
    public void visit(AssignmentStatement assignment) {
        super.visit(assignment);

        // Generate code for right-hand side (expression)
        assignment.getRight().accept(this);

        // Generate code for left-hand side (variable)
        assignment.getLeft().accept(this);

        // Generate assignment instruction
        ps.println("    ; Perform assignment");
        ps.println("    pop     rax");
        ps.println("    mov     [rsp], rax");
    }

    @Override
    public void visit(CallStatement callStatement) {
        super.visit(callStatement);

        // Generate code for function arguments
        for (Expression argument : callStatement.getCallExpression().getParameters()) {
            argument.accept(this);
        }

        // Call the function
        ps.println("    ; Call function: " + callStatement.getCallExpression().getIdentifier());
        ps.println("    call    " +  callStatement.getCallExpression().getIdentifier());
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        super.visit(binaryExpression);

        // Generate code for both operands
        binaryExpression.getLeft().accept(this);
        binaryExpression.getRight().accept(this);

        // Pop operands into registers and perform operation
        ps.println("    pop     rbx"); // Right operand
        ps.println("    pop     rax"); // Left operand

        switch (binaryExpression.getBinaryOperator()) {
            case PLUS:
                ps.println("    add     rax, rbx");
                break;
            case MINUS:
                ps.println("    sub     rax, rbx");
                break;
            case TIMES:
                ps.println("    imul    rax, rbx");
                break;
            case DIV:
                ps.println("    xor     rdx, rdx");
                ps.println("    idiv    rbx");
                break;
            case MOD:
                ps.println("    xor     rdx, rdx");
                ps.println("    idiv    rbx");
                ps.println("    mov     rax, rdx");
                break;
            default:
                errorListener.semanticError("Unsupported binary operation");
                break;
        }

        // Push result back onto stack
        ps.println("    push    rax");
    }

    @Override
    public void visit(IfStatement ifStatement) {
        // Generate unique labels for branching
        String elseLabel = "else_" + System.identityHashCode(ifStatement);
        String endLabel = "end_if_" + System.identityHashCode(ifStatement);

        // Evaluate the condition expression
        ifStatement.getExpression().accept(this);

        // Assuming the result of the expression is in a register (e.g., rax), compare it; TODO !!
        ps.println("    cmp     rax, 0"); // Check if the condition is false





        ps.println("    je      " + elseLabel); // Jump to "else" block if false

        // Generate code for the "if" block
        for (Statement statement : ifStatement.getStatements()) {
            statement.accept(this); // Visit each statement in the "if" block
        }
        ps.println("    jmp     " + endLabel); // Skip over the "else" block

        // Generate code for the "else" block (if present)
        ps.println(elseLabel + ":");
        if (ifStatement.getElseBlock() != null) {
            ifStatement.getElseBlock().accept(this); // Visit the "else" block
        }

        // End label for the if-else construct
        ps.println(endLabel + ":");
    }


    @Override
    public void visit(WhileStatement whileStatement) {
        super.visit(whileStatement);

        String labelStart = "L" + System.nanoTime();
        String labelEnd = "L" + System.nanoTime();

        // Start of loop
        ps.println(labelStart + ":");

        // Evaluate condition
        whileStatement.getExpression().accept(this);
        ps.println("    pop     rax");
        ps.println("    cmp     rax, 0");
        ps.println("    je      " + labelEnd);

        // Loop body
        for (Statement statement : whileStatement.getStatements()) {
            statement.accept(this); // Visit each statement in the "if" block
        }

        // Jump back to start
        ps.println("    jmp     " + labelStart);

        // End of loop
        ps.println(labelEnd + ":");
    }

    @Override
    public void visit(VariableAccess variable) {
        super.visit(variable);

        ps.println("    ; Access variable: " + variable.getIdentifier());
        ps.println("    mov     rax, [rsp]");
        ps.println("    push    rax");
    }
}

