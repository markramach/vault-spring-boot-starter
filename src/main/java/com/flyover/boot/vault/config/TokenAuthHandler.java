/**
 * 
 */
package com.flyover.boot.vault.config;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

/**
 * @author mramach
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class TokenAuthHandler implements AuthHandler {

    public String getToken(VaultConfiguration configuration) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, String.format("token=%s", configuration.getToken()));
        headers.set("X-Vault-Token", configuration.getToken());
        
        new RestTemplate().exchange(configuration.getEndpoint() + "/auth/token/lookup-self", HttpMethod.GET, 
                new HttpEntity(headers), Map.class);
        
        return configuration.getToken();
        
    }

}
