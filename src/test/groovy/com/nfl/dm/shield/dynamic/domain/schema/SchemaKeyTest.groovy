package com.nfl.dm.shield.dynamic.domain.schema

import spock.lang.Specification

class SchemaKeyTest extends Specification {

    def "SchemaKey-s considered equal only if all their components are equal"() {
        setup:
        def key1 = new SchemaKey(schema1, schemaNS1)
        def key2 = new SchemaKey(schema2, schemaNS2)

        expect:
        key1.equals(key2) == result

        where:
        schema1   | schemaNS1   | schema2   | schemaNS2   | result
        'aSchema' | 'aSchemaNS' | 'aSchema' | 'aSchemaNS' | true
        'aSchema' | 'aSchemaNS' | 'DIFFERS' | 'aSchemaNS' | false
        'aSchema' | 'aSchemaNS' | 'aSchema' | 'DIFFERS'   | false
    }

    def "SchemaKey rendered as String must show all its components"() {
        setup:
        def schema = 'aSchema'
        def schemaNS = 'aSchemaNS'
        def key = new SchemaKey(schema, schemaNS)

        when:
        def rendered = key.toString()

        then:
        rendered.contains(schema)
        rendered.contains(schemaNS)
    }

}
