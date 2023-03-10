package sg.gov.tech.molbagencyportalbackend.model

import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "agency")
@ExcludeFromGeneratedCoverageTest
data class Agency(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "code")
    var code: String,

    @Column(name = "name")
    var name: String
)
