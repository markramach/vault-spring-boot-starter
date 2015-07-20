/**
 * 
 */
package com.flyover.boot.vault.config;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

/**
 * @author mramach
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class VaultPropertySourceLocatorTest {

    @Spy
    private VaultConfiguration configuration = new VaultConfiguration();
    @Mock
    private VaultAdapter vaultAdapter;
    @InjectMocks
    private VaultPropertySourceLocator locator;
    
    @Test
    public void testLocate() {

        configuration.setPaths(Arrays.asList("path/to/property"));
        
        when(vaultAdapter.auth()).thenReturn("token");
        when(vaultAdapter.getValue(isA(String.class), isA(String.class))).thenReturn("Hello World!");
        
        PropertySource<?> propertySource = locator.locate(new StandardEnvironment());
        
        assertNotNull("Checking that a non-null property sources was returned.", propertySource);
        
        assertTrue("Checking that the property source contains a property.", 
                propertySource.containsProperty("path.to.property"));
        
        assertEquals("Checking that the property source contains the expected value.", 
                "Hello World!", propertySource.getProperty("path.to.property"));
        
    }

}
