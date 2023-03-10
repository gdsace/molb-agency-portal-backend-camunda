package sg.gov.tech.molbagencyportalbackend.dto

import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest

@ExcludeFromGeneratedCoverageTest
data class ErrorResponseDTO(
    val message: String,
    val errors: List<SubErrorDTO> = emptyList()
)

@ExcludeFromGeneratedCoverageTest
data class SubErrorDTO(
    val pointer: String,
    val rejectedValue: String,
    val message: String
)
