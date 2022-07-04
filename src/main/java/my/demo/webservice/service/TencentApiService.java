package my.demo.webservice.service;

import com.tls.tls_sigature.tls_sigature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TencentApiService {

    public String genTestUserSig(String userId) {
        String sig = null;
        try {
            tls_sigature.GenTLSSignatureResult result = tls_sigature.genSig(123, userId,
                    "-----BEGIN PRIVATE KEY-----\n" +
                            "example" +
                            "-----END PRIVATE KEY-----");
            sig = result.urlSig;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sig;
    }

    public String genProUserSig(String userId) {
        String sig = null;
        try {
            tls_sigature.GenTLSSignatureResult result = tls_sigature.genSig(1234, userId,
                    "-----BEGIN PRIVATE KEY-----\n" +
                            "example" +
                            "-----END PRIVATE KEY-----");
            sig = result.urlSig;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sig;
    }
}
