package sg.gov.tech.molbagencyportalbackend

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.util.X509CertUtils
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import sg.gov.tech.molbagencyportalbackend.auth.AuthorizationHeaderGenerator
import sg.gov.tech.molbagencyportalbackend.util.CommonUtil
import sg.gov.tech.security.SecretUtils
import sg.gov.tech.security.service.AuthorizationHeaderVerifier
import sg.gov.tech.testing.MolbUnitTesting
import java.security.interfaces.RSAPublicKey

@MolbUnitTesting
class AuthHeaderTest {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    companion object {
        private const val PRIVATE_KEY =
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCyB4+K1taBRo1sLnrQPeGnf9/FtjjTnRUkDqP6wPkMxt3WzGLIZdd4rQLRUsTvdySfAnOaarO5bEeuq5UjeT0SsomZz+4TdZzCvR2zXRYnoXz5emGpgO/UPseGcgn4OlrWkSyqW6a54c/qLXUh2jsLM+idWVvEDmzYc+prusrxmGP1MIuuql9LDG8B1U07YwfSUJwTqG3xD86N6dL/VYwX+ZzlURkwLnBAkDKoRi7qmuDzXg04WUccIBAe4IjLCrTFAQYgeQBzRcBcOehNrBZenJ5MWL23hvH/wXA5Ch2Vv0BUyWQgOorEjAoSKqv04w9xiPdylLnJGzlgS2OJvfsHAgMBAAECggEAIbKmq/wQbDXjjQebPZGJBgCBZdYhGYURPFr9FPiz64Q093SGejI0v+XXs7F8D2u45dQ7942c6UsiomBeAXHwanqa6x9djBdmDcJoPcwv5l593mcY11HNk5oygDNTycWZ7iValAB3JZYlmcjy8+5v4KX5sNb6NLkaRzphs/SOd341CrPS9pqlXaYPnXd2r+vOMXXA3mfClUc1KCJgSd7tkWb1oMClqLFTyKy6y8SPryhNk4d8RfZXFf6Vv3IzqUjMhdM6k+KKr85xcT7Vh55ZKMk5SMqfL7+cRTSb9hsFyUcgUNthi+/z7YEf9/tlXSC7pJKoZDdviF4x2ndjC7BxeQKBgQDb6lllJKRXsM0pIYVH6ijUuYwx3cvabVSHbBpDB6ByJ27Mu7KEh4/9mSSwhKiB5x8mVpBSqkd2K111c80OeaBGmmSDudgzeeD4xXDPySco13ruI/4/S6Je2paUcLA9jLpgbDg7TDG0gAUPglOmvLtERlAToSm3O+arM8AhgJfYDQKBgQDPPcZhM2s2dnQ+Bfki8aCY5I6g5Q4v+3iHgw+sHHUJF9gzXqvM6F3Nrtv4K+zZnZl43fPufRfsdo47smc4Sm5VraPJLtLallrT2o0b7xxtbqX9TD4uOBP04pMnVwjWJNwlWg4mA9xDoi0YdmMd4+Sqrh2RRfUzDV2ePIhWbGGmYwKBgQCmo4Fxvr45JGORkzDnOWwPJToyXxqlPMscgrGzsQfG+FJhGKlA+2gEhhLjHuKA7J0R8+4Qda8Iv/tlkIYFPexRE5SurBhDiUsLtrCxAn+F1yHzaZqyY6F5trD7chhZy1U+00IYzLetSoSLJ/ozEjfkmFL9TNXLXrx/ZgtkV4i30QKBgGtFe6Emn3FSgTr1lo7q6YkajWVlCyH1Fd93DK63EDGDwSpd76miAaQMxdnAytmdxSoWIUSqRa7lcxwdbWZqyI/a0HO7o/L3bs5IS1BRRbKzWoIxa+I6rsHMYgUz/OPzsmw3ICm3R9VOZe9KoGWBaASdGbrvrksduP7GMIAjm6wnAoGBALqkCh8J8wbhJx4UpIdfW3ofxCkKa0PYhjXEdSK3dDkaAMkReW0xBn9F2PAWF9Rx+CtTjtJfOeNGVvnG+l98ZqjFFu4KEp2FU1AbW8ziS8eWewjDWPpRAZdULa/SSgPLATB9AdUdwea/YCzZn6VuFOkcVXZrgk1bl5d3D1onXoTJ"
        private const val PUBLIC_KEY =
            "MIIDYDCCAkgCCQCH3SMgX2SGDTANBgkqhkiG9w0BAQsFADByMQswCQYDVQQGEwJzZzELMAkGA1UECAwCc2cxCzAJBgNVBAcMAnNnMQswCQYDVQQKDAJzZzELMAkGA1UECwwCc2cxCzAJBgNVBAMMAnNnMSIwIAYJKoZIhvcNAQkBFhNucmFqcHV0QHBhbG8taXQuY29tMB4XDTIyMDYyODEwMjA0MloXDTIzMDYyODEwMjA0MlowcjELMAkGA1UEBhMCc2cxCzAJBgNVBAgMAnNnMQswCQYDVQQHDAJzZzELMAkGA1UECgwCc2cxCzAJBgNVBAsMAnNnMQswCQYDVQQDDAJzZzEiMCAGCSqGSIb3DQEJARYTbnJhanB1dEBwYWxvLWl0LmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALIHj4rW1oFGjWwuetA94ad/38W2ONOdFSQOo/rA+QzG3dbMYshl13itAtFSxO93JJ8Cc5pqs7lsR66rlSN5PRKyiZnP7hN1nMK9HbNdFiehfPl6YamA79Q+x4ZyCfg6WtaRLKpbprnhz+otdSHaOwsz6J1ZW8QObNhz6mu6yvGYY/Uwi66qX0sMbwHVTTtjB9JQnBOobfEPzo3p0v9VjBf5nOVRGTAucECQMqhGLuqa4PNeDThZRxwgEB7giMsKtMUBBiB5AHNFwFw56E2sFl6cnkxYvbeG8f/BcDkKHZW/QFTJZCA6isSMChIqq/TjD3GI93KUuckbOWBLY4m9+wcCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEArnusfcsKaOigr09QrdXpe1BojhCA+oHEXeTU0cK9evmpfOnPEdEuG8RhpKCiUSqECBHP8a2xahrplTb033J9iIRRbJR2KMqm01g32c1RdlJut0f7h7nLtvZKzvz6aynANZE5VrnNkyfHGXE/OFGBsNXfC/DyZeVPrC71cdY3IigCBP6lE3o3cTzmqcnMDsM6tdjFvZu407yDt3RvKMc/OCUSkvWV/tKfxk+k9vePXtLxWvSffp8b1iH0BlIswbLWpbbtBFJ9ktyj4ASP3Tb3cZDq4iDK2E1xzTdpS/X4h9hiZBCpXy4mipgxluPXS+/CE2iDni2/zT6kJdxZsTfb5g=="
    }

