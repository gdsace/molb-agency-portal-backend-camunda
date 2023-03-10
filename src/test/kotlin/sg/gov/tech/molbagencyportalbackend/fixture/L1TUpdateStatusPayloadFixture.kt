package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TApplication
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TLicence
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TResult
import sg.gov.tech.molbagencyportalbackend.dto.l1t.L1TStatusPushRequestDTO
import sg.gov.tech.molbagencyportalbackend.integration.job.L1TUpdateStatusPayload

object L1TUpdateStatusPayloadFixture {
    val getL1TApplication = L1TApplication(
        applicationNumber = "application1",
        applicationStatus = "Processing",
        paymentAmount = null,
        licence = null,
        message = "Message to applicant",
        clarificationField = null
    )

    val getUpdateApplicationStatusPayload = L1TStatusPushRequestDTO(
        result = L1TResult(
            operation = "statusPoll",
            application = listOf(getL1TApplication),
            licence = null
        )
    )

    val getUpdateApplicationStatus = L1TUpdateStatusPayload(
        referenceNumber = "application1",
        l1tStatusPushRequest = getUpdateApplicationStatusPayload
    )

    val getL1TLicence = L1TLicence(
        licenceNumber = "123test",
        issueDate = "10/08/2022",
        startDate = "10/08/2022",
        expiryDate = "10/08/2023",
        status = "Active",
        renewalIndicator = "N",
        licenceFile = null
    )

    val getUpdateLicenceStatusPayload = L1TStatusPushRequestDTO(
        result = L1TResult(
            operation = "statusPoll",
            application = null,
            licence = listOf(getL1TLicence)
        )
    )
}
