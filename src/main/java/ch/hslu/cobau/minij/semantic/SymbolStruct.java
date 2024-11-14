package ch.hslu.cobau.minij.semantic;

import ch.hslu.cobau.minij.ast.type.RecordType;
import ch.hslu.cobau.minij.ast.type.Type;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a struct symbol in the symbol table of the MiniJ language.
 *
 * @param identifier The identifier of the struct symbol.
 * @param type       The type of the struct symbol.
 * @param fields     The fields of the struct symbol.
 */
public record SymbolStruct(String identifier, RecordType type, Map<String, Type> fields) {

    /**
     * A struct symbol is equal if the identifier is the same.
     *
     * @param o Another object to compare to.
     * @return True if the other object is equal, false if not.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SymbolStruct symbol)) return false;

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
