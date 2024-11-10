package ch.hslu.cobau.minij.semantic;

import ch.hslu.cobau.minij.ast.type.Type;

import java.util.Objects;

/**
 * Represents a symbol in the symbol table of the MiniJ language.
 */
public record Symbol(String identifier, SymbolEntity entity, Type type) {

    /**
     * A symbol is equal if the identifier is the same.
     *
     * @param o Another object to compare to.
     * @return True if the other object is equal, false if not.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Symbol symbol)) return false;

        return Objects.equals(identifier, symbol.identifier);
    }

    /**
     * Calculates the hashcode only based on the identifier.
     *
     * @return The hashcode.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
