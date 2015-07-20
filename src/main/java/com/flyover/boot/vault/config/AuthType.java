/**
 * 
 */
package com.flyover.boot.vault.config;

import java.util.function.Supplier;

/**
 * @author mramach
 *
 */
public enum AuthType {
    
    APPID(AppIdAuthHandler::new),
    TOKEN(TokenAuthHandler::new);

    private AuthHandler handler;
    
    private AuthType(Supplier<? extends AuthHandler> supplier) {
        handler = supplier.get();
    }

    public AuthHandler getHandler() {
        return handler;
    }
    
}