    @Test
    fun testAuthHeaderVerification() {
        // Generate authorization header
        val authHeader: String = AuthorizationHeaderGenerator().generateHeader(
            "G2B_L1T",
            JWSAlgorithm.RS256,
            SecretUtils.getPrivateKey(PRIVATE_KEY)
        )

        logger.info("Authorization Header:$authHeader")

        val authorizationHeaderVerifier = AuthorizationHeaderVerifier()
        val authorizationHeaderInfo = authorizationHeaderVerifier.parseHeader(authHeader)

        val authorizationHeaderValidationCert: RSAPublicKey by lazy {
            RSAKey.parse(
                X509CertUtils.parse(SecretUtils.toPEMCert(PUBLIC_KEY))
            ).toRSAPublicKey()
        }

        assertDoesNotThrow {
            // Verify if authorization header signature is valid
            authorizationHeaderVerifier.verifyHeader(authorizationHeaderInfo, authorizationHeaderValidationCert)
        }
    }

    @Test
    fun testAuthHeaderVerificationDDS() {
        // Generate authorization header
        val authHeader: String = AuthorizationHeaderGenerator().generateHeader(
            "G2B_AGENCYPORTAL",
            JWSAlgorithm.RS256,
            SecretUtils.getPrivateKey(PRIVATE_KEY)
        )

        logger.info("Authorization Header DDS:$authHeader")

        val authorizationHeaderVerifier = AuthorizationHeaderVerifier()
        val authorizationHeaderInfo = authorizationHeaderVerifier.parseHeader(authHeader)

        val authorizationHeaderValidationCert: RSAPublicKey by lazy {
            RSAKey.parse(
                X509CertUtils.parse(SecretUtils.toPEMCert(PUBLIC_KEY))
            ).toRSAPublicKey()
        }

        assertDoesNotThrow {
            // Verify if authorization header signature is valid
            authorizationHeaderVerifier.verifyHeader(authorizationHeaderInfo, authorizationHeaderValidationCert)
        }
    }

    @Test
    fun generateTraceId() {
        val uuid = CommonUtil.generateUUID().replace("-", "")
        logger.info("UUID-> $uuid")
    }
}
