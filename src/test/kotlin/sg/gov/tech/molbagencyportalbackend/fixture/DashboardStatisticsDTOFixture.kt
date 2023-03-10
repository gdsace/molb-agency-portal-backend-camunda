package sg.gov.tech.molbagencyportalbackend.fixture

import com.fasterxml.jackson.module.kotlin.convertValue
import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.model.Application
import sg.gov.tech.molbagencyportalbackend.model.ApplicationStatus
import sg.gov.tech.molbagencyportalbackend.model.ApplyAs
import sg.gov.tech.utils.ObjectMapperConfigurer.GlobalObjectMapper
import java.time.LocalDateTime

@Component
object DashboardStatisticsDTOFixture {
    val openCases = listOf<Application>(
        Application(
            id = 1,
            applicationNumber = "A1234567",
            agency = AgencyFixture.agency,
            licenceType = LicenceTypeFixture.licenceType,
            licenceName = "Test Licence",
            updatedAt = LocalDateTime.now(),
            status = ApplicationStatus.SUBMITTED,
            submittedDate = LocalDateTime.now(),
            transactionType = "new",
            applyAs = ApplyAs.APPLICANT,
            loginType = "Singpass",
            applicant = CreateApplicationRequestDTOFixture.applicant,
            filer = CreateApplicationRequestDTOFixture.filer_null,
            company = CreateApplicationRequestDTOFixture.company_null,
            licenceDataFields = GlobalObjectMapper.convertValue(""),
            formMetaData = GlobalObjectMapper.convertValue(CreateApplicationRequestDTOFixture.versionDTO),
            applicantName = "Obi Wan Kenobi",
            caseStatus = ApplicationStatus.SUBMITTED.getCaseStatus(UserModelFixture.userA),
            activityType = ActivityTypeFixture.activityType
        ),
        Application(
            id = 2,
            applicationNumber = "A1234568",
            agency = AgencyFixture.agency,
            licenceType = LicenceTypeFixture.licenceType,
            licenceName = "Test Licence",
            updatedAt = LocalDateTime.now(),
            status = ApplicationStatus.SUBMITTED,
            submittedDate = LocalDateTime.now(),
            transactionType = "new",
            applyAs = ApplyAs.APPLICANT,
            loginType = "Singpass",
            applicant = CreateApplicationRequestDTOFixture.applicant,
            filer = CreateApplicationRequestDTOFixture.filer_null,
            company = CreateApplicationRequestDTOFixture.company_null,
            licenceDataFields = GlobalObjectMapper.convertValue(""),
            formMetaData = GlobalObjectMapper.convertValue(CreateApplicationRequestDTOFixture.versionDTO),
            applicantName = "Obi Wan Kenobi",
            caseStatus = ApplicationStatus.SUBMITTED.getCaseStatus(UserModelFixture.userA),
            activityType = ActivityTypeFixture.activityType
        )
    )
    val unassignedCases = listOf<Application>(
        Application(
            id = 3,
            applicationNumber = "A1234569",
            agency = AgencyFixture.agency,
            licenceType = LicenceTypeFixture.licenceType,
            licenceName = "Test Licence",
            updatedAt = LocalDateTime.now(),
            status = ApplicationStatus.SUBMITTED,
            submittedDate = LocalDateTime.now(),
            transactionType = "new",
            applyAs = ApplyAs.APPLICANT,
            loginType = "Singpass",
            applicant = CreateApplicationRequestDTOFixture.applicant,
            filer = CreateApplicationRequestDTOFixture.filer_null,
            company = CreateApplicationRequestDTOFixture.company_null,
            licenceDataFields = GlobalObjectMapper.convertValue(""),
            formMetaData = GlobalObjectMapper.convertValue(CreateApplicationRequestDTOFixture.versionDTO),
            applicantName = "Obi Wan Kenobi",
            caseStatus = ApplicationStatus.SUBMITTED.getCaseStatus(null),
            activityType = ActivityTypeFixture.activityType
        ),
        Application(
            id = 4,
            applicationNumber = "A1234570",
            agency = AgencyFixture.agency,
            licenceType = LicenceTypeFixture.licenceType,
            licenceName = "Test Licence",
            updatedAt = LocalDateTime.now(),
            status = ApplicationStatus.SUBMITTED,
            submittedDate = LocalDateTime.now(),
            transactionType = "new",
            applyAs = ApplyAs.APPLICANT,
            loginType = "Singpass",
            applicant = CreateApplicationRequestDTOFixture.applicant,
            filer = CreateApplicationRequestDTOFixture.filer_null,
            company = CreateApplicationRequestDTOFixture.company_null,
            licenceDataFields = GlobalObjectMapper.convertValue(""),
            formMetaData = GlobalObjectMapper.convertValue(CreateApplicationRequestDTOFixture.versionDTO),
            applicantName = "Obi Wan Kenobi",
            caseStatus = ApplicationStatus.SUBMITTED.getCaseStatus(null),
            activityType = ActivityTypeFixture.activityType
        )
    )
    val applicationsCases = listOf<Application>(
        Application(
            id = 5,
            applicationNumber = "A1234571",
            agency = AgencyFixture.agency,
            licenceType = LicenceTypeFixture.licenceType,
            licenceName = "Test Licence",
            updatedAt = LocalDateTime.now(),
            status = ApplicationStatus.SUBMITTED,
            submittedDate = LocalDateTime.now(),
            transactionType = "new",
            applyAs = ApplyAs.APPLICANT,
            loginType = "Singpass",
            applicant = CreateApplicationRequestDTOFixture.applicant,
            filer = CreateApplicationRequestDTOFixture.filer_null,
            company = CreateApplicationRequestDTOFixture.company_null,
            licenceDataFields = GlobalObjectMapper.convertValue(""),
            formMetaData = GlobalObjectMapper.convertValue(CreateApplicationRequestDTOFixture.versionDTO),
            applicantName = "Obi Wan Kenobi",
            caseStatus = ApplicationStatus.SUBMITTED.getCaseStatus(null),
            activityType = ActivityTypeFixture.activityType
        ),
        Application(
            id = 6,
            applicationNumber = "A1234572",
            agency = AgencyFixture.agency,
            licenceType = LicenceTypeFixture.licenceType,
            licenceName = "Test Licence",
            updatedAt = LocalDateTime.now(),
            status = ApplicationStatus.SUBMITTED,
            submittedDate = LocalDateTime.now(),
            transactionType = "new",
            applyAs = ApplyAs.APPLICANT,
            loginType = "Singpass",
            applicant = CreateApplicationRequestDTOFixture.applicant,
            filer = CreateApplicationRequestDTOFixture.filer_null,
            company = CreateApplicationRequestDTOFixture.company_null,
            licenceDataFields = GlobalObjectMapper.convertValue(""),
            formMetaData = GlobalObjectMapper.convertValue(CreateApplicationRequestDTOFixture.versionDTO),
            applicantName = "Obi Wan Kenobi",
            caseStatus = ApplicationStatus.SUBMITTED.getCaseStatus(UserModelFixture.userA),
            activityType = ActivityTypeFixture.activityType
        )
    )
}
