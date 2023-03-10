package sg.gov.tech.molbagencyportalbackend.service

import org.springframework.stereotype.Service
import sg.gov.tech.molbagencyportalbackend.model.Agency
import sg.gov.tech.molbagencyportalbackend.repository.AgencyRepository

@Service
class AgencyService(private val agencyRepository: AgencyRepository) {
    fun findByCode(code: String): Agency? = agencyRepository.findByCode(code)

    fun existByCode(code: String) = agencyRepository.existsByCode(code)
}
