package sg.gov.tech.molbagencyportalbackend.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import sg.gov.tech.audit.configuration.EnableAuditLogging
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.ReassignUserDTOProjection
import sg.gov.tech.utils.MolbEnum
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.ColumnResult
import javax.persistence.ConstructorResult
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToOne
import javax.persistence.NamedNativeQueries
import javax.persistence.NamedNativeQuery
import javax.persistence.SqlResultSetMapping
import javax.persistence.SqlResultSetMappings
import javax.persistence.Table

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
@EnableAuditLogging
@ExcludeFromGeneratedCoverageTest
@NamedNativeQueries(
    NamedNativeQuery(
        name = "User.getUsersForReassignAsProjection",
        resultSetMapping = "ReassignUsersDTOMapping",
        query = "SELECT usr.id, usr.name, usr.email from users usr  " +
            "INNER  JOIN user_role usr_role ON usr_role.users_id = usr.id " +
            "INNER JOIN role_authority role_auth on role_auth.role_id  = usr_role.role_id" +
            " where role_auth.authority_id in (select auth.id  from authority auth where auth.code=?2) " +
            "and usr.is_deleted =false and usr.agency_id =?1 and status ='ACTIVE' and usr.id !=?3"
    )
)

@SqlResultSetMappings(
    SqlResultSetMapping(
        name = "ReassignUsersDTOMapping",
        classes = arrayOf(
            ConstructorResult(
                targetClass = ReassignUserDTOProjection::class,
                columns = arrayOf(
                    ColumnResult(name = "id", type = Long::class),
                    ColumnResult(name = "name", type = String::class),
                    ColumnResult(name = "email", type = String::class)
                )
            )
        )
    )
)
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "agency_id")
    var agencyId: Long?,

    @Column(name = "name")
    var name: String,

    @Column(name = "email")
    var email: String,

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var status: UserStatus,

    @Column(name = "is_deleted")
    var isDeleted: Boolean = false,

    @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = [JoinColumn(name = "users_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    @JsonManagedReference
    var role: Role,

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
    var updatedAt: LocalDateTime? = null
)

@ExcludeFromGeneratedCoverageTest
enum class UserStatus(@JsonValue override val value: String) : MolbEnum<String> {
    INACTIVE("Inactive"),
    ACTIVE("Active")
}
