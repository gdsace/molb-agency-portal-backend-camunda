package sg.gov.tech.molbagencyportalbackend.controller

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.testing.MolbIntegrationTesting

@MolbIntegrationTesting
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = ["classpath:fixtures/sql/seed_application_test_data.sql"])
internal class LicenceControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should return 200 and valid response when valid licence number is provided`() {
        mockMvc
            .perform(
                get("/api/licence")
                    .param("licenceNumber", "FC222002Licence")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.licenceNumber").value("FC222002Licence"))
            .andExpect(jsonPath("$.licenceName").value("test1234567"))
    }

    @Test
    fun `should return NotFoundException when invalid licence number is provided`() {
        mockMvc
            .perform(
                get("/api/licence")
                    .param("licenceNumber", "missingLicence")
            )
            .andExpect(status().isNotFound)
            .andExpect(
                jsonPath("$.message")
                    .value(ExceptionControllerAdvice.NOT_FOUND_MESSAGE)
            )
    }

    @Test
    fun `should return null application when user is of different agency`() {
        mockMvc
            .perform(
                get("/api/licence")
                    .param("licenceNumber", "FC222002Licence2")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.licenceNumber").value("FC222002Licence2"))
            .andExpect(jsonPath("$.licenceName").value("test1234567"))
            .andExpect(jsonPath("$.applications").value(null))
    }

    fun `should return NotFound for invalid licence number for download document`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/licence/document")
                    .param("licenceNumber", "invalid_licence")
                    .param("documentId", "0cd63a32-4e46-4f5b-9d4d-c3d30fff77cc")
                    .param("documentName", "valid_licence_file.pdf")
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `should return bad request when download request parameter missing`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/licence/document")
                    .param("licenceNumber", "FC222002Licence")
                    .param("documentName", "valid_licence_file.pdf")
            ).andExpect(status().isInternalServerError)
    }

    @Nested
    inner class GetLicences {
        @Test
        fun `should throw 400 error due to missing params`() {
            mockMvc
                .perform(
                    get("/api/licences")
                        .param("tab", "agencyLicences")
                        .param("sortField", "application.applicantName")
                        .param("sortOrder", "asc")
                        .param("limit", "10")
                )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should throw 400 error due to invalid params`() {
            mockMvc
                .perform(
                    get("/api/licences")
                        .param("tab", "agencyLicencesASD")
                        .param("page", "0")
                        .param("sortField", "application.applicantName")
                        .param("sortOrder", "asc")
                        .param("limit", "10")
                )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should return status 200 for retrieving user agency licences if valid payload is passed`() {

            mockMvc
                .perform(
                    get("/api/licences")
                        .param("tab", "agencyLicences")
                        .param("page", "0")
                        .param("sortField", "application.applicantName")
                        .param("sortOrder", "asc")
                        .param("limit", "10")
                )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.agencyLicenceCount").value(1))
                .andExpect(jsonPath("$.licenceCount").value(1))
                .andExpect(jsonPath("$.licences[0].licenceNumber").value("FC222002Licence"))
        }

        @Test
        fun `should return status 200 for retrieving other licences if valid payload is passed`() {

            mockMvc
                .perform(
                    get("/api/licences")
                        .param("tab", "otherLicences")
                        .param("page", "0")
                        .param("sortField", "application.applicantName")
                        .param("sortOrder", "asc")
                        .param("limit", "10")
                )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.agencyLicenceCount").value(1))
                .andExpect(jsonPath("$.licenceCount").value(1))
                .andExpect(jsonPath("$.licences[0].licenceNumber").value("FC222002Licence2"))
        }
    }
}
