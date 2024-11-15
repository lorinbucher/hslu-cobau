package ch.hslu.cobau.minij.semantic;

import ch.hslu.cobau.minij.ast.type.Type;

import java.util.Objects;

/**
 * Represents a variable symbol in the symbol table of the MiniJ language.
 *
 * @param identifier The identifier of the variable symbol.
 * @param type       The type of the variable symbol.
 */
public record VariableSymbol(String identifier, Type type) {

    /**
     * A variable symbol is equal if the identifier is the same.
     *
     * @param o Another object to compare to.
     * @return True if the other object is equal, false if not.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariableSymbol symbol)) return false;

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
