package com.nfl.dm.shield.dynamic.service

import com.nfl.dm.shield.dynamic.domain.context.GraphQLInstanceRequestContext
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance
import com.nfl.dm.shield.dynamic.domain.schema.FilterConfiguration
import com.nfl.dm.shield.dynamic.repository.ExternalReferenceRepository
import com.nfl.dm.shield.dynamic.repository.SchemaInstanceRepository
import graphql.schema.DataFetchingEnvironmentImpl
import spock.lang.Specification

@SuppressWarnings("GroovyAssignabilityCheck")
class InstanceDataFetcherTest extends Specification {

    def cacheService = Stub(CacheService)
    def schemaService = Stub(SchemaService)
    def schemaInstanceRepository = Stub(SchemaInstanceRepository)
    def externalReferenceRepository = Stub(ExternalReferenceRepository)

    def dataFetcherFactory = new DataFetcherFactory(cacheService, schemaService, schemaInstanceRepository, externalReferenceRepository)

    def "simple filter"() {
        setup:
        def filterConfigurations = [new FilterConfiguration(fieldName: "filteredFieldName", outputFilterName: "outputFilterName", filterOperator: FilterConfiguration.FilterOperator.EQUALS)]
        def instanceDataFetcher = dataFetcherFactory.getInstanceFetcher("AnyInstanceNamespace",
                "DomainWithFilterConfiguration", "AnySchemaNamespace",
                filterConfigurations)
        schemaInstanceRepository.listInstances(_) >> [new SchemaInstance([filteredFieldName: "filteredFieldNameValue"]),
                                                      new SchemaInstance([filteredFieldName: "somethingElse"]) ]
        schemaInstanceRepository.findById(_,_) >> new SchemaInstance([filteredFieldName: "filteredFieldNameValue", anotherFilteredFieldName: "something"])
        schemaInstanceRepository.findInstances(_,_) >> [new SchemaInstance([filteredFieldName: "filteredFieldNameValue"]),
                                                        new SchemaInstance([filteredFieldName: "somethingElse"]) ]
        def result = instanceDataFetcher.get(new DataFetchingEnvironmentImpl(null, argumentMap, new GraphQLInstanceRequestContext(null, null), null, null, null, null, null, null, null, null, null, null))


        expect:
        result.edges.size() == resultSize

        where:
        argumentMap || resultSize
        [:] || 2
        [filters: [outputFilterName: "filteredFieldNameValue"]] || 1
        [filters: [outputFilterName: "nonMatching"]] || 0
        [ids:["abc"], filters: [outputFilterName: "filteredFieldNameValue"]] || 1
        [ids:["abc"], filters: [outputFilterName: "nonMatching"]] || 0
        [ids:["abc", "def"], filters: [outputFilterName: "filteredFieldNameValue"]] || 1
        [ids:["abc", "def"], filters: [outputFilterName: "nonMatching"]] || 0

    }


    def "multiple filter"() {
        setup:
        def filterConfigurations = [new FilterConfiguration(fieldName: "filteredFieldName", outputFilterName: "outputFilterName", filterOperator: FilterConfiguration.FilterOperator.EQUALS),
                                    new FilterConfiguration(fieldName: "anotherFilteredFieldName", outputFilterName: "anotherOutputFilterName", filterOperator: FilterConfiguration.FilterOperator.EQUALS)]
        def instanceDataFetcher = dataFetcherFactory.getInstanceFetcher("AnyInstanceNamespace",
                "DomainWithFilterConfiguration", "AnySchemaNamespace", filterConfigurations)
        schemaInstanceRepository.listInstances(_) >> [new SchemaInstance([filteredFieldName: "filteredFieldNameValue", anotherFilteredFieldName: 20]),
                                                      new SchemaInstance([filteredFieldName: "somethingElse", anotherFilteredFieldName: 20]) ]
        schemaInstanceRepository.findById(_,_) >> new SchemaInstance([filteredFieldName: "filteredFieldNameValue", anotherFilteredFieldName: 20])
        schemaInstanceRepository.findInstances(_,_) >> [new SchemaInstance([filteredFieldName: "filteredFieldNameValue", anotherFilteredFieldName: 20]),
                                                        new SchemaInstance([filteredFieldName: "somethingElse", anotherFilteredFieldName: 20]) ]


        def result = instanceDataFetcher.get(new DataFetchingEnvironmentImpl(null, argumentMap,
                new GraphQLInstanceRequestContext(null, null), null, null, null, null, null, null, null, null, null, null))

        expect:
        result.edges.size() == resultSize


        where:
        argumentMap || resultSize
        [:] || 2
        [filters: [outputFilterName: "filteredFieldNameValue"]] || 1
        [filters: [outputFilterName: "nonMatching"]] || 0
        [filters: [outputFilterName: "filteredFieldNameValue", anotherOutputFilterName: 20]] || 1
        [filters: [outputFilterName: "filteredFieldNameValue", anotherOutputFilterName: 10]] || 0
        [ids:["abc"], filters: [outputFilterName: "filteredFieldNameValue", anotherOutputFilterName: 20]] || 1
        [ids:["abc"], filters: [outputFilterName: "nonMatching", anotherOutputFilterName: 20]] || 0
        [ids:["abc", "def"], filters: [outputFilterName: "filteredFieldNameValue", anotherOutputFilterName: 20]] || 1
        [ids:["abc", "def"], filters: [outputFilterName: "nonMatching", anotherOutputFilterName: 20]] || 0

    }
}
