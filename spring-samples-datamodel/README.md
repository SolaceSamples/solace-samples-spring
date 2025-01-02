The purpose of a data model in a software application is to define the structure and format of the data that the application will use. It typically includes classes that represent the entities in the application, along with their attributes and relationships. In this context, The `SensorReading` class is part of the data model, representing a sensor reading with attributes like 

```
timestamp, sensorID, temperature, and baseUnit
```

### Installing the data model

Install the data model.

```sh
cd spring-samples-datamodel
mvn clean install
```

When you run `mvn install`, Maven performs the following tasks:
1. **Compiles the Code**
2. **Runs Tests** (if present)
3. **Packages the Code**
4. **Installs the Package** into the local Maven repository.

This allows other projects to include it as a dependency, ensuring that they can use the data model classes without having to include the source code directly. 


```log
NOTE: This should be the first step before running any of the projects from this repository.
```

### Including the Data Model as a Dependency

In all the projects in this repository wherever ```SensorReading``` object is referred and used, you will find reference to this data-model dependency by the inclusion in the `pom.xml`:

```xml
<dependency>
    <groupId>com.solace.samples.spring</groupId>
    <artifactId>spring-samples-datamodel</artifactId>
    <version>1.0.0</version>
</dependency>
```

This way, the other project can use the `SensorReading` class and other data model classes defined in the 

