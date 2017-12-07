package com.nfl.dm.shield.dynamic.repository;

import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseRepositoryImpl {

    protected List<SchemaInstance> buildListByIds(Map<String, SchemaInstance> store, List<String> ids) {
        return ids.stream().filter(store::containsKey).map(store::get).collect(Collectors.toList());
    }
}
