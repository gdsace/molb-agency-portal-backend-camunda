package sg.gov.tech.molbagencyportalbackend.model

import com.fasterxml.jackson.annotation.JsonValue
import org.hibernate.annotations.Type
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import sg.gov.tech.audit.MaskingConverter
import sg.gov.tech.audit.configuration.EnableAuditLogging
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.dds.DDSUploadResponseDTO
import sg.gov.tech.utils.MolbEnum
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Convert
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
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "licence")
@EntityListeners(AuditingEntityListener::class)
@EnableAuditLogging
@ExcludeFromGeneratedCoverageTest
@Where(clause = "is_deleted = false")
data class Licence(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", referencedColumnName = "id")
    var application: Application? = null,

    @Column(name = "licence_number")
    var licenceNumber: String,

    @Column(name = "licence_name")
    var licenceName: String,

    @Column(name = "login_type")
    var loginType: String,

    @Column(name = "uen")
    var uen: String?,

    @Column(name = "nric")
    @Convert(converter = MaskingConverter::class)
    var nric: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "licence_type_id", referencedColumnName = "id")
    var licenceType: LicenceType,

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var status: LicenceStatus = LicenceStatus.INACTIVE,

    @Column(name = "licence_issuance_type")
    @Enumerated(EnumType.STRING)
    var licenceIssuanceType: LicenceIssuanceType = LicenceIssuanceType.NO_LICENCE,

    @Column(name = "licence_documents")
    @Type(type = "jsonb")
    var licenceDocuments: List<DDSUploadResponseDTO>?,

    @Column(name = "issue_date")
    var issueDate: LocalDate,

    @Column(name = "start_date")
    var startDate: LocalDate,

    @Column(name = "expiry_date")
    var expiryDate: LocalDate?,

    @Column(name = "due_for_renewal")
    var dueForRenewal: Boolean,

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

    @Column(name = "is_deleted")
    var isDeleted: Boolean = false

) {
    fun setInitialStatus(): Licence {
        status =
            if (startDate <= LocalDate.now()) LicenceStatus.ACTIVE
            else LicenceStatus.INACTIVE
        return this
    }

    fun setLicenceIssuanceType(licenceIssuanceTypeString: String): Licence {
        licenceIssuanceType =
            if (licenceIssuanceTypeString == LicenceIssuanceType.UPLOAD_LICENCE.value)
                LicenceIssuanceType.UPLOAD_LICENCE
            else LicenceIssuanceType.NO_LICENCE
        return this
    }

    fun getDocumentName(documentId: String): String? {
        return licenceDocuments?.filter {
            it.documentId == documentId
        }?.get(0)?.filename
    }
}

enum class LicenceStatus(@JsonValue override val value: String) : MolbEnum<String> {
    ACTIVE("Active"),
    EXPIRED("Expired"),
    INACTIVE("Inactive")
}

enum class LicenceIssuanceType(@JsonValue override val value: String) : MolbEnum<String> {
    UPLOAD_LICENCE("uploadLicence"),
    NO_LICENCE("noLicence")
}
