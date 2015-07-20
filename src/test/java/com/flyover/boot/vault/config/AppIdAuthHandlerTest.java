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

import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * @author mramach
 *
 */
public class AppIdAuthHandlerTest {
    
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8200);

    @Test
    public void testGetToken() throws Exception {
        
        VaultConfiguration configuration = new VaultConfiguration();
        configuration.setAuthType(AuthType.APPID);
        configuration.setAppId("APP_ID");
        configuration.setKeyStore(new ClassPathResource("hmac.jks"));
        configuration.setKeyStorePassword("password");
        configuration.setKeyAlias("vault");
        configuration.setKeyPassword("password");

        String userId = new MacService().createMac(configuration);
        
        stubLogin();
        
        String token = new AppIdAuthHandler().getToken(configuration);
        
        verify(postRequestedFor(urlMatching("/v1/auth/app-id/login"))
                .withRequestBody(equalToJson("{\"app_id\":\"APP_ID\",\"user_id\":\"" + userId + "\"}")));
        
        assertEquals("Checking that the token was returned.", "ebf1abbc-9a04-7534-7b21-76d240d41f1a", token);
        
    }
    
    @Test
    public void testGetToken_WithUserIdAuth() throws Exception {
        
        VaultConfiguration configuration = new VaultConfiguration();
        configuration.setAuthType(AuthType.APPID);
        configuration.setAppId("APP_ID");
        configuration.setKeyStore(new ClassPathResource("hmac.jks"));
        configuration.setKeyStorePassword("password");
        configuration.setKeyAlias("vault");
        configuration.setKeyPassword("password");
        configuration.setPushUserId(true);
        configuration.setToken("asdfasdf-asdf-asdf-asdf-asdfasdfasdf");
        
        String userId = new MacService().createMac(configuration);
        
        stubPushAuth();
        stubLogin();
        
        String token = new AppIdAuthHandler().getToken(configuration);
        
        verify(postRequestedFor(urlMatching("/v1/auth/app-id/map/user-id/" + userId))
                .withRequestBody(equalToJson("{\"value\":\"APP_ID\"}")));
        
        verify(postRequestedFor(urlMatching("/v1/auth/app-id/login"))
                .withRequestBody(equalToJson("{\"app_id\":\"APP_ID\",\"user_id\":\"" + userId + "\"}")));
        
        assertNotNull("Checkng that the token is not null.", token);
        
    }
    
    @Test
    public void testRevoke() throws Exception {
        
        VaultConfiguration configuration = new VaultConfiguration();
        configuration.setAuthType(AuthType.APPID);
        configuration.setAppId("APP_ID");
        configuration.setKeyStore(new ClassPathResource("hmac.jks"));
        configuration.setKeyStorePassword("password");
        configuration.setKeyAlias("vault");
        configuration.setKeyPassword("password");
        
        stubRevoke();
        
        try {
            
            new AppIdAuthHandler().revoke(configuration, "ebf1abbc-9a04-7534-7b21-76d240d41f1a");
            
        } catch (Exception e) {
            
            new AppIdAuthHandler().revoke(configuration, "ebf1abbc-9a04-7534-7b21-76d240d41f1a");
            
        }
        
        verify(putRequestedFor(urlMatching("/v1/auth/token/revoke/.*")));
        
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
