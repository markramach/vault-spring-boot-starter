
keytool -genseckey -alias vault -keyalg AES -keysize 256 -keypass password -storetype jceks -storepass password -keystore hmac.jks

## spring-boot-starter-consul

Are you struggling to maintain your distributed property files? Thinking about using a key-value store to configure your environment? This starter project will demonstrate how you can leverage a tool like Consul to simplify your configuration management.

### Getting Started

The easiest way to get started with this starter project is to fork, clone or download this repository.

	git clone https://github.com/markramach/spring-boot-starter-consul.git  
	
You will also need to install [Consul](https://www.consul.io/intro/getting-started/install.html). This starter project assumes that you have a basic understanding of Consul's key-value management operations.

### Spring Boot Auto Configuration

If you're developing a Spring Boot application, you can have properties injected into the application context during the bootstrap phase of application startup. Injecting properties at this point allows you to leverage many of the features that were recently released from the [Spring Cloud Config](http://projects.spring.io/spring-cloud/) project. This includes the ability to refresh your properties at any point during the application lifecycle using the predefined `/refresh` endpoint and the `@RefreshScope` annotation.

This starter project has a `ConsulBootstrapConfiguration` class configured in the `META-INF/spring.factories` file that indicates certain beans need to be configured on startup. While bean configuration is automatic, it can be disabled using an environment variable or a bootstrap.yml file like the following:

	consul:
	  enabled: false
	  
The bootstrap configuration creates three components needed to fetch configuration from Consul. `ConsulProperties`, `ConsulAdapter` as well as a `ConsulPropertySourceLocator`. This last locator class implements a Spring Boot `PropertySourceLocator` interface that is automatically detected by the cloud configuration components and makes the property source available to the application context.

Because the bootstrap components are auto-configured, there is no additional code that needs to be written. However, there are a couple of configuration items that you will want to set for yourself. The first is the Consul services endpoint. By default the endpoint is set to `http://localhost:8500/v1`. You can update this using the bootstrap.yml file.

	consul:
	  endpoint: http://consul.service.consul:8500/v1

You will then want to set the paths element to the Consul key-value pairs that you want to make available to the application. By default Consul recursive directory access is used. So any property at or after the specified path will be returned by the locator.

	consul:
	  endpoint: http://consul.service.consul:8500/v1
	  paths:
	    - root/api/defaults
	    - root/global/defaults
	    
The final configuration option available is the `failFast` option. This instructs the application to either fail passively logging the failure, or throw an exception if the property load fails. This option defaults to false, or passive failure.

	consul:
	  endpoint: http://consul.service.consul:8500/v1
	  paths:
	    - root/api/defaults
	    - root/global/defaults
	  failFast: false
	  
After configuration is complete, you should be able to inject the Consul properties using any of the normal injection devices, like `@Value("root.api.defaults.<name>")`.

I also strongly encourage you to take a look at the [Spring Cloud Consul](https://github.com/spring-cloud/spring-cloud-consul) project. This is a brand new project from Spring that includes configuration as well as service registration and discovery. The project has just tagged the first milestone release.

### Non-Boot Configuration

If you are not using Spring Boot, you can still leverage Consul based property injection with the `@EnableConsulPropertySource` annotation in this starter project. Simply annotate your configuration class with the enable annotation and set the `paths` attribute of the annotation.

	@Configuration
	@EnableConsulPropertySource({"root/api/defaults", "root/global/defaults"})
	public class ApplicationConfiguration {
	  ...
	} 
	
This will execute calls to the Consul API to get all properties from the specified paths during application context initialization. This solution also utilizes the same environment variables, `consul.endpoint` and  `consul.failFast` as the Spring Boot solution. You can set these configuration options like any other environment variable, using `-Dconsul.endpoint=http://consul.service.consul:8500/v1` for instance.

 After configuration is complete, you should again be able to inject the Consul properties using any of the normal injection devices, like `@Value("root.api.defaults.<name>")`.