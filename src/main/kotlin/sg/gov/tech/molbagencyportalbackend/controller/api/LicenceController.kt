package sg.gov.tech.molbagencyportalbackend.controller.api

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sg.gov.tech.molbagencyportalbackend.dto.AgencyLicenceDetailRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.AgencyLicenceDocumentsRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.LicenceDetailDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.AgencyLicencesRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.LicenceStatisticsDTO
import sg.gov.tech.molbagencyportalbackend.service.LicenceService
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class LicenceController(
    private val licenceService: LicenceService
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/licences")
    fun getDashboardLicences(@Valid requestParams: AgencyLicencesRequestParams): LicenceStatisticsDTO {
        logger.info("Retrieving Agency Licences")
        return licenceService.getDashboardLicences(requestParams)
    }

    @GetMapping("/licence")
    fun getLicence(@Valid requestParams: AgencyLicenceDetailRequestParams): LicenceDetailDTO {
        logger.info("Retrieving licence : ${requestParams.licenceNumber}")

        return licenceService.getLicenceDetails(requestParams)
    }

    @GetMapping("/licence/document")
    fun getLicenceDocument(@Valid requestParams: AgencyLicenceDocumentsRequestParams): ResponseEntity<ByteArray> {
        return licenceService.getLicenceDocument(requestParams)
    }
}
