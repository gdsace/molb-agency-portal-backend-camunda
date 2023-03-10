package sg.gov.tech.molbagencyportalbackend.auth

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.util.CommonUtil
import java.security.PrivateKey
import java.time.Instant

// TODO Consider moving to molb-backend-common
@ExcludeFromGeneratedCoverageTest
class AuthorizationHeaderGenerator {
    /**
     * Generate an authorization header.
     *
     * Token will be in the format of appid.timestamp.signaturemethod.uuid.signature
     *
     * E.g. G2B_DASHBOARD.1608014086.RS256.2510a5dc-3c0e.e23e3geGHHbrtnh6htr
     *
     * @param appId The App ID
     * @param signatureMethod The signature algorithm
     * @param privateKey The private key
     *
     * @return An authorization header
     */
    fun generateHeader(
        appId: String,
        signatureMethod: JWSAlgorithm,
        privateKey: PrivateKey
    ): String {
        val timestamp = Instant.now().epochSecond
        val nonce = CommonUtil.generateUUID()

        val strArr = listOf<String>(appId, timestamp.toString(), signatureMethod.name, nonce)
        val baseString = strArr.joinToString(".")

        val signer = RSASSASigner(privateKey)
        val header = JWSHeader.Builder(signatureMethod)
            .type(JOSEObjectType.JWT)
            .build()

        val signature = signer.sign(header, baseString.toByteArray()).toString()

        return "$baseString.$signature"
    }
}
