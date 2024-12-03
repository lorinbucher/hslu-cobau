package ch.hslu.cobau.minij.asm;

import ch.hslu.cobau.minij.EnhancedConsoleErrorListener;
import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.constants.FalseConstant;
import ch.hslu.cobau.minij.ast.constants.IntegerConstant;
import ch.hslu.cobau.minij.ast.constants.StringConstant;
import ch.hslu.cobau.minij.ast.constants.TrueConstant;
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

public class AsmGenerator extends BaseAstVisitor {

    private final EnhancedConsoleErrorListener errorListener;
    private final SymbolTable symbolTable;
    private final Stack<Type> returnStack = new Stack<>();
    private final Stack<Type> typeStack = new Stack<>();
    private final PrintStream ps = new PrintStream(System.out);

    public AsmGenerator(EnhancedConsoleErrorListener errorListener, SymbolTable symbolTable) {
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
        //ps.println("    push    rbp");
        //ps.println("    mov     rbp, rsp");

        // Visit function body (statements)
        function.visitChildren(this);

        // Ensure all return statements match the return type

        if (function.getIdentifier().equals("main")) {
            function.visitChildren(this);
            ps.println("mov rax, 0");
            ps.println("ret");
            ps.println("call _exit");
        }
        // Function epilogue -> seperate return statement
        //ps.println("    mov     rsp, rbp");
        //ps.println("    pop     rbp");
        //ps.println("    ret");
    }

    @Override
    public void visit(CallStatement callStatement) {
        super.visit(callStatement);
        typeStack.pop();
        ps.println(";this is in the call Statement");

        System.out.println("call " + callStatement);
    }

    @Override
    public void visit(CallExpression callExpression) {
        super.visit(callExpression);

        FunctionSymbol symbol = symbolTable.getFunction(callExpression.getIdentifier());
        String functionName = callExpression.getIdentifier();
        ps.println(";this is in the call Expression");
        ps.println("call " + functionName);
        typeStack.push(symbol.returnType());
    }
}
