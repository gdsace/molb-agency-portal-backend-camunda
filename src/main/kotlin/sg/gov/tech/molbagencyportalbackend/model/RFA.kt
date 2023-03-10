package sg.gov.tech.molbagencyportalbackend.model

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import org.hibernate.annotations.Type
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import sg.gov.tech.audit.configuration.EnableAuditLogging
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.utils.MolbEnum
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "rfa")
@EntityListeners(AuditingEntityListener::class)
@EnableAuditLogging
@ExcludeFromGeneratedCoverageTest
data class RFA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "application_id", referencedColumnName = "id")
    val application: Application,

    @Column(name = "revision_id_index_updated")
    var revisionIdIndexUpdated: Int,

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var status: RFAStatus,

    @Column(name = "clarification_fields", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    val clarificationFields: JsonNode,

    @Column(name = "applicant_remarks")
    val applicantRemarks: String?,

    @Column(name = "response_date")
    val responseDate: LocalDateTime?,

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

    @Column(name = "revision_id_index_created")
    val revisionIdIndexCreated: Int
)

enum class RFAStatus(@JsonValue override val value: String) : MolbEnum<String> {
    PENDING_APPLICANT_ACTION("Pending Applicant Action"),
    RFA_RESPONDED("RFA Responded"),
    CANCELLED("Cancelled")
}
