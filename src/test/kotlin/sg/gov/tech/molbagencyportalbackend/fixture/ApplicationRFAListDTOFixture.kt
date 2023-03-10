package sg.gov.tech.molbagencyportalbackend.fixture

import sg.gov.tech.molbagencyportalbackend.dto.ApplicationRFADTO
import sg.gov.tech.molbagencyportalbackend.model.RFAStatus

object ApplicationRFAListDTOFixture {
    val ascList = listOf(
        ApplicationRFADTO(
            rfaNo = 1,
            sendDate = RFAFixture.oldRFA.createdAt,
            responseDate = RFAFixture.oldRFA.responseDate?.toLocalDate(),
            rfaStatus = RFAStatus.RFA_RESPONDED,
            agencyMessageToApplicant = "Please make the following correction",
            internalRemarks = "old internal remark",
            applicantResponse = "some applicant remark",
            updatedBy = ReviewerDTOFixture.reviewerDTOA
        ),
        ApplicationRFADTO(
            rfaNo = 2,
            sendDate = RFAFixture.newRFA.createdAt,
            responseDate = null,
            rfaStatus = RFAStatus.PENDING_APPLICANT_ACTION,
            agencyMessageToApplicant = "some agency Message To Applicant",
            internalRemarks = "new internal remark",
            applicantResponse = null,
            updatedBy = ReviewerDTOFixture.reviewerDTOA
        )
    )
    val descList = listOf(
        ApplicationRFADTO(
            rfaNo = 2,
            sendDate = RFAFixture.newRFA.createdAt,
            responseDate = null,
            rfaStatus = RFAStatus.PENDING_APPLICANT_ACTION,
            agencyMessageToApplicant = "some agency Message To Applicant",
            internalRemarks = "new internal remark",
            applicantResponse = null,
            updatedBy = ReviewerDTOFixture.reviewerDTOA
        ),
        ApplicationRFADTO(
            rfaNo = 1,
            sendDate = RFAFixture.oldRFA.createdAt,
            responseDate = RFAFixture.oldRFA.responseDate?.toLocalDate(),
            rfaStatus = RFAStatus.RFA_RESPONDED,
            agencyMessageToApplicant = "Please make the following correction",
            internalRemarks = "old internal remark",
            applicantResponse = "some applicant remark",
            updatedBy = ReviewerDTOFixture.reviewerDTOA
        )
    )
}
