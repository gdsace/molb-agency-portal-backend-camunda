package sg.gov.tech.molbagencyportalbackend.model

import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.utils.MolbEnum
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "activity_type")
@ExcludeFromGeneratedCoverageTest
data class ActivityType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    val type: ActivityValue
)

@ExcludeFromGeneratedCoverageTest
enum class ActivityValue(override val value: String) : MolbEnum<String> {
    CREATE_APPLICATION("CreateApplication"),
    CLAIM_APPLICATION("ClaimApplication"),
    PARTIALLY_APPROVE_APPLICATION("PartiallyApproveApplication"),
    APPROVE_APPLICATION("ApproveApplication"),
    ASSIGN_APPLICATION("AssignApplication"),
    REJECT_APPLICATION("RejectApplication"),
    REASSIGN_APPLICATION("ReassignApplication"),
    APPROVE_APPLICATION_WITHDRAWAL("ApproveApplicationWithdrawal"),
    REJECT_APPLICATION_WITHDRAWAL("RejectApplicationWithdrawal"),
    WITHDRAW_APPLICATION("WithdrawApplication"),
    APPLICATION_OVERDUE("ApplicationOverdue"),
    RFA_RESPONDED("RFAResponded"),
    SEND_RFA("SendRFA"),
    CANCEL_RFA("CancelRFA")
}
