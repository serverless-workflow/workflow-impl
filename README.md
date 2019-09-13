# Serverless Workflow Impl

This project provides an implementation of the 
Serverless Workflow Specification Version 0.1 (https://github.com/cncf/wg-serverless/blob/master/workflow/spec/spec.md)

It provides two implementations for the serverless workflow api:
* WorkflowManagerImpl
* WorkflowValidatorImpl

as well as two implementations of workflow expression evaluators:
* JexlExpressionEvaluatorImpl
* SpelExpressionEvaluatorImpl

### Getting Started

To build project and run tets:

```
mvn clean install
```

To use this project add the following dependencies into your project pom.xml:

```xml
<dependency>
    <groupId>org.servlerless</groupId>
    <artifactId>workflow-impl</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### API Examples

#### Workflow Model(JSON/YAML) To Object Model
This project supports Workflow Model in both JSON and YAML formats.

Given serverless workflow JSON which represents a workflow with a single Event State, for example:

```json
{
  "name" : "testname",
  "version" : "testversion",
  "description" : "testdescription",
  "owner" : "testOwner",
  "states" : [ {
    "events" : [ {
      "event-expression" : "testEventExpression",
      "timeout" : "testTimeout",
      "action-mode" : "SEQUENTIAL",
      "actions" : [ {
        "function" : "testFunction",
        "timeout" : 5,
        "retry" : {
          "match" : "testMatch",
          "retry-interval" : 2,
          "max-retry" : 10,
          "next-state" : "testNextRetryState"
        }
      } ],
      "next-state" : "testNextState"
    } ],
    "name" : "eventstate",
    "type" : "EVENT",
    "start" : true
  } ]
}

```

You can use this project to read it into the workflow api:

```java
WorkflowManager manager = WorkflowManagerProvider.getInstance().get();
manager.setMarkup(json);

Workflow workflow = manager.getWorkflow();

EventState eventState = (EventState) workflow.getStates().get(0);
assertEquals("testEventExpression", event.getEventExpression());

Action action = eventState.getActions().get(0);
assertEquals("testFunction", action.getFunction());


```

Same workflow model can be represented with YAML, for example:

```yaml
name: "test-wf"
states:
- events:
  - event-expression: "testEventExpression"
    timeout: "testTimeout"
    action-mode: "SEQUENTIAL"
    actions:
    - function:
        name: "testFunction"
      timeout: 5
      retry:
        match: "testMatch"
        retry-interval: 2
        max-retry: 10
        next-state: "testNextRetryState"
    next-state: "testNextState"
  name: "test-state"
  type: "EVENT"
  start: true

```

#### Object model to Workflow Model(JSON/YAML)

You can create the Workflow programatically, for example:

```java
Workflow workflow = new Workflow().withStates(new ArrayList<State>() {{
    add(
            new SwitchState().withName("switch-state").withDefault("defaultteststate").withStart(false).withChoices(
                    new ArrayList<Choice>() {{
                        add(
                                new AndChoice().withNextState("testnextstate").withAnd(
                                        Arrays.asList(
                                                new DefaultChoice().withNextState("testnextstate")
                                                        .withOperator(DefaultChoice.Operator.EQ)
                                                        .withPath("testpath")
                                                        .withValue("testvalue")
                                        )
                                )
                        );
                    }}
            )
    );
}});

WorkflowManager manager = WorkflowManagerProvider.getInstance().get();
manager.setWorkflow(workflow)

String json = manager.toJson();


```
This will produce a workflow JSON with a single Switch State:

```json
{
  "states" : [ {
    "choices" : [ {
      "and" : [ {
        "path" : "testpath",
        "value" : "testvalue",
        "operator" : "EQ",
        "next-state" : "testnextstate"
      } ],
      "next-state" : "testnextstate"
    } ],
    "default" : "defaultteststate",
    "name" : "switchstate",
    "type" : "SWITCH",
    "start" : false
  } ]
}
```

If we want to get Yaml:


```java
String yaml = manager.toYaml();

```

which would produce:

```yaml
name: "test-wf"
states:
- choices:
  - and:
    - path: "testpath"
      value: "testvalue"
      operator: "EQ"
      next-state: "testnextstate"
    next-state: "testnextstate"
  default: "defaultteststate"
  name: "test-state"
  type: "SWITCH"
  start: false

```

#### Workflow Validation
This project provides an implementation of the Workflow Validator. 

Workflow manager can help you get both JSON schema and workflow model validation errors. 
For example if we have a bare valid workflow definition without any states:

```json
{
  "states" : []
}
```

we can get validation errors:

```java
    WorkflowManager manager = WorkflowManagerProvider.getInstance().get();
    manager.setMarkup(json);
    
    WorkflowValidator validator = workflowManager.getWorkflowValidator();
    List<ValidationError> errors = validator.validate();
    
    assertEquals(0, errors.size());
    assertTrue(validator.isValid());
    
```

In cases of defined enum types, if illegal values are specified in json, WorkflowManager will
be unable to parse it and throw an IllegalStateException. For example let's say we define an illegal state type:


```json
{
  "name": "test-wf",
  "states": [
    {
      "time-delay": 5,
      "next-state": "testNextState",
      "name": "test-state",
      "type": "CUSTOMSTATETYPE",
      "start": true
    }
  ]
}
```

This will cause parsing exception:

 ```java
 assertThrows(IllegalArgumentException.class,
                      () -> {
                          WorkflowManager workflowManager =  WorkflowManagerProvider.getInstance().get();
                          workflowManager.setMarkup(json);
                      });
     
```

Workflow validation checks for both schema validation and workflow-specific validation.
There are some tests which are considered "strict mode", these include for example multiple start/end states etc.

String validation is disabled by default, but you can enable it with:

```java
    WorkflowManager workflowManager = WorkflowManagerProvider.getInstance().get();
    workflowManager.setMarkup(json);
    WorkflowValidator workflowValidator = workflowManager.getWorkflowValidator();
    workflowValidator.setStrictValidationEnabled(true);    
```

You can also disable schema validation completely (only workflow based validation will be performed):
```java
   ...
    workflowValidator.setSchemaValidationEnabled(false);
```

Or you can disable validation completely:
```java
    ...
    workflowValidator.setEnabled(false);
```

#### Event Expression evaluation
According to the specification Event States wait for events to happen before triggering one or more functions.
Event states can have multiple events, and each event has an event-expression which defines which outside
events they should trigger upon.

This project provides two event expression evaluator implementations. 
The default one is based on Apache Commons JEXL (http://commons.apache.org/proper/commons-jexl/).
Alternatively out of the box you can also use Spring Expression Language (SpEL) (https://docs.spring.io/spring/docs/5.2.0.RC1/spring-framework-reference/core.html#expressions)
expressions.

Each evaluator has a name specified. To use SpEL evaluator you need to tell the workflow manager:

To use SpEL you need to pass it to the workflow controller:
```java
    ...
    workflowManager.setDefaultExpressionEvaluator("spel");
```

If no expression evaluator is specified, the default one based on Apache Commons JEXL is used.

If the default event expression evaluator is used, you can use full powers of JEXL to write your event expressions.
Here are two simple examples:

```json
...
"event-expression": "trigger.equals('testtrigger')"
...
"event-expression": "trigger.equals('testtrigger') or trigger.equals('testtrigger2')",
...
```

For more information on JEXL language syntax, see here: https://commons.apache.org/proper/commons-jexl/reference/syntax.html


Similarly if you use SpEL, you can do for example:

```json
...
"event-expression": "trigger != null && trigger.equals('testtrigger')",
...
"event-expression": "trigger != null && (trigger.equals('testtrigger') || trigger.equals('testtrigger2'))",
...
```

### More to come soon!