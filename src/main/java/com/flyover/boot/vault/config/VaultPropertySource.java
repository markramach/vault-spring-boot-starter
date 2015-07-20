/**
 * 
 */
package com.flyover.boot.vault.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;

/**
 * @author mramach
 *
 */
public class VaultPropertySource extends EnumerablePropertySource<Map<String, Object>> {
    
    private static final Logger LOG = LoggerFactory.getLogger(VaultPropertySourceLocator.class);
    
    private Map<String, Object> source = new LinkedHashMap<String, Object>();

    public VaultPropertySource(VaultConfiguration configuration, VaultAdapter adapter) {
        
        super("vault-property-source-" + UUID.randomUUID().toString());
        
        String token = adapter.auth();
        
        try {
            
            configuration.getPaths().stream()
                .forEach(p -> setProperty(p, adapter.getValue(token, p)));
            
        } catch (RuntimeException e) {
            
            if(configuration.isFailFast()) {
                throw e;
            }
            
            LOG.info("Unable to fetch properties from resource {}.", configuration.getEndpoint());
            
        } finally {
            
            adapter.revoke(token);
            
        }
        
    }

    private Object setProperty(String key, String value) {
        return source.put(pathToProperty(key), value);
    }
    
    private String pathToProperty(String path) {
        return path.replace('/', '.');
    }
    
    @Override
    public String[] getPropertyNames() {
        return source.keySet().toArray(new String[source.size()]);
    }

    @Override
    public Object getProperty(String name) {
        return source.get(name);
    }

}
