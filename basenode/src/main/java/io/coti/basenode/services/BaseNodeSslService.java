package io.coti.basenode.services;

import io.coti.basenode.exceptions.SslServiceException;
import io.coti.basenode.services.interfaces.ISslService;
import org.springframework.stereotype.Service;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BaseNodeSslService implements ISslService {

    private final List<X509TrustManager> trustManagers = new ArrayList<>();

    @Override
    public void init() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            trustManagerFactory.init((KeyStore) null);

            for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {

                if (trustManager instanceof X509TrustManager) {
                    trustManagers.add((X509TrustManager) trustManager);
                }
            }
        } catch (Exception e) {
            throw new SslServiceException("Error getting ssl trust managers", e);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] serverCertificates) {
        trustManagers.forEach(trustManager -> {
            try {
                trustManager.checkServerTrusted(serverCertificates, "RSA");
            } catch (CertificateException e) {
                throw new SslServiceException("Untrusted CA", e);
            }
        });
    }

}
