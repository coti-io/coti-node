package io.coti.basenode.services.interfaces;

import java.security.cert.X509Certificate;

public interface ISslService {
    void init();

    void checkServerTrusted(X509Certificate[] serverCertificates);
}
