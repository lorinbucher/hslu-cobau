package ch.hslu.cobau.minij.semantic;

import ch.hslu.cobau.minij.ast.AstElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements the symbol table for the MiniJ language.
 */
public class SymbolTable {

    /**
     * Implements the scope in the symbol table for the MiniJ language.
     */
    public static class Scope {

        private final Scope parent;
        private final Map<String, Symbol> symbols = new HashMap<>();

        private String getSymbol = "test";

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
         * @return False if the symbol already exists in the current scope, true if not.
         */
        public boolean addSymbol(Symbol symbol) {
            return symbols.putIfAbsent(symbol.identifier(), symbol) == null;
        }

        /**
         * Returns the symbol if this or any parent scope contains the symbol.
         *
         * @param identifier The identifier of the symbol.
         * @param entity     The entity of the symbol.
         * @return The symbol if this or any parent scope contains the symbol, null if not.
         */
        public Symbol getSymbol(String identifier, SymbolEntity entity) {
            Scope currentScope = this;
            do {
                if (currentScope.symbols.containsKey(identifier)) {
                    Symbol symbol = currentScope.symbols.get(identifier);
                    if (entity.equals(symbol.entity())) {
                        return symbol;
                    }
                }
                currentScope = currentScope.parent;
            } while (currentScope != null);
            return null;
        }

        /**
         * Returns true if this or any parent scope contains the symbol.
         *
         * @param identifier The identifier of the symbol.
         * @param entity     The entity of the symbol.
         * @return True if this or any parent scope contains the symbol, false if not.
         */
        public boolean hasSymbol(String identifier, SymbolEntity entity) {
            return getSymbol(identifier, entity) != null;
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
