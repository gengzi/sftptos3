package com.gengzi;

import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.Collection;
import java.util.Collections;

public class DynamicKeyPairProvider implements KeyPairProvider {
    private KeyPair loadFromSecureStorage() {
        // 实现从安全存储加载密钥的逻辑
        // ...
        return null;
    }

    /**
     * Load available keys.
     *
     * @param session The {@link SessionContext} for invoking this load command - may be {@code null}
     *                if not invoked within a session context (e.g., offline tool or session unknown).
     * @return an {@link Iterable} instance of available keys - ignored if {@code null}
     * @throws IOException              If failed to read/parse the keys data
     * @throws GeneralSecurityException If failed to generate the keys
     */
    @Override
    public Iterable<KeyPair> loadKeys(SessionContext session) throws IOException, GeneralSecurityException {
        // 从安全存储（如Vault、数据库）动态加载密钥对
        // 示例：加载最新的RSA密钥
        KeyPair latestKey = loadFromSecureStorage();
        return Collections.singletonList(latestKey);
    }
}