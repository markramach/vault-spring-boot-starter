/**
 * 
 */
package com.flyover.boot.vault.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author mramach
 *
 */
@Configuration
@EnableConfigurationProperties
public class VaultBootstrapConfiguration {
    
    @Autowired
    private Environment environment;
    
    @Bean
    @ConditionalOnProperty(value = "vault.enabled", matchIfMissing = true)
    public VaultConfiguration vaultConfiguration() {
        return new VaultConfiguration();
    }
    
    @Bean 
    @ConditionalOnProperty(value = "vault.enabled", matchIfMissing = true)
    public VaultAdapter vaultAdapter(VaultConfiguration configuration) {
        return new VaultAdapter(configuration);
    }

    @Bean
    @ConditionalOnProperty(value = "vault.enabled", matchIfMissing = true)
    public VaultPropertySourceLocator vaultPropertySourceLocator() {
        return new VaultPropertySourceLocator();
    }
    
}
