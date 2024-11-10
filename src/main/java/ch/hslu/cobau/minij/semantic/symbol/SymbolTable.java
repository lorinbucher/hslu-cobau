package ch.hslu.cobau.minij.semantic.symbol;

import ch.hslu.cobau.minij.ast.AstElement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implements the symbol table for the MiniJ language.
 */
public class SymbolTable {

    /**
     * Implements the scope in the symbol table for the MiniJ language.
     */
    public static class Scope {

        private final Scope parent;
        private final Set<Symbol> symbols = new HashSet<>();

        /**
         * Creates a new scope with a parent scope.
         *
         * @param parent The parent scope (can be null).
         */
        public Scope(Scope parent) {
            this.parent = parent;
        }

        /**
         * Adds a symbol to this scope.
         *
         * @param symbol Symbol to add to scope (must not be null).
         */
        public void addSymbol(Symbol symbol) {
            if (!symbols.add(symbol)) {
                throw new IllegalStateException("declaration: symbol '" + symbol.identifier() + "' already declared");
            }
        }

        /**
         * Returns true if this or any parent scope contains the symbol.
         *
         * @param symbol Symbol to check.
         * @return True if this or any parent scope contains the symbol.
         */
        public boolean hasSymbol(Symbol symbol) {
            Scope currentScope = this;
            do {
                if (currentScope.symbols.contains(symbol)) {
                    return true;
                }
                currentScope = currentScope.parent;
            } while (currentScope != null);
            return false;
        }

        /**
         * Returns the parent scope of the current scope.
         *
         * @return The parent scope.
         */
        public Scope getParent() {
            return parent;
        }
    }

    // mapping of scope to an element of the AST
    Map<AstElement, Scope> scopes = new HashMap<>();

    /**
     * Adds a scope for a specific element of the AST with a given parent.
     *
     * @param element The element of the AST.
     * @param parent  The parent of the scope (can be null).
     * @return The scope of the element.
     */
    public Scope addScope(AstElement element, Scope parent) {
        Scope scope = new Scope(parent);
        scopes.put(element, scope);
        return scope;
    }

    /**
     * Returns the scope for a specific element of the AST.
     *
     * @param element The element of the AST.
     * @return The scope of the element.
     */
    public Scope getScope(AstElement element) {
        return scopes.get(element);
    }
}
