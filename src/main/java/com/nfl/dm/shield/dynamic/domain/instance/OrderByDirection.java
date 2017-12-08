package com.nfl.dm.shield.dynamic.domain.instance;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

public enum OrderByDirection {

    ASC(Function.identity()), DESC(Comparator::reversed);

    private Function<Comparator<SchemaInstance>, Comparator<SchemaInstance>> directionSwitch;

    OrderByDirection(Function<Comparator<SchemaInstance>, Comparator<SchemaInstance>> directionSwitch) {
        this.directionSwitch = directionSwitch;
    }

    public Comparator<SchemaInstance> deriveComparator(OrderBy orderBy) {
        return directionSwitch.apply(orderBy.getComparator());
    }

    public static OrderByDirection resolve(String direction) {
        return Arrays.stream(values())
                .filter(v -> v.name().equals(direction))
                .findAny()
                .orElse(ASC);
    }

}
