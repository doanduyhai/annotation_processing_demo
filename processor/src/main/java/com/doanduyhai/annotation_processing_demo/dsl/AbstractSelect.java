package com.doanduyhai.annotation_processing_demo.dsl;

import java.util.StringJoiner;

public abstract class AbstractSelect<T extends AbstractWhere> {

    final protected StringBuilder query = new StringBuilder("SELECT ");
    final protected StringJoiner selectClauses = new StringJoiner(", ");

    public abstract T where();
}
