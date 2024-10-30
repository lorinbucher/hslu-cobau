/**
 * Copyright (c) 2020-2024 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.expression;

import ch.hslu.cobau.minij.ast.AstVisitor;

import java.util.Objects;

public class ArrayAccess extends MemoryAccess {
    private final MemoryAccess base;
    private final Expression indexExpression;

    public ArrayAccess(MemoryAccess base, Expression indexExpression) {
        Objects.requireNonNull(base);
        Objects.requireNonNull(indexExpression);

        this.base = base;
        this.indexExpression = indexExpression;
    }

    public MemoryAccess getBase() {
        return base;
    }

    public Expression getIndexExpression() {
        return indexExpression;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void visitChildren(AstVisitor astVisitor) {
        base.accept(astVisitor);
        indexExpression.accept(astVisitor);
    }
}
