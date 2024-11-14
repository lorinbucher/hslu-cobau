package ch.hslu.cobau.minij.semantic;

import ch.hslu.cobau.minij.EnhancedConsoleErrorListener;
import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.entity.Function;
import ch.hslu.cobau.minij.ast.expression.CallExpression;
import ch.hslu.cobau.minij.ast.expression.FieldAccess;
import ch.hslu.cobau.minij.ast.expression.VariableAccess;
import ch.hslu.cobau.minij.ast.type.IntegerType;
import ch.hslu.cobau.minij.ast.type.RecordType;

/**
 * Implements the semantic analysis for the MiniJ language.
 */
public class SemanticAnalyser extends BaseAstVisitor {

    private final EnhancedConsoleErrorListener errorListener;
    private final SymbolTable symbolTable;

    /**
     * Creates an instance of the semantic analyzer for the MiniJ language.
     *
     * @param errorListener The error listener.
     * @param symbolTable   The symbol table.
     */
    public SemanticAnalyser(EnhancedConsoleErrorListener errorListener, SymbolTable symbolTable) {
        this.errorListener = errorListener;
        this.symbolTable = symbolTable;
    }

    @Override
    public void visit(Function function) {
        super.visit(function);

        if (function.getIdentifier().equals("main")) {
            if (!function.getFormalParameters().isEmpty()) {
                errorListener.semanticError("main function must not have any parameters");
            }
            if (!function.getReturnType().getClass().equals(IntegerType.class)) {
                errorListener.semanticError("main function must have return type integer");
            }
        }
    }

    @Override
    public void visit(Declaration declaration) {
        super.visit(declaration);

        // NOTE (lorin): void is technically not defined as keyword in the language, but checking explicitly anyway
        if (declaration.getType().equals(new RecordType("void"))) {
            errorListener.semanticError("type of '" + declaration.getIdentifier() + "' must not be void");
        }

        SymbolTable.Scope scope = symbolTable.getScope(declaration);
        if (scope == null) {
            errorListener.semanticError("scope for '" + declaration.getIdentifier() + "' not found");
            return;
        }

        if (declaration.getType() instanceof RecordType type) {
            if (!scope.hasSymbol(type.getIdentifier(), SymbolEntity.STRUCT)) {
                errorListener.semanticError("struct type '" + type.getIdentifier() + "' not found");
            }
        }
    }

    @Override
    public void visit(CallExpression call) {
        super.visit(call);

        SymbolTable.Scope scope = symbolTable.getScope(call);
        if (scope == null) {
            errorListener.semanticError("scope for '" + call.getIdentifier() + "' not found");
            return;
        }

        Symbol symbol = scope.getSymbol(call.getIdentifier(), SymbolEntity.FUNCTION);
        if (symbol == null) {
            errorListener.semanticError("function '" + call.getIdentifier() + "' not found");
            return;
        }

        int paramCount = ((Function) symbol.astElement()).getFormalParameters().size();
        if (paramCount != call.getParameters().size()) {
            errorListener.semanticError("function '" + call.getIdentifier() + "' expects " + paramCount
                    + " parameters but " + call.getParameters().size() + " were given");
        }
    }

    @Override
    public void visit(VariableAccess variable) {
        super.visit(variable);

        SymbolTable.Scope scope = symbolTable.getScope(variable);
        if (scope == null) {
            errorListener.semanticError("scope for '" + variable.getIdentifier() + "' not found");
            return;
        }

        if (!scope.hasSymbol(variable.getIdentifier(), SymbolEntity.DECLARATION)) {
            errorListener.semanticError("variable '" + variable.getIdentifier() + "' not found");
        }
    }

    @Override
    public void visit(FieldAccess field) {
        super.visit(field);

        SymbolTable.Scope scope = symbolTable.getScope(field);
        if (scope == null) {
            errorListener.semanticError("scope for '" + field.getField() + "' not found");
            return;
        }

        VariableAccess variable = (VariableAccess) field.getBase();
        Symbol declaration = scope.getSymbol(variable.getIdentifier(), SymbolEntity.DECLARATION);
        if (declaration != null && declaration.type() instanceof RecordType type) {
            Symbol struct = scope.getSymbol(type.getIdentifier(), SymbolEntity.STRUCT);
            if (struct != null) {
                if (!symbolTable.getScope(struct.astElement()).hasSymbol(field.getField(), SymbolEntity.DECLARATION)) {
                    errorListener.semanticError("field '" + field.getField() + "' not found");
                }
            }
        } else {
            errorListener.semanticError("variable '" + variable.getIdentifier() + "' is not a struct");
        }
    }
}
