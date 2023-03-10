package sg.gov.tech.molbagencyportalbackend.dao

interface JwtDao {

    fun add(email: String, jti: String)

    fun contains(email: String, jti: String): Boolean

    fun remove(email: String)
}
