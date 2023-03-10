package sg.gov.tech.molbagencyportalbackend.repository

import org.springframework.data.jpa.repository.JpaRepository

fun <T, S> JpaRepository<T, S>.findOne(id: S): T? {
    val result = this.findById(id)
    return if (result.isPresent) result.get() else null
}
