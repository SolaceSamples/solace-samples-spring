## Spring Overview

The Spring Framework is an application framework and inversion of control container for the Java platform. Dating back to the early 2000s the Spring ecosystem has grown tremendously and has become the most popular framework for Enterprise Java Developers. 

- https://spring.io/
- https://spring.io/projects

## Solace PubSub+ Spring

At Solace we support integrating our PubSub+ Event Broker with Spring in several different ways to promote the ease of building event driven applications. This repository contains code samples for doing so, but it's best to clone the repo and then follow the step by step tutorials here: [tutorials home page](https://solace.com/samples/solace-samples-spring/)
* Spring Boot Autoconfigure
* Spring Cloud Stream
* Spring Integration (JMS) [Coming soon!]

## Access a PubSub+ Service

The Spring Tutorials require that you have access to a PubSub+ Service. You can quickly set one up for FREE by following [these instructions](https://solace.com/try-it-now/)

## Contents

This repository contains code and matching tutorial walk throughs for different basic scenarios. It is best to view the associated [tutorials home page](https://dev.solace.com/samples/solace-samples-spring/).

## Prerequisites

Install the data model
``` bash
cd spring-samples-datamodel
mvn clean install
```

## Running the Samples

To try individual samples, go into the project directory and run the sample using maven.

``` bash
cd cloud-streams-sink
mvn spring-boot:run
```

See the individual tutorials linked from the [tutorials home page](https://dev.solace.com/samples/solace-samples-spring/) for full details which can walk you through the samples, what they do, and how to correctly run them to explore Spring

## Exploring the Samples

### Setting up your preferred IDE

Using a modern Java IDE provides cool productivity features like auto-completion, on-the-fly compilation, assisted refactoring and debugging which can be useful when you're exploring the samples and even modifying the samples. Follow the steps below for your preferred IDE.

This repository uses Maven projects. If you would like to import the projects into your favorite IDE you should be able to import them as Maven Projects. For examples, in eclipse choose "File -> Import -> Maven -> Existing Maven Projects -> Next -> Browse for your repo -> Select which projects -> Click Finish"

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Authors

See the list of [contributors](https://github.com/SolaceSamples/solace-samples-spring/contributors) who participated in this project.

## License

This project is licensed under the Apache License, Version 2.0. - See the [LICENSE](LICENSE) file for details.

## Resources

For more information try these resources:

- [Tutorials](https://tutorials.solace.dev/)
- The Solace Developer Portal website at: [Developer Portal](http://solace.com/developers)
- Check out the [Solace blog](https://solace.com/blog/category/developers/) for other interesting discussions around Solace technology
- Follow our Developer Advocates on Twitter for development tips & to keep up with the latest Solace news! [@SolaceDevs](https://twitter.com/solacedevs)
