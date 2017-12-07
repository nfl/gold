package com.nfl.dm.shield.dynamic.service

import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey
import com.nfl.dm.shield.dynamic.domain.schema.FilterConfiguration
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey
import com.nfl.dm.shield.dynamic.domain.schema.instancefield.*
import com.nfl.dm.shield.dynamic.repository.SchemaRepository
import graphql.Scalars
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLInputObjectType
import spock.lang.Specification

class GraphQLInstanceServiceUnitTest extends Specification {

    //graphql.Scalars.GraphQLString

    def schemaRepository = Stub(SchemaRepository)
    def relayGraphQLBuilder = Stub(GraphQLRelayBuilder)
    def instanceOutputTypeServiceFactory = Stub(InstanceOutputTypeServiceFactory)
    def cacheService = Stub(CacheService)
    def dataFetcherFactory = Stub(DataFetcherFactory)

    def graphQLInstanceService = new GraphQLInstanceService(schemaRepository, relayGraphQLBuilder,
            instanceOutputTypeServiceFactory, cacheService, dataFetcherFactory)

    def "build root child"() {

        setup:
        def schemaInstanceKey = new SchemaInstanceKey("FilteredSchema",
                "AnySchemaNamespace","AnyInstanceNamespace")
        def schemaKey = new SchemaKey("AnyDomain", "AnySchemaNamespace")
        def schemaDescription = new SchemaDescription()
        schemaDescription.domainFields = domainFields
        schemaDescription.name = 'schema_to_test_basic_types'
        schemaDescription.filterConfigurations = [new FilterConfiguration(fieldName: "filteredFieldName", outputFilterName: "outputFilterName", filterOperator: FilterConfiguration.FilterOperator.EQUALS)]
        def rootChild = graphQLInstanceService.buildRootChild(schemaInstanceKey, schemaKey, schemaDescription, null, 0, null)
        def fieldDefinition = rootChild.buildField()

        def filterInputType = (GraphQLInputObjectType) fieldDefinition.getArgument(FilterConfiguration.FILTERS_ARGUMENT).type

        expect:
        filterInputType.getField("outputFilterName").type == graphqlType

        where:
        domainFields                                                                                        || graphqlType
        [new StringType(memberType: InstanceFieldType.STRING, memberFieldName: "filteredFieldName")]        || Scalars.GraphQLString
        [new BooleanType(memberType: InstanceFieldType.BOOLEAN, memberFieldName: "filteredFieldName")]      || Scalars.GraphQLBoolean
        [new IntegerType(memberType: InstanceFieldType.INTEGER, memberFieldName: "filteredFieldName")]      || Scalars.GraphQLInt

    }

    def "test for enum"() {

        setup:
        def schemaInstanceKey = new SchemaInstanceKey("FilteredSchema",
                "AnySchemaNamespace", "AnyInstanceNamespace")
        def schemaKey = new SchemaKey("AnyDomain", "AnySchemaNamespace")
        def schemaDescription = new SchemaDescription()
        schemaDescription.name = 'schema_to_test_enum'
        schemaDescription.domainFields = [new EnumType(memberType: InstanceFieldType.ENUM,
                memberFieldName: "filteredFieldName", enumValues:[new EnumValueDef(name:"ABC", value:"ABC")] )]
        schemaDescription.filterConfigurations = [new FilterConfiguration(fieldName: "filteredFieldName", outputFilterName: "outputFilterName", filterOperator: FilterConfiguration.FilterOperator.EQUALS)]
        def rootChild = graphQLInstanceService.buildRootChild(schemaInstanceKey, schemaKey, schemaDescription, null, 0, null)

        when:
        def fieldDefinition = rootChild.buildField()
        def filterInputType = (GraphQLInputObjectType) fieldDefinition.getArgument(FilterConfiguration.FILTERS_ARGUMENT).type

        then:
        filterInputType.getField("outputFilterName").type.class == GraphQLEnumType

    }
}
