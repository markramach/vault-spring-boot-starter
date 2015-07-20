/**
 * 
 */
package com.flyover.boot.vault.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * @author mramach
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class AppIdAuthHandler implements AuthHandler {

    public String getToken(VaultConfiguration configuration) {
        
        Assert.hasLength(configuration.getAppId(), 
                "appId is a required configuration item for App Id authentication.");
        
        Assert.notNull(configuration.getKeyStore(), 
                "keyStore is a required configuration item for App Id authentication.");
        
        Assert.hasLength(configuration.getKeyStorePassword(), 
                "keyStorePassword is a required configuration item for App Id authentication.");
        
        Assert.hasLength(configuration.getKeyAlias(), 
                "keyAlias is a required configuration item for App Id authentication.");
        
        Assert.hasLength(configuration.getKeyPassword(), 
                "keyPassword is a required configuration item for App Id authentication.");
        
        String userId = new MacService().createMac(configuration);
        
        // Push the user id to vault if requested.
        pushUserIdIfRequired(configuration, userId);
        
        // Authenticate with Vault to fetch a token.
        Map<String, String> req = new LinkedHashMap<String, String>();
        req.put("app_id", configuration.getAppId());
        req.put("user_id", userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<Map<String,String>>(req, headers);
        
        try {
            
            ResponseEntity<Map> res = new RestTemplate().exchange(
                    configuration.getEndpoint() + "/auth/app-id/login", HttpMethod.POST, entity, Map.class);
            
            Map<String, Object> auth = (Map<String, Object>) res.getBody().get("auth");
            String token = (String) auth.get("client_token");
            
            return token;
            
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Vault App ID authentication failed.", e);
        }
        
    }
    
    private void pushUserIdIfRequired(VaultConfiguration configuration, String userId) {
        
        if(!configuration.isPushUserId()) {
            return;
        }
        
        Assert.hasLength(configuration.getAppId(), 
                "appId is a required configuration item to push a user ID to vault.");
        
        Assert.hasLength(configuration.getToken(), 
                "token is a required configuration item to push a user ID to vault.");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, String.format("token=%s", configuration.getToken()));
        
        new RestTemplate().exchange(configuration.getEndpoint() + "/auth/app-id/map/user-id/{userId}", HttpMethod.POST, 
                new HttpEntity(Collections.singletonMap("value", configuration.getAppId()), headers), Map.class, userId);
        
    }

    public void revoke(VaultConfiguration configuration, String token) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, String.format("token=%s", token));
        
        new RestTemplate().exchange(configuration.getEndpoint() + "/auth/token/revoke/{token}", HttpMethod.PUT, 
                new HttpEntity(headers), Void.class, token);
        
    }

}
