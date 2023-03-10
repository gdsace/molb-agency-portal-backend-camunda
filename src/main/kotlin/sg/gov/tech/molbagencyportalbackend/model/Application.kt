package sg.gov.tech.molbagencyportalbackend.model

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import org.hibernate.annotations.Type
import org.hibernate.annotations.Where
import org.hibernate.envers.Audited
import org.hibernate.envers.RelationTargetAuditMode
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import sg.gov.tech.audit.configuration.EnableAuditLogging
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.l1t.ApplicantDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.CompanyDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.FilerDTO
import sg.gov.tech.utils.MolbEnum
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "application")
@EntityListeners(AuditingEntityListener::class)
@Audited
@EnableAuditLogging
@ExcludeFromGeneratedCoverageTest
@Where(clause = "is_deleted = false")
data class Application(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "application_number")
    var applicationNumber: String,

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agency_id", referencedColumnName = "id")
    var agency: Agency,

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "licence_type_id", referencedColumnName = "id")
    var licenceType: LicenceType,

    @Column(name = "licence_name")
    var licenceName: String,

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var status: ApplicationStatus,

    @Column(name = "submitted_date")
    var submittedDate: LocalDateTime,

    @Column(name = "transaction_type")
    var transactionType: String,

    @Column(name = "apply_as")
    @Enumerated(EnumType.STRING)
    var applyAs: ApplyAs,

    @Column(name = "login_type")
    var loginType: String,

    @Column(name = "applicant", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var applicant: ApplicantDTO,

    @Column(name = "filer", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var filer: FilerDTO,

    @Column(name = "company", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var company: CompanyDTO,

    @Column(name = "licence_data_fields", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var licenceDataFields: JsonNode,

    @Column(name = "form_meta_data", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    var formMetaData: JsonNode,

    @Column(name = "created_by")
    @CreatedBy
    var createdBy: String? = null,

    @Column(name = "created_at")
    @CreatedDate
    var createdAt: LocalDateTime? = null,

    @Column(name = "updated_by")
    @LastModifiedBy
    var updatedBy: String? = null,

    @Column(name = "updated_at")
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null,

    @Column(name = "applicant_name")
    var applicantName: String,

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", referencedColumnName = "id")
    var reviewer: User? = null,

    @Column(name = "case_status")
    var caseStatus: String,

    @Column(name = "internal_remarks")
    var internalRemarks: String? = null,

    @Column(name = "message_to_applicant")
    var messageToApplicant: String? = null,

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_type_id", referencedColumnName = "id")
    var activityType: ActivityType,

    @Column(name = "is_deleted")
    var isDeleted: Boolean = false
) {
    fun setMessageToApplicant(messageToApplicant: String?): Application {
        this.messageToApplicant = messageToApplicant?.ifEmpty { null }
        return this
    }

    fun setInternalRemarks(internalRemarks: String?): Application {
        this.internalRemarks = internalRemarks?.ifEmpty { null }
        return this
    }
}

@ExcludeFromGeneratedCoverageTest
enum class ApplyAs(override val value: String) : MolbEnum<String> {
    APPLICANT("As an applicant"),
    ON_BEHALF("On behalf of applicant");

    companion object {
        fun isValidValue(value: String): Boolean {
            return MolbEnum.enumContains<ApplyAs, String>(value)
        }
    }
}

@ExcludeFromGeneratedCoverageTest
enum class ApplicationStatus(@JsonValue override val value: String) : MolbEnum<String> {
    APPROVED("Approved") {
        override fun getCaseStatus(reviewer: User?): String = "Closed"
    },
    OVERDUE("Overdue") {
        override fun getCaseStatus(reviewer: User?): String = "Closed"
    },
    PARTIALLY_APPROVED("Partially Approved") {
        override fun getCaseStatus(reviewer: User?): String = "Closed"
    },
    PENDING_APPLICANT_ACTION("Pending Applicant Action") {
        override fun getCaseStatus(reviewer: User?): String =
            reviewer?.let { "Pending Applicant Assigned" } ?: "Pending Applicant"
    },
    PENDING_ONLINE_PAYMENT("Pending Online Payment") {
        override fun getCaseStatus(reviewer: User?): String =
            reviewer?.let { "Assigned" } ?: "Pending Applicant"
    },
    PENDING_OFFLINE_PAYMENT("Pending Offline Payment") {
        override fun getCaseStatus(reviewer: User?): String =
            reviewer?.let { "Assigned" } ?: "Pending Applicant"
    },
    PENDING_WITHDRAWAL("Pending Withdrawal") {
        override fun getCaseStatus(reviewer: User?): String =
            reviewer?.let { "Assigned" } ?: "Pending Assignment"
    },
    PROCESSING("Processing") {
        override fun getCaseStatus(reviewer: User?): String = "Assigned"
    },
    REJECTED("Rejected") {
        override fun getCaseStatus(reviewer: User?): String = "Closed"
    },
    RFA_RESPONDED("RFA Responded") {
        override fun getCaseStatus(reviewer: User?): String =
            reviewer?.let { "Assigned" } ?: "Pending Assignment"
    },
    STAGE2_SUBMITTED("Stage 2 Submitted") {
        override fun getCaseStatus(reviewer: User?): String =
            reviewer?.let { "Assigned" } ?: "Pending Assignment"
    },
    SUBMITTED("Submitted") {
        override fun getCaseStatus(reviewer: User?): String = "Pending Assignment"
    },
    WITHDRAWN("Withdrawn") {
        override fun getCaseStatus(reviewer: User?): String = "Closed"
    };

    abstract fun getCaseStatus(reviewer: User?): String

    companion object {
        fun isValidValue(value: String): Boolean =
            MolbEnum.enumContains<ApplicationStatus, String>(value)

        fun getFinalStatuses(): List<ApplicationStatus> = listOf(APPROVED, PARTIALLY_APPROVED, REJECTED)
        fun getAssignedStatuses(): List<ApplicationStatus> = listOf(PENDING_WITHDRAWAL, PROCESSING)
        fun getClaimableStatuses(): List<ApplicationStatus> = listOf(
            SUBMITTED, PENDING_WITHDRAWAL,
            PENDING_APPLICANT_ACTION, RFA_RESPONDED
        )
        fun getSubmittedStatuses(): List<ApplicationStatus> = listOf(SUBMITTED, RFA_RESPONDED)
        fun getRejectableStatuses(): List<ApplicationStatus> = listOf(
            PROCESSING, PENDING_APPLICANT_ACTION, RFA_RESPONDED
        )
        fun getApprovableStatuses(): List<ApplicationStatus> = listOf(
            PROCESSING, PENDING_APPLICANT_ACTION, RFA_RESPONDED
        )
        fun getPendingApplicantActionStatuses(): List<ApplicationStatus> = listOf(PROCESSING, RFA_RESPONDED)
        fun getWhitelistRemarkStatuses(): List<ApplicationStatus> = listOf(
            PARTIALLY_APPROVED,
            APPROVED,
            REJECTED
        )

        fun getRFASubmittedStatuses(): List<ApplicationStatus> = listOf(PENDING_APPLICANT_ACTION)
        fun getWithdrawableStatuses(): List<ApplicationStatus> = listOf(PENDING_WITHDRAWAL)
        fun getPendingApplicantStatuses(): List<ApplicationStatus> = listOf(PENDING_APPLICANT_ACTION)
        fun getPendingAgencyStatuses(): List<ApplicationStatus> = listOf(SUBMITTED, PROCESSING, RFA_RESPONDED)
    }
}
