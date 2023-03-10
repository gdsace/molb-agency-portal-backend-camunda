package sg.gov.tech.molbagencyportalbackend.model

import com.google.common.primitives.Longs
import org.hibernate.envers.RevisionEntity
import org.hibernate.envers.RevisionNumber
import org.hibernate.envers.RevisionTimestamp
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import java.text.DateFormat
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table

@Entity
@Table(name = "revision_info")
@RevisionEntity
@ExcludeFromGeneratedCoverageTest
data class HistoryRevisionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "revision_info_revision_id_seq")
    @SequenceGenerator(
        name = "revision_info_revision_id_seq",
        sequenceName = "revision_info_revision_id_seq",
        allocationSize = 1
    )
    @RevisionNumber
    @Column(name = "revision_id")
    var revisionId: Long,

    @RevisionTimestamp
    @Column(name = "revision_timestamp")
    var revisionTimestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is HistoryRevisionEntity) {
            return false
        }

        return (
            revisionId == other.revisionId &&
                revisionTimestamp == other.revisionTimestamp
            )
    }

    override fun hashCode(): Int {
        return 31 * Longs.hashCode(revisionId) + (revisionTimestamp xor (revisionTimestamp ushr 32)).toInt()
    }

    @Override
    override fun toString(): String {
        return (
            "DefaultRevisionEntity(id = " + revisionId +
                ", revisionDate = " + DateFormat.getDateTimeInstance().format(revisionTimestamp) + ")"
            )
    }
}
