package sg.gov.tech.molbagencyportalbackend.fixture

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import sg.gov.tech.molbagencyportalbackend.model.RFA
import sg.gov.tech.molbagencyportalbackend.model.RFAStatus
import java.time.LocalDateTime

object RFAFixture {
    val createRFA = RFA(
        application = ApplicationFixture.createApplication_SingpassSelf,
        revisionIdIndexUpdated = 2,
        status = RFAStatus.PENDING_APPLICANT_ACTION,
        clarificationFields = ObjectMapper().convertValue(""),
        applicantRemarks = null,
        responseDate = null,
        createdBy = null,
        createdAt = null,
        updatedBy = null,
        updatedAt = null,
        revisionIdIndexCreated = 2
    )
    val oldRFA = RFA(
        application = ApplicationFixture.createApplication_SingpassSelf,
        revisionIdIndexUpdated = 2,
        status = RFAStatus.RFA_RESPONDED,
        clarificationFields = ObjectMapper().convertValue(""),
        applicantRemarks = "some applicant remark",
        responseDate = LocalDateTime.now(),
        createdBy = "1",
        createdAt = LocalDateTime.now(),
        updatedBy = "1",
        updatedAt = LocalDateTime.now(),
        revisionIdIndexCreated = 2
    )
    val newRFA = RFA(
        application = ApplicationFixture.createApplication_SingpassSelf,
        revisionIdIndexUpdated = 4,
        status = RFAStatus.PENDING_APPLICANT_ACTION,
        clarificationFields = ObjectMapper().convertValue(""),
        applicantRemarks = null,
        responseDate = null,
        createdBy = "1",
        createdAt = LocalDateTime.now(),
        updatedBy = "1",
        updatedAt = LocalDateTime.now(),
        revisionIdIndexCreated = 2
    )
    val baseRFA = RFA(
        application = ApplicationFixture.createApplication_SingpassSelf,
        revisionIdIndexUpdated = 4,
        status = RFAStatus.PENDING_APPLICANT_ACTION,
        clarificationFields = ObjectMapper().convertValue(""),
        applicantRemarks = null,
        responseDate = null,
        createdBy = "1",
        createdAt = LocalDateTime.now(),
        updatedBy = "1",
        updatedAt = LocalDateTime.now(),
        revisionIdIndexCreated = 2
    )
}
