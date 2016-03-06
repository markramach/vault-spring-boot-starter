/**
 * 
 */
package com.flyover.boot.vault.config;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * @author mramach
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class VaultAdapter {
    
    @Autowired
    private VaultConfiguration configuration;
    
    public VaultAdapter(VaultConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getValue(String token, String path) {
        
        HttpHeaders headers = createHeaders(token);
        
        try {
            ResponseEntity<Map> res = new RestTemplate().exchange(
                    configuration.getEndpoint() + "/{mount}/{path}", HttpMethod.GET, new HttpEntity(headers), Map.class, 
                            configuration.getMount(), path);
            
            
            Map<String, Object> data = (Map<String, Object>) res.getBody().get("data");
            
            return (String) data.get("value");
            
        } catch (HttpClientErrorException e) {
            
            if(e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return null;
            } else {
                throw e;
            }
            
        }
        
    }
    
    public void setValue(String token, String path, String value) {
        
        HttpHeaders headers = createHeaders(token);
        
        new RestTemplate().exchange(configuration.getEndpoint() + "/{mount}/{path}", HttpMethod.POST, 
                new HttpEntity(Collections.singletonMap("value", value), headers), Void.class, 
                        configuration.getMount(), path);
        
    }

    public String auth() {
        return configuration.getAuthType().getHandler().getToken(configuration);
    }
    
    public void revoke(String token) {
        configuration.getAuthType().getHandler().revoke(configuration, token);
    }
    
    public void addUserId(String token, String appId, String userId) {
        
        HttpHeaders headers = createHeaders(token);
        
        new RestTemplate().exchange(configuration.getEndpoint() + "/auth/app-id/map/user-id/{userId}", HttpMethod.POST, 
                new HttpEntity(Collections.singletonMap("value", appId), headers), Map.class, userId);
        
    }

    private HttpHeaders createHeaders(String token) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, String.format("token=%s", token));
        headers.set("X-Vault-Token", token);
        
        return headers;
        
    }
    
}
