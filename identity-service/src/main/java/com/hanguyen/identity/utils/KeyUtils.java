package com.hanguyen.identity.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KeyUtils {

    @Getter
    private RSAPublicKey publicKey;

    @Getter
    private RSAPrivateKey privateKey;

    private static final String PUBLIC_KEY_FILE = "app_public_key.der";
    private static final String PRIVATE_KEY_FILE = "app_private_key.der";

    public KeyUtils() {
        manageKeyPair();
    }

    private void manageKeyPair() {
        File publicKeyFile = new File(PUBLIC_KEY_FILE);
        File privateKeyFile = new File(PRIVATE_KEY_FILE);

        if (publicKeyFile.exists() && privateKeyFile.exists()) {
            loadKeyPair(publicKeyFile, privateKeyFile);
        } else {
            generateAndSaveKeyPair(publicKeyFile, privateKeyFile);
        }
    }

    private void generateAndSaveKeyPair(File publicKeyFile, File privateKeyFile) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            this.publicKey = (RSAPublicKey) keyPair.getPublic();
            this.privateKey = (RSAPrivateKey) keyPair.getPrivate();

            try (FileOutputStream fos = new FileOutputStream(publicKeyFile)) {
                fos.write(this.publicKey.getEncoded());
            }

            try (FileOutputStream fos = new FileOutputStream(privateKeyFile)) {
                fos.write(this.privateKey.getEncoded());
            }

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadKeyPair(File publicKeyFile, File privateKeyFile) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            this.publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            this.privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public JWKSet jwkSet() {
        RSAKey.Builder builder = new RSAKey.Builder(publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("bookteria-auth-key-id");
        return new JWKSet(builder.build());
    }
}