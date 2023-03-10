package sg.gov.tech.molbagencyportalbackend.service

import org.springframework.stereotype.Service
import sg.gov.tech.molbagencyportalbackend.model.LicenceType
import sg.gov.tech.molbagencyportalbackend.repository.LicenceTypeRepository

@Service
class LicenceTypeService(private val licenceTypeRepository: LicenceTypeRepository) {
    fun findByLicenceId(licenceId: String): LicenceType? =
        licenceTypeRepository.findByLicenceId(licenceId)

    fun existByLicenceId(licenceId: String) = licenceTypeRepository.existsByLicenceId(licenceId)
}
