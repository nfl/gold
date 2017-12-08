package com.nfl.dm.shield.dynamic.service

import com.nfl.dm.shield.dynamic.domain.context.GraphQLSchemaRequestContext
import com.nfl.dm.shield.dynamic.domain.schema.FilterConfiguration
import com.nfl.dm.shield.dynamic.domain.schema.instancefield.InstanceFieldType
import com.nfl.dm.shield.dynamic.repository.SchemaInstanceRepository
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess
import graphql.schema.DataFetchingEnvironmentImpl
import spock.lang.Specification

class SchemaGraphQLBuilderTest extends Specification {

    def schemaService = Stub(SchemaService)
    def instanceOutputTypeServiceFactory = Stub(InstanceOutputTypeServiceFactory)
    def schemaInstanceRepository = Stub(SchemaInstanceRepository)
    def dataFetcherFactory = Stub(DataFetcherFactory)

    def schemaGraphQLBuilder = new SchemaGraphQLBuilder(schemaService, schemaInstanceRepository, instanceOutputTypeServiceFactory, dataFetcherFactory)
    def upsertDataFetcher = schemaGraphQLBuilder.getUpsertFetcher()

    def "happy"() {

        def argumentMap = [schemaDef:[name:"someSchemaName", domainFields: [[memberFieldName:"someFieldName", memberType:InstanceFieldType.STRING]] ]]

        SchemaWriteAccess mutableAccess = new SchemaWriteAccess()
        mutableAccess.addPermission("SOME_SCHEMA_NAMESPACE", SchemaWriteAccess.SCHEMA_MODIFY)
        def context = new GraphQLSchemaRequestContext([DYNAMIC_TYPE_NAMESPACE: "SOME_SCHEMA_NAMESPACE"], mutableAccess, Collections.emptyMap())

        when:
        def result = upsertDataFetcher.get(buildDataFetchingEnvironment(argumentMap, context))

        then:
        result != null

    }

    def "happy filter configuration"() {

        def argumentMap = [schemaDef:[name:"someSchemaName", domainFields: [[memberFieldName:"someFieldName", memberType:InstanceFieldType.STRING]],
                            filterConfigurations:[[fieldName:"someFieldName", filterName:"someFilterName", filterOperator:FilterConfiguration.FilterOperator.EQUALS]]]]

        SchemaWriteAccess mutableAccess = new SchemaWriteAccess();
        mutableAccess.addPermission("SOME_SCHEMA_NAMESPACE", SchemaWriteAccess.SCHEMA_MODIFY)
        def context = new GraphQLSchemaRequestContext([DYNAMIC_TYPE_NAMESPACE: "SOME_SCHEMA_NAMESPACE"], mutableAccess, Collections.emptyMap())

        when:
        def result = upsertDataFetcher.get(buildDataFetchingEnvironment(argumentMap, context))

        then:
        result != null

    }

    def "non existing field with filter configuration"() {

        def argumentMap = [schemaDef:[name:"someSchemaName", domainFields: [[memberFieldName:"anotherFieldName", memberType:InstanceFieldType.STRING]],
                                      filterConfigurations:[[fieldName:"someFieldName", filterName:"someFilterName", filterOperator:FilterConfiguration.FilterOperator.EQUALS]]]]

        SchemaWriteAccess mutableAccess = new SchemaWriteAccess()
        mutableAccess.addPermission("SOME_SCHEMA_NAMESPACE", SchemaWriteAccess.SCHEMA_MODIFY)
        def context = new GraphQLSchemaRequestContext([DYNAMIC_TYPE_NAMESPACE: "SOME_SCHEMA_NAMESPACE"], mutableAccess, Collections.emptyMap())

        when:
        upsertDataFetcher.get(buildDataFetchingEnvironment(argumentMap, context))

        then:
        thrown IllegalArgumentException
    }

    def "non scala field with filter configuration"() {

        def argumentMap = [schemaDef:[name:"someSchemaName", domainFields: [[memberFieldName:"someFieldName", memberType:InstanceFieldType.SAME_REFERENCE]],
                                      filterConfigurations:[[fieldName:"someFieldName", filterName:"someFilterName", filterOperator:FilterConfiguration.FilterOperator.EQUALS]]]]

        SchemaWriteAccess mutableAccess = new SchemaWriteAccess()
        mutableAccess.addPermission("SOME_SCHEMA_NAMESPACE", SchemaWriteAccess.SCHEMA_MODIFY)
        def context = new GraphQLSchemaRequestContext([DYNAMIC_TYPE_NAMESPACE: "SOME_SCHEMA_NAMESPACE"], mutableAccess, Collections.emptyMap())

        when:
        upsertDataFetcher.get(buildDataFetchingEnvironment(argumentMap, context))

        then:
        thrown IllegalArgumentException
    }

    private static DataFetchingEnvironmentImpl buildDataFetchingEnvironment(
            LinkedHashMap<String, LinkedHashMap<String, Object>> argumentMap,
            GraphQLSchemaRequestContext context)
    {
        new DataFetchingEnvironmentImpl(null, argumentMap, context, null, null, null, null, null, null, null, null, null, null)
    }

}
