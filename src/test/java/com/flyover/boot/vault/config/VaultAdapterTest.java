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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * @author mramach
 *
 */
public class VaultAdapterTest {
    
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8200);

    private VaultConfiguration configuration = new VaultConfiguration();
    private VaultAdapter adapter;
    
    @Before
    public void before() {
        
        adapter = new VaultAdapter(configuration);
        
    }
    
    @Test
    public void testGetValue() throws Exception {
        
        stubGetValue();
        
        String value = adapter.getValue("ebf1abbc-9a04-7534-7b21-76d240d41f1a", "foo");
        
        verify(getRequestedFor(urlMatching("/v1/secret/foo"))
                .withHeader("Cookie", equalTo("token=ebf1abbc-9a04-7534-7b21-76d240d41f1a"))
                .withHeader("X-Vault-Token", equalTo("ebf1abbc-9a04-7534-7b21-76d240d41f1a")));
        
        assertNotNull("Checking that the value is not null.", value);
        
    }
    
    @Test
    public void testSetValue() throws Exception {
        
        stubSetValue();
        
        adapter.setValue("ebf1abbc-9a04-7534-7b21-76d240d41f1a", "foo", "value");
        
        verify(postRequestedFor(urlMatching("/v1/secret/foo"))
                .withHeader("Cookie", equalTo("token=ebf1abbc-9a04-7534-7b21-76d240d41f1a"))
                .withHeader("X-Vault-Token", equalTo("ebf1abbc-9a04-7534-7b21-76d240d41f1a"))
                .withRequestBody(equalToJson("{\"value\":\"value\"}")));
        
    }
    
    @Test
    public void testLogin() throws Exception {
        
        configuration.setAuthType(AuthType.APPID);
        configuration.setAppId("APP_ID");
        configuration.setKeyStore(new ClassPathResource("hmac.jks"));
        configuration.setKeyStorePassword("password");
        configuration.setKeyAlias("vault");
        configuration.setKeyPassword("password");

        String userId = new MacService().createMac(configuration);
        
        stubLogin();
        
        String result = adapter.auth();
        
        verify(postRequestedFor(urlMatching("/v1/auth/app-id/login"))
                .withRequestBody(equalToJson("{\"app_id\":\"APP_ID\",\"user_id\":\"" + userId + "\"}")));
        
        assertEquals("Checking that the token was returned.", "ebf1abbc-9a04-7534-7b21-76d240d41f1a", result);
        
    }
    
    @Test
    public void testPushAuth() throws Exception {
        
        stubPushAuth();
        
        adapter.addUserId("ebf1abbc-9a04-7534-7b21-76d240d41f1a", "APP_ID", "USER_ID");
        
        verify(postRequestedFor(urlMatching("/v1/auth/app-id/map/user-id/USER_ID"))
                .withRequestBody(equalToJson("{\"value\":\"APP_ID\"}")));
        
    }

    private void stubLogin() throws URISyntaxException, IOException {

        Path responsePath = Paths.get(Thread.currentThread().getContextClassLoader().
                getResource("vault/post_login_response.json").toURI());

        stubFor(post(urlMatching("/v1/auth/app-id/login"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(Files.readAllBytes(responsePath))));

    }
    
    @Test
    public void testRevoke() throws Exception {
        
        configuration.setAuthType(AuthType.APPID);
        
        stubRevoke();
        
        try {
            
            // PUT operation always fails the first time with mockito.
            adapter.revoke("ebf1abbc-9a04-7534-7b21-76d240d41f1a");
            
        } catch (Exception e) {
            
            adapter.revoke("ebf1abbc-9a04-7534-7b21-76d240d41f1a");
            
        }
        
        verify(putRequestedFor(urlMatching("/v1/auth/token/revoke/.*")));
        
    }

    private void stubGetValue() throws URISyntaxException, IOException {

        Path responsePath = Paths.get(Thread.currentThread().getContextClassLoader().
                getResource("vault/get_value_response.json").toURI());

        stubFor(get(urlMatching("/v1/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(Files.readAllBytes(responsePath))));

    }
    
    private void stubSetValue() throws URISyntaxException, IOException {

        stubFor(post(urlMatching("/v1/.*"))
                .willReturn(aResponse()
                        .withStatus(200)));

    }

    private void stubPushAuth() throws URISyntaxException, IOException {

        stubFor(post(urlMatching("/v1/auth/app-id/map/user-id/.*"))
                .willReturn(aResponse()
                        .withStatus(200)));

    }
    
    private void stubRevoke() throws URISyntaxException, IOException {

        stubFor(put(urlMatching("/v1/auth/token/revoke/.*"))
                .willReturn(aResponse()
                        .withStatus(204)));

    }

}
