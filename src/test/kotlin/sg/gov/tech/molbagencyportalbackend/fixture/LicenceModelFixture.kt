package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.model.Licence
import sg.gov.tech.molbagencyportalbackend.model.LicenceIssuanceType
import sg.gov.tech.molbagencyportalbackend.model.LicenceStatus
import java.time.LocalDate
import java.time.LocalDateTime

object LicenceModelFixture {
    val licence = Licence(
        id = 1L,
        application = ApplicationFixture.createApplication_SingpassSelf,
        licenceNumber = ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles.licenceNumber,
        licenceName = ApplicationFixture.createApplication_SingpassSelf.licenceName,
        loginType = ApplicationFixture.createApplication_SingpassSelf.loginType,
        uen = "testUEN",
        nric = "testNRIC",
        licenceType = LicenceTypeFixture.licenceType,
        licenceDocuments = null,
        issueDate = LocalDate.now(),
        startDate = LocalDate.now(),
        expiryDate = LocalDate.now().plusDays(365),
        createdBy = null,
        createdAt = null,
        updatedBy = null,
        updatedAt = null,
        dueForRenewal = false,
        licenceIssuanceType = LicenceIssuanceType.NO_LICENCE,
        isDeleted = false
    )

    val licenceList = listOf<Licence>(
        Licence(
            id = 1,
            application = ApplicationFixture.createApplication_SingpassSelf,
            licenceNumber = ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles.licenceNumber,
            licenceName = ApplicationFixture.createApplication_SingpassSelf.licenceName,
            loginType = ApplicationFixture.createApplication_SingpassSelf.loginType,
            uen = "testUEN",
            nric = "testNRIC",
            licenceType = LicenceTypeFixture.licenceType,
            licenceDocuments = null,
            issueDate = LocalDate.now(),
            startDate = LocalDate.now(),
            expiryDate = LocalDate.now().plusDays(365),
            createdBy = null,
            createdAt = null,
            updatedBy = null,
            updatedAt = LocalDateTime.now(),
            dueForRenewal = false,
            licenceIssuanceType = LicenceIssuanceType.NO_LICENCE,
            isDeleted = false
        ),
        Licence(
            id = 2,
            application = ApplicationFixture.createApplication_SingpassSelf,
            licenceNumber = ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles.licenceNumber,
            licenceName = ApplicationFixture.createApplication_SingpassSelf.licenceName,
            loginType = ApplicationFixture.createApplication_SingpassSelf.loginType,
            uen = "testUEN",
            nric = "testNRIC",
            licenceType = LicenceTypeFixture.licenceType,
            licenceDocuments = null,
            issueDate = LocalDate.now(),
            startDate = LocalDate.now(),
            expiryDate = LocalDate.now().plusDays(365),
            createdBy = null,
            createdAt = null,
            updatedBy = null,
            updatedAt = LocalDateTime.now(),
            dueForRenewal = false,
            licenceIssuanceType = LicenceIssuanceType.NO_LICENCE,
            isDeleted = false
        )
    )

    val licenceListToExpired = listOf<Licence>(
        Licence(
            id = 1,
            application = ApplicationFixture.createApplication_SingpassSelf,
            licenceNumber = ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles.licenceNumber,
            licenceName = ApplicationFixture.createApplication_SingpassSelf.licenceName,
            loginType = ApplicationFixture.createApplication_SingpassSelf.loginType,
            uen = "testUEN",
            nric = "testNRIC",
            licenceType = LicenceTypeFixture.licenceType,
            licenceDocuments = null,
            issueDate = LocalDate.now().minusDays(365),
            startDate = LocalDate.now().minusDays(365),
            expiryDate = LocalDate.now(),
            status = LicenceStatus.EXPIRED,
            createdBy = null,
            createdAt = null,
            updatedBy = null,
            updatedAt = LocalDateTime.now(),
            dueForRenewal = false,
            licenceIssuanceType = LicenceIssuanceType.NO_LICENCE,
            isDeleted = false
        ),
        Licence(
            id = 2,
            application = ApplicationFixture.createApplication_SingpassSelf,
            licenceNumber = ApproveApplicationRequestDTOFixture.ApproveApplicationRequestNoFiles.licenceNumber,
            licenceName = ApplicationFixture.createApplication_SingpassSelf.licenceName,
            loginType = ApplicationFixture.createApplication_SingpassSelf.loginType,
            uen = "testUEN",
            nric = "testNRIC",
            licenceType = LicenceTypeFixture.licenceType,
            licenceDocuments = null,
            issueDate = LocalDate.now().minusDays(365),
            startDate = LocalDate.now().minusDays(365),
            expiryDate = LocalDate.now(),
            status = LicenceStatus.EXPIRED,
            createdBy = null,
            createdAt = null,
            updatedBy = null,
            updatedAt = LocalDateTime.now(),
            dueForRenewal = false,
            licenceIssuanceType = LicenceIssuanceType.NO_LICENCE,
            isDeleted = false
        )
    )
}
