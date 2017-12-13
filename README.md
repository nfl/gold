# gold
GraphQL based library that allows for the dynamic specification of domain objects

This service allows application developers to define new domain objects
dynamically that will behave like any other domain objects.

# API

The API is based on GraphQL.

A natural partitioning exists between schema operations (DDL in DB terms)
and data operations (DML in DB terms).  This project encapsulates both.
Adding a "schema" object results in enabled operations on the endpoint. 
Removing a "schema" object removes all data and operations on that object
are no longer allowed.

## Terminology

* **type namespace** separates schema definitions (and future definitions such as versionPolicy)
* **instance namespace** separates instances.  While instances may employ a schema definition from any **type namespace**,
all instance data is contained within a single **instance namespace**.

## Requirements

Field selection is required with the caveat that dynamic domain objects
do not have the concept of a default field.  The client must explicitly
specify desired fields.  By default, no fields are returned.

Literal fields are implemented: integer, boolean, String, enum.
References to other types of dynamic domain objects is implemented.
References to the same type is implemented.  Value types are also available.
Value types are structures that are stored with the dynamic domain object
and do not have their own id.  An example would be an address.  The dynamic
domain object could have both a billing and shipping address.

## Security
Read/View operations are allowed without the need for a specific 
permission.

All mutation operations require valid
permissions.  The available permissions are

* SCHEMA_MODIFY
* INSTANCE_MODIFY
* INSTANCE_DELETE
* INSTANCE_TRUNCATE

**SCHEMA_MODIFY** allows for creation of new schemas,
 modification to the structure of existing schemas and deletion of
 existing schemas.  Note that deletions are only allowed on schemas
 that contain no instance data.  This permission is restricted to a given
 schema namespace.
 
**INSTANCE_MODIFY** allows for creation and updates to
instances within the namespace that the role is bound to.

**INSTANCE_DELETE** allows for deletion of an instance within
the bound namespace.

**INSTANCE_TRUNCATE** allows for deletion of all instances
within the bound namespace.

## Persistence Layer

In memory hash maps are used currently for automated testing.  For
deployed servers, SchemaRepository and SchemaInstanceRepository interfaces shall be implemented.

# SchemaDescription - Defining Domain Objects

Schemas are defined using the SchemaDescription class.  Runtime uses a _SchemaDescription_ to build a _SchemaDefinition_, so these 2 terms are often used here interchangeably.
  
The *schemaKey* field specifies both the name of the schema and the (schema) namespace on which it resides.
schemaKey is how the schema is referenced and serves as its id.

Each schema may have a *description*, which can be useful for tools as
documentation.

## ID Generation/Ownership

A dynamic domain object may have either a client generated id (which
the application manages) or a server generated id (guids) and is specified
by setting the *idGeneration* field in the SchemaDescription.

## Member Configuration

An optional *memberConfiguration* field provides the ability for the client to attach client-specific
information to the domain object as a whole.  The contents of the field can be in any format (e.g. csv, json, etc)
serialized to a string and is interpreted by the client for their purposes.

## Filter Configuration

An optional *filterConfiguration* field provides a way to filter instances returned from root relay edges.
*filterConfiguration* is set up on the schema side.
Three fields need to be provided for *filterConfiguration*:
* *fieldName* field name (must be a literal type [String, Number, Boolean, Enum])
* *filterName* name of the parameter to be provided on the relay object.
* *filterOperator* operator that should be applied for the filtering.

Ex]
```
 mutation  {
   upsertSchemaDefinition(schemaDef: {
     name: "FilterableObject",
     idGeneration: Client,
     domainFields: [{
         memberType:        String,
         memberFieldName:   "filterableStringField"
     },
     {
         memberType:        Boolean,
         memberFieldName:   "filterableBooleanField"
      },
      {
         memberType:        Integer,
         memberFieldName:   "filterableIntegerField"
       }],
     filterConfiguration: [{
         fieldName: "filterableStringField",
         filterName: "thisIsAStringFilter",
         filterOperator: EQUALS
      },
      {
         fieldName: "filterableBooleanField",
         filterName: "thisIsABooleanFilter",
         filterOperator: EQUALS
        },
      {
         fieldName: "filterableIntegerField",
         filterName: "thisIsAIntegerFilter",
         filterOperator: EQUALS
      }]
   })
   {
     name
   }
 }
``` 

