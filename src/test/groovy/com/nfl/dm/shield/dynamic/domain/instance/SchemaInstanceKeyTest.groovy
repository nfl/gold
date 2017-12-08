package com.nfl.dm.shield.dynamic.domain.instance

import spock.lang.Specification

class SchemaInstanceKeyTest extends Specification {

    def "SchemaInstanceKey-s considered equal only if all their components are equal"() {
        setup:
        def key1 = new SchemaInstanceKey(schema1, schemaNS1, instanceNS1, label1)
        def key2 = new SchemaInstanceKey(schema2, schemaNS2, instanceNS2, label2)

        expect:
        key1.equals(key2) == result

        where:
        schema1   | schemaNS1   | instanceNS1    | label1   | schema2   | schemaNS2   | instanceNS2    | label2    | result
        'aSchema' | 'aSchemaNS' | 'anInstanceNS' | 'aLabel' | 'aSchema' | 'aSchemaNS' | 'anInstanceNS' | 'aLabel'  | true
        'aSchema' | 'aSchemaNS' | 'anInstanceNS' | 'aLabel' | 'DIFFERS' | 'aSchemaNS' | 'anInstanceNS' | 'aLabel'  | false
        'aSchema' | 'aSchemaNS' | 'anInstanceNS' | 'aLabel' | 'aSchema' | 'DIFFERS'   | 'anInstanceNS' | 'aLabel'  | false
        'aSchema' | 'aSchemaNS' | 'anInstanceNS' | 'aLabel' | 'aSchema' | 'aSchemaNS' | 'DIFFERS'      | 'aLabel'  | false
        'aSchema' | 'aSchemaNS' | 'anInstanceNS' | 'aLabel' | 'aSchema' | 'aSchemaNS' | 'anInstanceNS' | 'DIFFERS' | false
    }

    def "SchemaInstanceKey-s with implicit label considered equal only if all other components are equal"() {
        setup:
        def key1 = new SchemaInstanceKey(schema1, schemaNS1, instanceNS1)
        def key2 = new SchemaInstanceKey(schema2, schemaNS2, instanceNS2)

        expect:
        key1.equals(key2) == result

        where:
        schema1   | schemaNS1   | instanceNS1    | schema2   | schemaNS2   | instanceNS2    | result
        'aSchema' | 'aSchemaNS' | 'anInstanceNS' | 'aSchema' | 'aSchemaNS' | 'anInstanceNS' | true
        'aSchema' | 'aSchemaNS' | 'anInstanceNS' | 'DIFFERS' | 'aSchemaNS' | 'anInstanceNS' | false
        'aSchema' | 'aSchemaNS' | 'anInstanceNS' | 'aSchema' | 'DIFFERS'   | 'anInstanceNS' | false
        'aSchema' | 'aSchemaNS' | 'anInstanceNS' | 'aSchema' | 'aSchemaNS' | 'DIFFERS'      | false
    }

    def "SchemaInstanceKey rendered as String shows all its components"() {
        setup:
        def schema = 'aSchema'
        def schemaNS = 'aSchemaNS'
        def instanceNS = 'anInstanceNS'
        def label = 'aLabel'
        def key = new SchemaInstanceKey(schema, schemaNS, instanceNS, label)

        when:
        def rendered = key.toString()

        then:
        rendered.contains(schema)
        rendered.contains(schemaNS)
        rendered.contains(instanceNS)
        rendered.contains(label)
    }

}
