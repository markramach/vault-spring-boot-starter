/**
 * 
 */
package com.flyover.boot.vault.config;

import java.util.LinkedList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * @author mramach
 *
 */
@ConfigurationProperties(VaultConfiguration.PREFIX)
public class VaultConfiguration {
    
    public static final String PREFIX = "vault";
    
    private String endpoint = "http://localhost:8200/v1";
    private boolean failFast = false;
    private List<String> paths = new LinkedList<String>();
    private AuthType authType = AuthType.TOKEN;
    
    // Authentication properties
    private String appId;
    private String token;
    private boolean pushUserId = false;
    
    // HMAC Settings
    private Resource keyStore;
    private String keyStorePassword;
    private String keyAlias;
    private String keyPassword;

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return the failFast
     */
    public boolean isFailFast() {
        return failFast;
    }

    /**
     * @param failFast the failFast to set
     */
    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    /**
     * @return the paths
     */
    public List<String> getPaths() {
        return paths;
    }

    /**
     * @param paths the paths to set
     */
    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    /**
     * @return the appId
     */
    public String getAppId() {
        return appId;
    }

    /**
     * @param appId the appId to set
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * @return the authType
     */
    public AuthType getAuthType() {
        return authType;
    }

    /**
     * @param authType the authType to set
     */
    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return the keyStore
     */
    public Resource getKeyStore() {
        return keyStore;
    }

    /**
     * @param keyStore the keyStore to set
     */
    public void setKeyStore(Resource keyStore) {
        this.keyStore = keyStore;
    }

    /**
     * @return the keyStorePassword
     */
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * @param keyStorePassword the keyStorePassword to set
     */
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * @return the keyAlias
     */
    public String getKeyAlias() {
        return keyAlias;
    }

    /**
     * @param keyAlias the keyAlias to set
     */
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    /**
     * @return the keyPassword
     */
    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * @param keyPassword the keyPassword to set
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * @return the pushUserId
     */
    public boolean isPushUserId() {
        return pushUserId;
    }

    /**
     * @param pushUserId the pushUserId to set
     */
    public void setPushUserId(boolean pushUserId) {
        this.pushUserId = pushUserId;
    }

}
