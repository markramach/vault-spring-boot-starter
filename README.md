## spring-boot-starter-vault

As we look to build highly scalable applications in the cloud, we commonly struggle with the task of securing our passwords, API keys and other critical secrets. How do we allow our applications to access these secrets in a scalable and secure manner without distributing the secrets themselves? One possible solution is a secure secret storage tool like [Vault](https://www.vaultproject.io/). Vault provides secure secret storage with a variety of persistent storage options. The Vault product also provides a complete REST API for configuration and access to your secrets. This project will leverage these REST operations to make the injection of Vault secret properties into a Spring Boot application as simple as possible.

### Getting Started

The easiest way to get started with this starter project is to fork, clone or download this repository.

	git clone https://github.com/markramach/spring-boot-starter-vault.git  
	
You will also need to install [Vault](https://www.vaultproject.io/docs/install/index.html).

Once installed, you can start a development server using the following command.

	vault server -dev
	
At this point you should have running Vault server running locally. Because the `-dev` argument was specified, the server instance is initialized and unsealed. In a typical production installation this will not be the case. You will need to follow the procedure to initialize and unseal the vault demonstrated in the [Basic Concepts](https://www.vaultproject.io/docs/concepts/index.html) documentation.

### Spring Boot Configuration

If you're developing a Spring Boot application, you can have properties injected into the application context during the bootstrap phase of application startup. Injecting properties at this point allows you to leverage many of the features that were recently released from the [Spring Cloud Config](http://projects.spring.io/spring-cloud/) project. This includes the ability to refresh your properties at any point during the application lifecycle using the predefined `/refresh` endpoint and the `@RefreshScope` annotation.

This starter project has a `VaulBootstrapConfiguration` class configured in the `META-INF/spring.factories` file that indicates certain beans need to be configured on startup. While bean configuration is automatic, it can be disabled using an environment variable or a bootstrap.yml file like the following:

	vault:
	  enabled: false
	  
The bootstrap configuration creates three components needed to fetch configuration from Vault. `VaultConfiguration`, `VaultAdapter` as well as a `VaultPropertySourceLocator`. This last locator class implements a Spring Boot `PropertySourceLocator` interface that is automatically detected by the cloud configuration components and makes the property source available to the application context.

Because the bootstrap components are auto-configured, there is no additional code that needs to be written. However, there are a couple of configuration items that you will want to set for yourself. The first is the Vault services endpoint. By default the endpoint is set to `http://localhost:8200/v1`. You can update this using the bootstrap.yml file.

	vault:
	  endpoint: http://hostname:port/v1

You will then want to set the paths element to the Vault key-value pairs that you want to make available to the application.

	vault:
	  endpoint: http://hostname:port/v1
	  paths:
	    - path/to/secret1
	    - path/to/secret2
	    
The final configuration option available is the `failFast` option. This instructs the application to either fail passively logging the failure, or throw an exception if the property load fails. This option defaults to false, or passive failure.

	vault:
	  endpoint: http://hostname:port/v1
	  paths:
	    - path/to/secret1
	    - path/to/secret2
	  failFast: false

### Token Authentication

The most straightforward way to authenticate with Vault is the use of pre-generated token. Depending on the permissions inherited through the token chain, this will grant you access to read Vault properties. For this example, you can generate a token using the Vault dev server by issuing the following command.

	vault token-create
	
This should return a token that can then be used for authentication. For example: 

	0c6c3fce-09ee-72aa-fd5e-4d6f1152b002

To test the token, you can execute the following command.

	vault auth 0c6c3fce-09ee-72aa-fd5e-4d6f1152b002

You should see output similar to the following.

	Successfully authenticated! The policies that are associated
	with this token are listed below:
	
	root

Now you can configure token based authentication in the bootstrap.yml file using the authentication type configuration property.

	vault:
	  endpoint: http://hostname:port/v1
	  paths:
	    - path/to/secret1
	    - path/to/secret2
	  failFast: false
	  authType: TOKEN
	  token: 0c6c3fce-09ee-72aa-fd5e-4d6f1152b002

Note that `TOKEN` is actually the default authentication type and will be used if not specified. However, for clarity it is shown above. Also note that any configuration item can be specified on the command line when starting the Spring Boot application and is recommended if using token base authentication. For example:

	java -jar spring-boot-app.jar --vault.token=0c6c3fce-09ee-72aa-fd5e-4d6f1152b002

After configuration is complete, you should be able to inject the Vault properties using any of the normal injection devices, like `@Value("path.to.secret1")`.

### App ID Authentication

An alternate authentication option is [App ID](https://www.vaultproject.io/docs/auth/app-id.html). This authentication strategy uses 2 pieces of information, an application ID and a user ID. Typically the user ID is only available to the host running the application. This starter project creates a HMAC value using all the resources from the application classpath to dynamically create that unique identifier. The user ID is then pushed to vault with the corresponding application ID allowing this version of our application access to secrets.

In order to use this authentication type, let first create a token that can be used to push the application ID and user ID to vault. 

	vault token-create
	
This should return a token that can then be used for authentication. For example: 

	0c6c3fce-09ee-72aa-fd5e-4d6f1152b002

To test the token, you can execute the following command.

	vault auth 0c6c3fce-09ee-72aa-fd5e-4d6f1152b002

You should see output similar to the following.

	Successfully authenticated! The policies that are associated
	with this token are listed below:
	
	root
	
Next we need to enable App ID authentication.

	vault auth-enable app-id

We can now push our application ID to Vault.

	vault write auth/app-id/map/app-id/sample-application-id value=root display_name=sample-application-id

Now you can configure App ID based authentication in the bootstrap.yml file using the authentication type configuration property. Additionally, there are a few items that will need to be specified to generate the HMAC value for the application. Specifically, you will need to provide access to a valid java keystore and AES key that can be used to generate a secure HMACSHA256 value. To generate a keystore you can use the following command.

	keytool -genseckey -alias vault -keyalg AES -keysize 256 -keypass password -storetype jceks -storepass password -keystore hmac.jks

The additional configuration elements are shown below.

	vault:
	  endpoint: http://hostname:port/v1
	  paths:
	    - path/to/secret1
	    - path/to/secret2
	  failFast: false
	  authType: APPID
	  appId: sample-application-id
	  keyStore: file:/path/to/hmac.jks
	  keyStorePassword: password
	  keyAlias: vault
	  keyPassword: password

Again, note that any configuration item can be specified on the command line when starting the Spring Boot application and is recommended when specifying the appId. For example:

	java -jar spring-boot-app.jar --vault.appId=sample-application-id
	
At this point if you attempt to start the application, you should see an error indicating `Vault App ID authentication failed.` This indicates everything is configured properly. In order to grant the application access, we need to push the generated user ID to Vault indicating that the application ID and user ID are a valid combination. We can have the application push this for us using the following arguments.

	 java -jar spring-boot-app.jar --vault.pushUserId=true --vault.token=0c6c3fce-09ee-72aa-fd5e-4d6f1152b002
	 
These arguments establish that you are granting this distinct version of the application access to Vault. The `vault.token` provided should have permissions to write to Vault. From this point on you should not need to provide these arguments as long as the application source is not changed in any way. If the application source is modified, a different user ID will be generated and application will fail to authenticate.

After configuration is complete, you should be able to inject the Vault properties using any of the normal injection devices, like `@Value("path.to.secret1")`.

I have found that this particular strategy is very powerful when deploying to cloud platforms where you may be spinning up and shutting down virtual machines to meet capacity demands.

### Additional Configuration

`vault.mount` - Once you have some familiarity with Vault you will notice you can create any number of backend mount points. A mount is a path extension that corresponds to a persistent storage solution. You can specify the mount point configuration using the vault.mount configuration value on the command line. Or, you can specify it in the bootstrap.yml file like the following.

	vault:
	  endpoint: http://hostname:port/v1
	  paths:
	    - path/to/secret1
	    - path/to/secret2
	  failFast: false
	  authType: APPID
	  appId: sample-application-id
	  keyStore: file:/path/to/hmac.jks
	  keyStorePassword: password
	  keyAlias: vault
	  keyPassword: password
	  mount: secret
	  
Note that the mount value will default to `secret` when not provided.