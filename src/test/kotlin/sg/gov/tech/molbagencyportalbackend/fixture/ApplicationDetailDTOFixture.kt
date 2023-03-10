package sg.gov.tech.molbagencyportalbackend.fixture

import com.fasterxml.jackson.module.kotlin.convertValue
import sg.gov.tech.molbagencyportalbackend.dto.ApplicationDetailDTO
import sg.gov.tech.utils.ObjectMapperConfigurer
import java.time.LocalDateTime

object ApplicationDetailDTOFixture {
    val getApplication = ApplicationDetailDTO(
        agency = AgencyFixture.agency,
        applicant = CreateApplicationRequestDTOFixture.applicant,
        applicationNumber = ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
        applyAs = ApplicationFixture.createApplication_SingpassSelf.applyAs.value,
        company = CreateApplicationRequestDTOFixture.company_null,
        filer = CreateApplicationRequestDTOFixture.filer_null,
        formMetaData = ObjectMapperConfigurer.GlobalObjectMapper.convertValue(
            CreateApplicationRequestDTOFixture.versionDTO
        ),
        licenceDataFields = ObjectMapperConfigurer.GlobalObjectMapper.convertValue(""),
        licenceName = ApplicationFixture.createApplication_SingpassSelf.licenceName,
        licenceNumber = LicenceTypeFixture.licenceType.licenceId,
        loginType = ApplicationFixture.createApplication_SingpassSelf.loginType,
        submittedDate = LocalDateTime.now(),
        transactionType = ApplicationFixture.createApplication_SingpassSelf.transactionType,
        updatedBy = ReviewerDTOFixture.reviewerDTOB,
        updatedAt = ApplicationFixture.createApplication_SingpassSelf.updatedAt,
        reviewer = null,
        status = ApplicationFixture.createApplication_SingpassSelf.status.value,
        internalRemarks = null,
        messageToApplicant = null
    )

    val getApplicationWithRemarksMessage = ApplicationDetailDTO(
        agency = AgencyFixture.agency,
        applicant = CreateApplicationRequestDTOFixture.applicant,
        applicationNumber = ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
        applyAs = ApplicationFixture.createApplication_SingpassSelf.applyAs.value,
        company = CreateApplicationRequestDTOFixture.company_null,
        filer = CreateApplicationRequestDTOFixture.filer_null,
        formMetaData = ObjectMapperConfigurer.GlobalObjectMapper.convertValue(
            CreateApplicationRequestDTOFixture.versionDTO
        ),
        licenceDataFields = ObjectMapperConfigurer.GlobalObjectMapper.convertValue(""),
        licenceName = ApplicationFixture.createApplication_SingpassSelf.licenceName,
        licenceNumber = LicenceTypeFixture.licenceType.licenceId,
        loginType = ApplicationFixture.createApplication_SingpassSelf.loginType,
        submittedDate = LocalDateTime.now(),
        transactionType = ApplicationFixture.createApplication_SingpassSelf.transactionType,
        updatedBy = ReviewerDTOFixture.reviewerDTOB,
        updatedAt = ApplicationFixture.createApplication_SingpassSelf.updatedAt,
        reviewer = null,
        status = ApplicationFixture.createApplication_SingpassSelf.status.value,
        internalRemarks = "This is a test Remarks",
        messageToApplicant = "This is a test Message"
    )
}
