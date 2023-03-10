package sg.gov.tech.molbagencyportalbackend.fixture

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import sg.gov.tech.molbagencyportalbackend.model.Application
import sg.gov.tech.molbagencyportalbackend.model.ApplicationStatus
import sg.gov.tech.molbagencyportalbackend.model.ApplyAs
import java.io.File
import java.time.LocalDateTime

object ApplicationFixture {

    var licenceFile: File = File(this.javaClass.classLoader.getResource("licence_data_fields.json").file)
    var metaDataFile: File = File(this.javaClass.classLoader.getResource("meta_data_fields.json").file)
    val createApplication_SingpassSelf = Application(
        id = 1,
        applicationNumber = "A1234567",
        agency = AgencyFixture.agency,
        licenceType = LicenceTypeFixture.licenceType,
        licenceName = "Test Licence",
        status = ApplicationStatus.SUBMITTED,
        submittedDate = LocalDateTime.now(),
        transactionType = "new",
        applyAs = ApplyAs.APPLICANT,
        loginType = "Singpass",
        applicant = CreateApplicationRequestDTOFixture.applicant,
        filer = CreateApplicationRequestDTOFixture.filer_null,
        company = CreateApplicationRequestDTOFixture.company_null,
        licenceDataFields = ObjectMapper().readValue(licenceFile, JsonNode::class.java),
        formMetaData = ObjectMapper().readValue(metaDataFile, JsonNode::class.java),
        applicantName = "Obi Wan Kenobi",
        caseStatus = ApplicationStatus.SUBMITTED.getCaseStatus(null),
        messageToApplicant = null,
        internalRemarks = null,
        activityType = ActivityTypeFixture.activityType,
        updatedBy = "1",
        isDeleted = false
    )
    val applicationWithOldRFA = createApplication_SingpassSelf.copy(
        status = ApplicationStatus.PROCESSING,
        caseStatus = ApplicationStatus.SUBMITTED.getCaseStatus(null),
        messageToApplicant = "Please make the following correction",
        internalRemarks = "old internal remark"
    )
    val applicationWithNewRFA = createApplication_SingpassSelf.copy(
        status = ApplicationStatus.PROCESSING,
        caseStatus = ApplicationStatus.SUBMITTED.getCaseStatus(null),
        messageToApplicant = "some agency Message To Applicant",
        internalRemarks = "new internal remark"
    )
    val withdrawApplication = createApplication_SingpassSelf.copy(
        status = ApplicationStatus.PENDING_WITHDRAWAL,
        caseStatus = ApplicationStatus.PENDING_WITHDRAWAL.getCaseStatus(null),
        messageToApplicant = "some message",
        internalRemarks = "a withdraw application"
    )

    val applicationHistory = listOf(
        createApplication_SingpassSelf.copy(
            status = ApplicationStatus.SUBMITTED,
            caseStatus = ApplicationStatus.SUBMITTED.getCaseStatus(null)
        ),
        createApplication_SingpassSelf.copy(
            status = ApplicationStatus.PROCESSING,
            caseStatus = ApplicationStatus.PROCESSING.getCaseStatus(null)
        ),
        createApplication_SingpassSelf.copy(
            status = ApplicationStatus.PENDING_WITHDRAWAL,
            caseStatus = ApplicationStatus.PENDING_WITHDRAWAL.getCaseStatus(null)
        )
    )

    val applicationHistorySameLatestStatus = listOf(
        createApplication_SingpassSelf.copy(
            status = ApplicationStatus.SUBMITTED,
            caseStatus = ApplicationStatus.SUBMITTED.getCaseStatus(null)
        ),
        createApplication_SingpassSelf.copy(
            status = ApplicationStatus.PENDING_WITHDRAWAL,
            caseStatus = ApplicationStatus.PENDING_WITHDRAWAL.getCaseStatus(null)
        ),
        createApplication_SingpassSelf.copy(
            status = ApplicationStatus.PENDING_WITHDRAWAL,
            caseStatus = ApplicationStatus.PENDING_WITHDRAWAL.getCaseStatus(null)
        )
    )

    val applicationHistoryPAA = listOf(
        createApplication_SingpassSelf.copy(
            status = ApplicationStatus.SUBMITTED,
            caseStatus = ApplicationStatus.SUBMITTED.getCaseStatus(null)
        ),
        createApplication_SingpassSelf.copy(
            status = ApplicationStatus.PROCESSING,
            caseStatus = ApplicationStatus.PROCESSING.getCaseStatus(null)
        ),
        createApplication_SingpassSelf.copy(
            status = ApplicationStatus.PENDING_APPLICANT_ACTION,
            caseStatus = ApplicationStatus.PENDING_APPLICANT_ACTION.getCaseStatus(null)
        ),
        createApplication_SingpassSelf.copy(
            status = ApplicationStatus.PENDING_WITHDRAWAL,
            caseStatus = ApplicationStatus.PENDING_WITHDRAWAL.getCaseStatus(null)
        )
    )
}
