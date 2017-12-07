package com.nfl.dm.shield.dynamic.service;

import graphql.relay.*;
import graphql.schema.DataFetchingEnvironment;

import java.util.List;

public class ConnectionWithTotal<T> extends DefaultConnection<T> {

    private Integer totalCount;

    private ConnectionWithTotal(List<Edge<T>> list, PageInfo pageInfo, Integer totalCount) {
        super(list, pageInfo);
        this.totalCount = totalCount;
    }

    @SuppressWarnings("unused")
    public Integer getTotalCount() {
        return totalCount;
    }

    public static <T> Connection<T> create(List<T> instances, DataFetchingEnvironment env) {
        Connection<T> cn = new SimpleListConnection<>(instances).get(env);
        return new ConnectionWithTotal<>(cn.getEdges(), cn.getPageInfo(), instances.size());
    }

}
