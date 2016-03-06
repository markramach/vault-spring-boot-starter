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

import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * @author mramach
 *
 */
public class TokenAuthHandlerTest {
    
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8200);

    private VaultConfiguration configuration = new VaultConfiguration();
    
    @Test
    public void testGetToken() throws Exception {
        
        configuration.setToken("ebf1abbc-9a04-7534-7b21-76d240d41f1a");
        
        stubAuth();
        
        String result = AuthType.TOKEN.getHandler().getToken(configuration);
        
        verify(getRequestedFor(urlMatching("/v1/auth/token/lookup-self")));
        
        assertEquals("Checking that the token was returned.", "ebf1abbc-9a04-7534-7b21-76d240d41f1a", result);
        
    }
    
    private void stubAuth() throws URISyntaxException, IOException {

        Path responsePath = Paths.get(Thread.currentThread().getContextClassLoader().
                getResource("vault/get_auth_token_lookup_self.json").toURI());

        stubFor(get(urlMatching("/v1/auth/token/lookup-self"))
                .withHeader("Cookie", equalTo("token=ebf1abbc-9a04-7534-7b21-76d240d41f1a"))
                .withHeader("X-Vault-Token", equalTo("ebf1abbc-9a04-7534-7b21-76d240d41f1a"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(Files.readAllBytes(responsePath))));

    }

}
