/**
 * 
 */
package com.flyover.boot.vault.config;

/**
 * @author mramach
 *
 */
public interface AuthHandler {

    String getToken(VaultConfiguration configuration);

    default void revoke(VaultConfiguration configuration, String token) {}

}
