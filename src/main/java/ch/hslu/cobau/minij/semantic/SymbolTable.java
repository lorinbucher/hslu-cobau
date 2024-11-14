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
        private final Map<String, VariableSymbol> symbols = new HashMap<>();

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
        public boolean addSymbol(VariableSymbol symbol) {
            return symbols.putIfAbsent(symbol.identifier(), symbol) == null;
        }

        /**
         * Returns the symbol if this or any parent scope contains the symbol.
         *
         * @param identifier The identifier of the symbol.
         * @return The symbol if this or any parent scope contains the symbol, null if not.
         */
        public VariableSymbol getSymbol(String identifier) {
            Scope currentScope = this;
            do {
                VariableSymbol symbol = currentScope.symbols.get(identifier);
                if (symbol != null) {
                    return symbol;
                }
                currentScope = currentScope.parent;
            } while (currentScope != null);
            return null;
        }

        /**
         * Returns true if this or any parent scope contains the symbol.
         *
         * @param identifier The identifier of the symbol.
         * @return True if this or any parent scope contains the symbol, false if not.
         */
        public boolean hasSymbol(String identifier) {
            return getSymbol(identifier) != null;
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

    Map<AstElement, Scope> scopes = new HashMap<>();
    Map<String, FunctionSymbol> functions = new HashMap<>();
    Map<String, StructSymbol> structs = new HashMap<>();

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

    /**
     * Adds a function symbol to the symbol table.
     *
     * @param identifier The identifier of the function symbol.
     * @param function   The function symbol.
     * @return False if the function symbol already exists, true if not.
     */
    public boolean addFunction(String identifier, FunctionSymbol function) {
        return functions.putIfAbsent(identifier, function) == null;
    }

    /**
     * Returns the function symbol for a given identifier.
     *
     * @param identifier The identifier of the function symbol.
     * @return The function symbol if it exists or null if not.
     */
    public FunctionSymbol getFunction(String identifier) {
        return functions.get(identifier);
    }

    /**
     * Returns true if the function symbol exists.
     *
     * @param identifier The identifier of the function symbol.
     * @return True if the function symbol exists, false if not.
     */
    public boolean hasFunction(String identifier) {
        return getFunction(identifier) != null;
    }

    /**
     * Adds a struct symbol to the symbol table.
     *
     * @param identifier The identifier of the struct symbol.
     * @param struct     The struct symbol.
     * @return False if the struct symbol already exists, true if not.
     */
    public boolean addStruct(String identifier, StructSymbol struct) {
        return structs.putIfAbsent(identifier, struct) == null;
    }

    /**
     * Returns the struct symbol for a given identifier.
     *
     * @param identifier The identifier of the struct symbol.
     * @return The struct symbol if it exists or null if not.
     */
    public StructSymbol getStruct(String identifier) {
        return structs.get(identifier);
    }

    /**
     * Returns true if the struct symbol exists.
     *
     * @param identifier The identifier of the struct symbol.
     * @return True if the struct symbol exists, false if not.
     */
    public boolean hasStruct(String identifier) {
        return getStruct(identifier) != null;
    }
}
