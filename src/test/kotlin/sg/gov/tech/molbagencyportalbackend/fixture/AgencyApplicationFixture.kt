package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.dto.internal.AgencyApplicationsApplicantDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.AgencyApplicationsDTO
import java.time.LocalDateTime

object AgencyApplicationsApplicantFixture {
    val getApplicant = AgencyApplicationsApplicantDTO(
        idNumber = "SX  XXXXXXA",
        idType = "NRIC",
    )
}

object AgencyApplicationFixture {
    val getApplication = AgencyApplicationsDTO(
        id = 1L,
        applicationNumber = ApplicationFixture.createApplication_SingpassSelf.applicationNumber,
        licenceName = ApplicationFixture.createApplication_SingpassSelf.licenceName,
        updatedAt = LocalDateTime.now(),
        submittedDate = ApplicationFixture.createApplication_SingpassSelf.submittedDate,
        status = ApplicationFixture.createApplication_SingpassSelf.status,
        transactionType = ApplicationFixture.createApplication_SingpassSelf.transactionType,
        applicant = AgencyApplicationsApplicantFixture.getApplicant,
        applicantName = ApplicationFixture.createApplication_SingpassSelf.applicantName,
        reviewer = ReviewerDTOFixture.reviewerDTOA,
        caseStatus = ApplicationFixture.createApplication_SingpassSelf.caseStatus,
    )
}
