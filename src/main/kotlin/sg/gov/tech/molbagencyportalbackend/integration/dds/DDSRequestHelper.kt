package sg.gov.tech.molbagencyportalbackend.integration.dds

import com.nimbusds.jose.JWSAlgorithm
import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.auth.AuthorizationHeaderGenerator
import sg.gov.tech.molbagencyportalbackend.dto.dds.DDSUploadRequest
import sg.gov.tech.molbagencyportalbackend.dto.dds.Owners
import sg.gov.tech.molbagencyportalbackend.model.Application
import sg.gov.tech.molbagencyportalbackend.model.Licence
import sg.gov.tech.molbagencyportalbackend.util.CommonUtil
import sg.gov.tech.security.SecretUtils

@Component
class DDSRequestHelper {
    companion object {
        private const val AUTHORIZATION_HEADER_KEY = "authorization"
        private const val TRACE_ID_HEADER_KEY = "trace-id"

        // document download headers
        private const val LOGIN_TYPE = "login-type"
        private const val UEN = "uen"
        private const val UIN_FIN = "uinfin"

        private const val METADATA_DOMAIN = "G2B_AGENCYPORTAL"
        private fun getAuthorizationHeader(appId: String, privateKey: String): String {
            return AuthorizationHeaderGenerator().generateHeader(
                appId,
                JWSAlgorithm.RS256,
                SecretUtils.getPrivateKey(privateKey)
            )
        }

        private fun getTraceID(): String {
            return CommonUtil.generateUUID().replace("-", "")
        }
    }

    fun getDDSUploadFileHeaderMap(appId: String, privateKey: String): HashMap<String, String> {
        val headers: HashMap<String, String> = HashMap()
        headers[AUTHORIZATION_HEADER_KEY] = getAuthorizationHeader(appId, privateKey)
        headers[TRACE_ID_HEADER_KEY] = getTraceID()
        return headers
    }

    fun getDDSRequestBodyMetaData(licenceNumber: String, application: Application): DDSUploadRequest {
        return DDSUploadRequest(
            domainReferenceId = licenceNumber,
            domain = METADATA_DOMAIN,
            agency = application.agency.code,
            owners = getOwners(application)
        )
    }

    private fun getOwners(application: Application) = buildList {
        add(
            Owners(
                uinfin = application.applicant.id.idNumber ?: "",
                uen = application.company.uen
            )
        )
        if (!application.filer.id.idNumber.isNullOrEmpty()) {
            add(
                Owners(
                    uinfin = application.filer.id.idNumber ?: "",
                    uen = application.company.uen
                )
            )
        }
    }

    fun getDDSDownloadFileHeaderMap(licence: Licence, appId: String, privateKey: String): HashMap<String, String?> {
        val headers: HashMap<String, String?> = HashMap()
        headers[AUTHORIZATION_HEADER_KEY] = getAuthorizationHeader(appId, privateKey)
        headers[TRACE_ID_HEADER_KEY] = getTraceID()
        headers[LOGIN_TYPE] = licence.loginType.uppercase()
        // uen is not a mandatory field do not send in header if null
        licence.uen?.let { headers[UEN] = it }
        headers[UIN_FIN] = licence.nric
        return headers
    }
}