Utilizing the filter on the instance side

```
query {
  viewer {
    instances(filters: {
        thisIsAStringFilter: "someValueToMatch",
        thisIsABooleanFilter: true,
        thisIsAIntegerFilter: 2015}) {
      edges {
        node {
          id
        }
      }
    }
  }
}
```

## Schema Dependency Relationships

The optional read only *referencedBy* field allows one to find a list of schemas that have definitional references
to this schema.  One of the use cases would be to insure that deleting a given schema does not break existing schemas.
It is a collection of SchemaDescriptions.

## Instance Count
The optional read only *instanceCount* field takes a required _instanceNamespace_ as an argument.  It returns a count
of the number of instances in _instanceNamespace_ for the given SchemaDescription.

## Instance updateDate
The optional read only *updateDate* field returns the time in milliseconds since the epoch (January 1, 1970, 00:00:00 GMT)
when the instance was last updated or 0, representing the epoch for objects not modified before this new field was 
introduced in the API.

## Schema Fields
A dynamic domain object has a *domainFields* collection containing
SchemaInstanceField definitions.  For every field, it *memberFieldName*
must be set which is the name of the field both in view and mutation
queries.  Each field may have a *memberDescription* which can be useful
for tools as documentation.

Each field has a type, specified in *member_type*.

A field may specified with the REQUIRED constraint, which insures that any mutation will specify a value for that field.
By default, the NONE constraint is applied.

Each domain field may also have its own optional *memberConfiguration* independent of the *memberConfiguration* in SchemaDescription.
*memberConfiguration* can be useful for holding client specific data (e.g. labels, help text, etc) 
in any format (e.g. csv, json, etc).

### Literals

String, Integer and Boolean behave as expected.  There is no explicit
length restriction for String.

### Enum

Provides for an explicit set of choices as specified in *enumValues*. 
Each enum value has both a *name* (displayed) and a *value* (stored).

### AnotherDynamicDomainReference
Contains a reference to another instance of another dynamic domain object.
Which kind of dynamic domain object is specified in *otherTypeName*.

### AnotherInstanceReference
Contains a reference to another instance (perhaps itself) of the same
type.  This is an optimization (with more flexibility) of
AnotherDynamicDomainReference.

### MultiTypeDynamicReference
Contains a reference to another instance of predefined *possibleTypes*. 
The *possibleTypes* specifies which kind of dynamic domain object are supported. 

For example *mediaRef* supports [Video, Audio] types: 

```
 mutation  {
   upsertSchemaDefinition(schemaDef: {
     name: "Media",
     idGeneration: Client,
     memberConfiguration: "some test data...",
     domainFields: [{
         memberType:        MultiTypeDynamicReference,
         memberFieldName:   "mediaRef",
         possibleTypes:     ["Photo", "Video"],
         memberConfiguration: "some domainFields specific test data..."
     }]
   })
   {
     name
   }
 }
``` 

When writing instances of dynamic domain object containing an *MultiTypeDynamicReference*,
the _id_ is compound one, containing both an *id* field and a *typeDefinition*
field containing one of the configured values from *possibleTypes*.  

For example, a Media object having a reference to a Video do the following upsert mutation:

```
{
    id: element1,
    mediaRef: {
        id: video-object-id,
        schemaInstanceKey: {
            schemaName: "Video"
        }
    }
}
```

As well as for Audio type:

```
{
    id: element2,
    mediaRef: {
        id: audio-object-id,
        schemaInstanceKey: {
            schemaName: "Audio"
        }
    }
}
```

Note that only the value of the schemaName field from schemaInstanceKey is used for specifying the reference type,
the other fields are accepted in the mutation but are ignored. There has not been a firm need to operate in multiple 
namespaces at this time.
 
### ExternalReference
Contains a reference to a domain object from another service.  Currently, only
Shield domain objects are supported but the implementation could be easily extended
to other services.

