package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.dto.internal.ApproveApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.model.LicenceIssuanceType

object ApproveApplicationRequestDTOFixture {
    val ApproveApplicationRequestNoFiles = ApproveApplicationRequestDTO(
        approvalType = "Partially Approved",
        licenceNumber = "123test",
        licenceIssuanceType = LicenceIssuanceType.NO_LICENCE.value,
        issueDate = "10/08/2022",
        startDate = "10/08/2022",
        expiryDate = "10/08/2023",
        agencyMessageToApplicant = "test message to applicant",
        internalRemarks = "test internal remark"
    )

    val ApproveApplicationRequestWithFiles = ApproveApplicationRequestDTO(
        approvalType = "Partially Approved",
        licenceNumber = "123test",
        licenceIssuanceType = LicenceIssuanceType.UPLOAD_LICENCE.value,
        issueDate = "10/08/2022",
        startDate = "10/08/2022",
        expiryDate = "10/08/2023",
        agencyMessageToApplicant = "test message to applicant",
        internalRemarks = "test internal remark"
    )
}
