package com.doanduyhai.annotation_processing_demo.dsl;

import java.util.StringJoiner;

public abstract class AbstractWhere {

    final protected StringBuilder query;
    final protected StringJoiner whereConditions = new StringJoiner(" AND ");


    protected AbstractWhere(StringBuilder query) {
        this.query = query;
    }

    public String generateQuery() {
        query.append(" WHERE ").append(whereConditions.toString());
        return query.toString();
    }
}
