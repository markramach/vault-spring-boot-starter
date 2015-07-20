/**
 * 
 */
package com.flyover.boot.vault.config;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * @author mramach
 *
 */
public class VaultBootstrapConfigurationTest {
    
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8200);

    @Before
    public void before() throws Exception {
        stubAuth();
    }
    
    @Test
    public void testVaultPropertySourceLocatorCreated() {

        ConfigurableApplicationContext context = new SpringApplicationBuilder(
                VaultBootstrapConfiguration.class).web(false).run();
        
        assertNotNull("Checking that the Vault adapter is available in the application context.",
                BeanFactoryUtils.beanOfType(context, VaultAdapter.class));
        
        assertNotNull("Checking that the property source locator is available in the application context.",
                BeanFactoryUtils.beanOfType(context, VaultPropertySourceLocator.class));
        
    }
    
    @Test
    public void testVaultPropertiesCreated_WithDefaultProperties() {
        
        ConfigurableApplicationContext context = new SpringApplicationBuilder(
                VaultBootstrapConfiguration.class).web(false).run();
        
        VaultConfiguration properties = BeanFactoryUtils.beanOfType(context, VaultConfiguration.class);
        
        assertNotNull("Checking that the Vault adapter is available in the application context.", properties);
        assertEquals("Checking that the endpoint has been defaulted.", "http://localhost:8200/v1", properties.getEndpoint());
        assertEquals("Checking that the fast fail option has been defaulted.", false, properties.isFailFast());
        
    }
    
    @Test
    public void testVaultPropertiesCreated_WithEnvironmentProperties() {
        
        String endpoint = "http://localhost:8200/v1";
        
        ConfigurableEnvironment environment = new StandardEnvironment() {

            /* (non-Javadoc)
             * @see org.springframework.core.env.StandardEnvironment#customizePropertySources(org.springframework.core.env.MutablePropertySources)
             */
            @Override
            protected void customizePropertySources(MutablePropertySources propertySources) {
                
                super.customizePropertySources(propertySources);
                
                propertySources.addFirst(new MapPropertySource("vault-properties", 
                        Collections.singletonMap("vault.endpoint", endpoint)));
                
            }
            
        };
        
        ConfigurableApplicationContext context = new SpringApplicationBuilder(
                VaultBootstrapConfiguration.class).environment(environment).web(false).run();
        
        VaultConfiguration properties = BeanFactoryUtils.beanOfType(context, VaultConfiguration.class);
        
        assertNotNull("Checking that the Vault adapter is available in the application context.", properties);
        assertEquals("Checking that the endpoint has been defaulted.", endpoint, properties.getEndpoint());
        
    }
    
    private void stubAuth() throws URISyntaxException, IOException {

        Path responsePath = Paths.get(Thread.currentThread().getContextClassLoader().
                getResource("vault/get_auth_token_lookup_self.json").toURI());

        stubFor(get(urlMatching("/v1/auth/token/lookup-self"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(Files.readAllBytes(responsePath))));

    }
    
}
