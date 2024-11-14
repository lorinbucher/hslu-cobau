package ch.hslu.cobau.minij.semantic;

import ch.hslu.cobau.minij.ast.type.Type;

import java.util.List;
import java.util.Objects;

/**
 * Represents a function symbol in the symbol table of the MiniJ language.
 *
 * @param identifier The identifier of the function symbol.
 * @param returnType The return type of the function symbol.
 * @param paramTypes The parameter types of the function symbol.
 */
public record SymbolFunction(String identifier, Type returnType, List<Type> paramTypes) {

    /**
     * A function symbol is equal if the identifier is the same.
     *
     * @param o Another object to compare to.
     * @return True if the other object is equal, false if not.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SymbolFunction symbol)) return false;

        return Objects.equals(identifier, symbol.identifier);
    }

    /**
     * Calculates the hashcode based on the identifier.
     *
     * @return The hashcode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
