package com.flyover.boot.vault.config;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.jar.JarEntry;

import javax.crypto.Mac;

import org.springframework.core.io.Resource;

/**
 * @author mramach
 *
 */
public class MacService {
    
    public String createMac(VaultConfiguration configuration) {
        
        Mac mac = initializeMac(configuration);
        URLClassLoader cl = (URLClassLoader)MacService.class.getClassLoader();
        
        Arrays.stream(cl.getURLs())
            .sorted((o1, o2) -> o1.toString().compareTo(o2.toString()))
                .forEach(i -> processUrl(mac, i));
        
        return Base64.getEncoder().encodeToString(mac.doFinal()).replaceAll("[^A-Za-z0-9 ]", "0");
        
    }
    
    private void processUrl(Mac mac, URL i) {
        
        try {
            
            updateMac(mac, Paths.get(i.toURI()));
        
        } catch (FileSystemNotFoundException e) {

            processJar(mac, i);
            
        } catch (Exception e) {
            System.out.println(String.format("Unable to process resource %s : %s", i, e.getMessage()));
        }
        
    }
    
    private void processJar(Mac mac, URL i) {
        
        try {
            
            JarURLConnection jc = (JarURLConnection)i.openConnection();
            
            Collections.list(jc.getJarFile().entries()).stream()
                .sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).forEach(je -> {
            
                    processJarEntry(mac, jc, je);
            
            });
        
        } catch (Exception e) {
            System.out.println(String.format("Unable to process jar %s : %s", i, e.getMessage()));
        }
        
    }
    
    private void processJarEntry(Mac mac, JarURLConnection jc, JarEntry je) {
        
        try {
            
            InputStream in = jc.getJarFile().getInputStream(je);
            byte[] buf = new byte[in.available()];
            in.read(buf);
            
            mac.update(buf);
            
        } catch (Exception e) {
            System.out.println(String.format("Unable to process jar entry %s : %s", je.getName(), e.getMessage()));
        }
        
    }

    private Mac initializeMac(VaultConfiguration configuration) {
        
        KeyStore keyStore;
        Key key;
        Mac mac;
        
        try {
            
            Resource keyStoreResource = configuration.getKeyStore();
            keyStore = KeyStore.getInstance("jceks");
            keyStore.load(keyStoreResource.getInputStream(), configuration.getKeyStorePassword().toCharArray());
            key = keyStore.getKey(configuration.getKeyAlias(), configuration.getKeyPassword().toCharArray());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed while attempting to load HMAC key store.", e);
        }
        
        try {
            
            mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            
        } catch (Exception e) {
            throw new RuntimeException("Faile while attempting to create Mac instance.", e);
        }
        
        return mac;
        
    }
    
    private void updateMac(Mac mac, Path p) {
        
        if(p.toFile().isDirectory()) {

            Arrays.stream(p.toFile().listFiles()).sorted((o1, o2) -> o1.toString().compareTo(o2.toString())).forEach(f -> {
                updateMac(mac, f.toPath());
            });

        } else {

            try {

                mac.update(Files.readAllBytes(p));

            } catch (Exception e) {
                System.out.println(p.toAbsolutePath().toString());
            }

        }
            
    }

}
