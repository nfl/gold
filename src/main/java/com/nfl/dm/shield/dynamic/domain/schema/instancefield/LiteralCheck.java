package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

interface LiteralCheck {
    boolean literalCheck(InstanceFieldType subArrayType);

    static LiteralCheck createBasicLiteral() {
        return subArrayType -> true;
    }

    static LiteralCheck createOtherLiteral() {
        return subArrayType -> false;
    }

    static LiteralCheck createListLiteral() {
        return subArrayType -> subArrayType.isLiteralType(InstanceFieldType.NONE);
    }
}


