/**
 * 
 */
package com.flyover.boot.vault.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * @author mramach
 *
 */
public class VaultPropertySourceLocator implements PropertySourceLocator {
    
    @Autowired
    private VaultConfiguration configuration;
    @Autowired
    private VaultAdapter consulAdapter;
    
    @Override
    public PropertySource<?> locate(Environment environment) {
        return new VaultPropertySource(configuration, consulAdapter);
    }

}
