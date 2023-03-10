package sg.gov.tech.molbagencyportalbackend.dto

import com.fasterxml.jackson.databind.JsonNode
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReviewerDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.ApplicantDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.CompanyDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.FilerDTO
import sg.gov.tech.molbagencyportalbackend.model.Agency
import java.time.LocalDateTime

@ExcludeFromGeneratedCoverageTest
data class ApplicationDetailDTO(
    val agency: Agency,
    val applicant: ApplicantDTO,
    val applicationNumber: String,
    val applyAs: String,
    val company: CompanyDTO,
    val filer: FilerDTO,
    val formMetaData: JsonNode,
    val licenceDataFields: JsonNode,
    val licenceName: String,
    val licenceNumber: String?,
    val loginType: String,
    val submittedDate: LocalDateTime,
    val transactionType: String,
    val updatedBy: ReviewerDTO?,
    val updatedAt: LocalDateTime?,
    val reviewer: ReviewerDTO?,
    val status: String,
    val internalRemarks: String?,
    val messageToApplicant: String?
)
