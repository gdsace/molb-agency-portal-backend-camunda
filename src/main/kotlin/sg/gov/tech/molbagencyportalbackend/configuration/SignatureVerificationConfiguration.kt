package sg.gov.tech.molbagencyportalbackend.configuration

import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.util.X509CertUtils
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.security.SecretUtils
import java.security.interfaces.RSAPublicKey

@ConfigurationProperties(prefix = "signature-verification")
@ConstructorBinding
@ExcludeFromGeneratedCoverageTest
data class SignatureVerificationConfiguration(
    val domains: List<DomainCredentials>
) {
    val domainCredentialMap: Map<String, DomainCredentials> = domains.associateBy { it.appId }

    data class DomainCredentials(
        val appId: String,
        val publicKey: String
    ) {
        val authorizationHeaderValidationCert: RSAPublicKey = RSAKey.parse(
            X509CertUtils.parse(SecretUtils.toPEMCert(publicKey))
        ).toRSAPublicKey()
    }
}
