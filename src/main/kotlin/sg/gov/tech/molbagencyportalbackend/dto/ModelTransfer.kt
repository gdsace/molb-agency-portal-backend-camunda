package sg.gov.tech.molbagencyportalbackend.dto

/**
 * Can put this in ResponseBodyAdvice to trigger toDTO automatically.
 * But it will hide the real response value rather than controller return value
 */
interface ModelTransfer<MODEL, DTO> {
    fun toDTO(model: MODEL): DTO
}

interface ResponseTransfer<MODEL, RESPONSE> {
    fun toResponse(model: MODEL): RESPONSE
}

interface RequestTransfer<REQUEST, MODEL> {
    fun fromRequest(request: REQUEST): MODEL
}