The external reference requires *serviceKey* to be set to _SHIELD_ and
*possibleTypes* set to a collection of anticipated types.  *serviceKey* allows for a variety of external services
in the future (such as "WEATHER" which could point to http://weather.gov).  Only the value of _SHIELD_ is
currently supported.  A Shield example for *possibleTypes*
would be ["Image", "Article", "Video"].

When writing instances of dynamic domain object containing an external reference,
the _id_ is compound one, containing both an *id* field and a *typeDefinition*
field containing one of the configured values from *possibleTypes*.  For example, a Hero object having a reference
to a shieldImage do the following upsert mutation:
```
{
    id: 1234-hero-id,
    shieldImage: {
        id: 1234-shield-id,
        typeDefinition: "Image"
    }
}
```

Upon the reading the value, the *shieldImage* field would appear to be an embedded shield image object.

### Struct
Defines a "value" object, one that is stored as part of the dynamic domain
object.  One example would be an "address" value object whereas the
dynamic domain contains a *billingAddress* and a *shippingAddress* field.
*otherTypeName* refers to a Struct definition.  Schema Definition field *valueDefinitions* must contain all Struct definitions used by this schema (see 'Value Objects').

### Value Objects
Value objects have a *name* and a *valueFields* collection containing
SchemaInstanceField definitions.  The name is referred to by one or more Struct fields using *otherTypeName* (see 'Struct').

### Array Objects
Defines a list of other types.  The other type is specified by *arrayEntryType*.
Arrays may not contain arrays.  All other types are supported.
    
## View schema definitions: `GraphQLSchemaService.executeQuery(String graphqlQuery, Map graphqlVariables, SchemaWriteAccess currentUserPermissions)`

Example queries and results may be found in test/resources/graphql.

All schemas can be listed.  If an argument is present, then a single
schema is selected.

### Example For Listing All Schemas
```
query {
  viewer {
    schemas {
      edges {
        node {
          schemaKey {
            schemaName
          }
          description
        }
      }
    }
  }
}
```
Response with no schemas stored:  `{data={viewer={schemas={edges=[]}}}}`

Response with the Paint schema stored:
```
{
  "data": {
    "viewer": {
      "schemas": {
        "edges": [
          {
            "node": {
              "schemaKey": {
                "schemaName": "Paint"
              },
              "description": "A placeholder description."
            }
          }
        }
      }
    }
  ]
}
```

### Example listing a specific schema
```
query {
  viewer {
    schemas(names:["Paint"]) {
      edges {
        node {
          schemaKey {
            schemaName
          },
          instanceCount(instanceNamespace:"colors")
        }
      }
    }
  }
}
```
Result:
```
{
  "data": {
    "viewer": {
      "schemas": {
        "edges": [
          {
            "node": {
              "schemaKey": {
                "schemaName": "Paint"
              }
              "instanceCount": 35
            }
          }
        ]
      }
    }
  }
}
```

## Modify a schema definition: `GraphQLSchemaService.executeQuery(String graphqlMutationQuery, Map graphqlVariables, SchemaWriteAccess currentUserPermissions)`

The schema description interface supports two mutation operations:
upsertSchemaDefinition and removeSchemaDefinition.

### upsertSchemaDefinition

Updates the schema definition.  There is no difference between an
insert or an update from an API perspective, hence the upsert term.

#### Upsert Schema Definition Example
```
mutation  {
  upsertSchemaDefinition(schemaDef: {
    name: "Paint",
    idGeneration: Client,
    domainFields: [{
        memberType:      Enum,
        memberFieldName: "color",
        memberDescription: "The color of the paint.",
        enumValues: [{
            name: "Red",
            value: "RED"
        }, {
            name: "Blue",
            value: "BLUE"
        }, {
            name: "Green",
            value: "GREEN"
        }],
        constraints: [
               { instanceMutationSchemaConstraint: REQUIRED}
               ]
    }]
  })
  {
    name,
    domainFields {
      memberType,
      memberFieldName,
      memberDescription
    }
  }
}
```

Results:
```
{
  "upsertSchemaDefinition": {
    "name": "Paint",
    "domainFields": [
      {
        "memberType": "Enum",
        "memberFieldName": "color",
        "memberDescription": "The color of the paint."
      }
    ]
  }
}
```

### removeSchemaDefinition

Precondition: No instance data.  If instance data exists, this mutation
fails.

Removes the schema definition from the store.

#### Remove Schema Example

```
mutation  {
  removeSchemaDefinition(name: "Paint")
  {
    name
  }
}
```
Results:
```
{
  "removeSchemaDefinition": {
    "name": "Paint"
  }
}
```

# Using Domain Objects

In the _variables_ parameter, the following three are required:
    DYNAMIC_TYPE_NAMESPACE = namespace to use for type definitions
    DYNAMIC_TYPE_NAME = top level type name of the desired dynamic domain
    DYNAMIC_INSTANCE_NAMESPACE = {role.domain}, where all the instances are stored

If any of these are missing or the SchemaDescription is not found, an exception is thrown.

For all definitions, `schemaName` is the domain name of the object,
e.g. "Paint".

Field selection and queries based on fields.

## Instance Dependency Relationships

The optional read only *referencedBy* field allows one to find a list of instances that reference this instance.
One of the use cases would be to insure that deleting this instance does not break another instance.
It is a collection of objects with the following structure
```
{
  id,
  schemaInstanceKey {
    schemaNamespace,
    schemaName,
    instanceNamespace,
    label
  }
```
All the fields allow one to uniquely identify the domain object of interest.

## Reserved Fields

The *id*, *updateDate* and *referencedBy* fields are reserved for *gold* usage.  They may not be specified when creating
a SchemaDescription.

### View instances: `GraphQLInstanceService.executeQuery(String graphqlQuery, Map graphqlVariables, SchemaWriteAccess currentUserPermissions, int maxQueryDepth)`
Executes the given query as specified in the _query_ parameter.

*maxQueryDepth* equal to 5 would be a good default.  Most users need only about 3 levels of depth.  Deeper nesting is
supported up to 15 levels.
Given the recursive nature of building out graph trees, setting deeper levels may have significant impact on performance.

*currentUserPermissions* does not need any permissions, but it cannot be null
since *SchemaWriteAccess.getAuthHeader()* is used to fetch by external references.
   
All dynamic domain instances can be listed.  If an argument is present, then the specific instances are selected.

#### Example For Listing All Instances
```
query {
  viewer {
    instances {
      edges {
        node {
          id
        }
      }
    }
  }
}
```
Results:
```
{
  "data": {
    "viewer": {
      "instances": {
        "edges": [
          {
            "node": {
              "id": "red"
            }
          }
        ]
      }
    }
  }
}
```

#### Example For Listing One or More Specific Instances
```
query {
  viewer {
    instances(ids: ["red", ...] {
      edges {
        node {
          id,
          referencedBy {
            id,
            schemaInstanceKey {
              schemaName
            }
        }
      }
    }
  }
}
```
Results:
```
{
  "data": {
    "viewer": {
      "instances": {
        "edges": [
          {
            "node": {
              "id": "red",
              "schemaInstanceKey": {
                "schemaName": "Paint"
              }
            }
          }
        ]
      }
    }
  }
}
```


## Managing instances: `GraphQLInstanceService.executeQuery(String graphqlMutationQuery, Map graphqlVariables, SchemaWriteAccess currentUserPermissions, int maxQueryDepth)`

The schema instance interface supports three mutation operations:
upsertSchemaInstance, removeInstance and removeAllInstances.

### upsertSchemaInstance

Updates the schema instance.  There is no difference between an
insert or an update from an API perspective, hence the upsert term.

#### Example

```
mutation  {
  upsertSchemaInstance(schemaInstance: {
    id: "red",
    color: Red
  })
  {
    id,
    color
  }
 }
```

Result:
```
{
  "upsertSchemaInstance": {
    "id": "red",
    "color": "Red"
  }
}
```

### removeInstance

Removes the selected instance.

#### Example
```
mutation  {
  removeInstance(id: "red")
  {
    id
  }
}
```
Results:
```
{
  "removeInstance": {
    "id": "red"
  }
}
```

### removeAllInstances

Truncates all instances in this schema.  Useful mainly in testing and
as a precondition to deleting a schema.

#### Example

```
mutation  {
  removeAllInstances
  { 
    count
  }
}
```
Results:
```
{
  "removeAllInstances": {
    "count": 1
  }
}
```
There was one instance in the Paint collection.

## Labels

Instances can have a label associated with them.  This allows for applications to have multiple versions of the
same instance.  If no label is specified, the system default is **PUBLISHED**.  A label is specified by the `label`
argument passed to upsert mutation or to the viewer query.

For example, an application could model a series of labels like **DRAFT**, **NEEDS_APPROVAL**, **APPROVED** in addition to
**PUBLISHED**.  It is up to the application to define what labels are meaningful.

### Viewing Example
```
query {
  viewer {
    instances(label: "DRAFT", ids: ["red", ...] {
      edges {
        node {
          id
        }
      }
    }
  }
}
```
Results:
```
{
  "data": {
    "viewer": {
      "instances": {
        "edges": [
          {
            "node": {
              "id": "red"
            }
          }
        ]
      }
    }
  }
}
```

### Mutation Example

```
mutation  {
  upsertSchemaInstance(label: "NEEDS_APPROVAL", schemaInstance: {
    id: "red",
    color: Red
  })
  {
    id,
    color
  }
 }
```

Result:
```
{
  "upsertSchemaInstance": {
    "id": "red",
    "color": "Red"
  }
}
```

## Ordering instances by a field
You can use connection arguments _orderBy_ and _orderByDirection_ to sort instances by a field.
At the moment, _OrderBy_ enum allows to order only by _updateDate_.
_OrderByDirection_ enum allows for values _ASC_ (the default) or _DESC_.

### Example

```
query {
  viewer {
    instances(orderBy: updateDate, orderByDirection: ASC) {
      edges {
        node {
          id
          updateDate
        }
      }
    }
  }
}
```
Result:
```
{
  "viewer": {
    "instances": {
      "edges": [
        {
          "node": {
            "id": "instance # 1",
            "updateDate": 1510167697350,
          }
        },
        {
          "node": {
            "id": "instance # 2"
            "updateDate": 1510167697346,
          }
        }
      ]
    }
  }
}
```

## Pagination

Both schema and instance endpoints support pagination.  By default, a query returns all matching elements.  To fetch only a page of data, use typical Relay arguments:
- Forward pagination arguments
  - *first* - fetching only the first certain number of nodes
  - *after* - fetching only nodes after this cursor (exclusive)
- Backward pagination arguments
  - *before* - fetching only nodes before this cursor (exclusive)
  - *last* - fetching only the last certain number of nodes

In response, a Relay connection provides:
- PageInfo object as per [Relay Cursor Connections Specification](https://facebook.github.io/relay/graphql/connections.htm)
- *totalCount* - the total number of elements matching other (semantic) filters; page filters are applied afterwards.

### Example

```
query {
  viewer {
    instances(first: 2, after: "c2ltcGxlLWN1cnNvcjI=") {
      edges {
        node {
          id
        }
        cursor
      }
      pageInfo {
        startCursor
        endCursor
        hasNextPage
        hasPreviousPage
      }
      totalCount
    }
  }
}
```
Result:
```
{
  "viewer": {
    "instances": {
      "edges": [
        {
          "node": {
            "id": "instance # 1"
          },
          "cursor": "c2ltcGxlLWN1cnNvcjM="
        },
        {
          "node": {
            "id": "instance # 0"
          },
          "cursor": "c2ltcGxlLWN1cnNvcjQ="
        }
      ],
      "pageInfo": {
        "startCursor": "c2ltcGxlLWN1cnNvcjM=",
        "endCursor": "c2ltcGxlLWN1cnNvcjQ=",
        "hasNextPage": false,
        "hasPreviousPage": false
      },
      "totalCount": 5
    }
  }
}
```