/**
 * 
 */
package com.flyover.boot.vault.config;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;

import javax.crypto.KeyGenerator;

import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;

/**
 * @author mramach
 *
 */
public class MacServiceTest {
    
    @Test
    public void testCreateMac() throws Exception {
        
        VaultConfiguration config = new VaultConfiguration();
        config.setKeyStore(new ByteArrayResource(createKeyStore("password", "vault")));
        config.setKeyStorePassword("password");
        config.setKeyAlias("vault");
        config.setKeyPassword("password");
        
        String mac = new MacService().createMac(config);
        
        assertNotNull("Checking that the MAC value is not null.", mac);
        assertEquals("Checking that the MAC value is consistently generated.", mac, new MacService().createMac(config));
        
    }
    
    private byte[] createKeyStore(String password, String keyAlias) throws Exception {
        
        KeyStore keyStore = KeyStore.getInstance("jceks");
        keyStore.load(null, null);
        
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        
        Key key = keyGen.generateKey();
        keyStore.setKeyEntry(keyAlias, key, password.toCharArray(), new Certificate[0]);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        keyStore.store(buf, password.toCharArray());
        
        return buf.toByteArray();
        
    }

}
