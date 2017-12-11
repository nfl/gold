package com.nfl.dm.shield.dynamic.domain.instance;

import java.util.Arrays;
import java.util.Comparator;

import static com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance.UPDATE_DATE_FIELD;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

public enum OrderBy {

    /**
     * Order by updateDate (Long millis since Epoch); 'null' is treated as the earliest possible date.
     */
    UPDATE_DATE(UPDATE_DATE_FIELD, nullsFirst(comparing(instance -> (Long) instance.get(UPDATE_DATE_FIELD))));

    private final String fieldName;
    private final Comparator<SchemaInstance> comparator;

    OrderBy(String fieldName, Comparator<SchemaInstance> comparator) {
        this.fieldName = fieldName;
        this.comparator = comparator;
    }

    /**
     * Returns the comparator for the field (for in-memory sorting of the result set).
     *
     * @return a comparator by updateDate: nulls first, then earliest update dates, and eventually most recent update dates
     */
    public Comparator<SchemaInstance> getComparator() {
        return comparator;
    }

    public String getFieldName() {
        return fieldName;
    }

    public static OrderBy resolve(String fieldName) {
        return Arrays.stream(values())
                .filter(v -> v.getFieldName().equals(fieldName))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find enum value by field name " + fieldName));
    }

}
