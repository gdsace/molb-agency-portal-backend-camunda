package sg.gov.tech.molbagencyportalbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.fixture.ApproveApplicationRequestDTOFixture
import sg.gov.tech.testing.MolbIntegrationTesting

@MolbIntegrationTesting
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = ["classpath:fixtures/sql/seed_application_test_data.sql"])
internal class ApplicationControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should get 200 and valid response when endpoint and applicationNumber are valid`() {
        mockMvc
            .perform(
                get("/api/application/FC222002954")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.applicationNumber").value("FC222002954"))
            .andExpect(jsonPath("$.licenceNumber").value("ecd5dfba-3c1e-4921-b87e-150279146ae6"))
    }

    @Test
    fun `should get 200 and valid response even when reviewer has been deleted`() {
        mockMvc
            .perform(
                get("/api/application/FC222002954-deleted_user")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.applicationNumber").value("FC222002954-deleted_user"))
            .andExpect(jsonPath("$.reviewer.email").value("deleted_user@tech.gov.sg"))
            .andExpect(jsonPath("$.reviewer.isDeleted").value(true))
    }

    @Test
    fun `should get 404 when invalid endpoint and invalid applicationNumber`() {
        mockMvc
            .perform(
                get("/api/applications/FC222")
            )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should get NotFoundException when valid endpoint and invalid applicationNumber`() {
        mockMvc
            .perform(
                get("/api/application/FC222")
            )
            .andExpect(status().isNotFound)
            .andExpect(
                jsonPath("$.message")
                    .value(ExceptionControllerAdvice.NOT_FOUND_MESSAGE)
            )
    }

    @Test
    fun `should get 404 when invalid endpoint and valid applicationNumber`() {
        mockMvc
            .perform(
                get("/api/applications/FC222002954")
            )
            .andExpect(status().isNotFound)
    }

    @Nested
    inner class ApproveApplication {
        @Test
        fun `should return 404 when the application is not found`() {
            val mockLicenceFile =
                MockMultipartFile(
                    "licenceFile",
                    javaClass.classLoader.getResourceAsStream("valid_licence_test.pdf")!!
                )
            val approveApplicationJsonMultipart = MockMultipartFile(
                "approveFormData",
                "approveFormData",
                MediaType.APPLICATION_JSON_VALUE,
                ObjectMapper().writeValueAsBytes(ApproveApplicationRequestDTOFixture.ApproveApplicationRequestWithFiles)
            )
            mockMvc
                .perform(
                    multipart("/api/application/FC2220020000/approve").file(mockLicenceFile)
                        .file(approveApplicationJsonMultipart)
                )
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return ValidationException when Application cannot be approved`() {
            val mockLicenceFile =
                MockMultipartFile(
                    "licenceFile",
                    javaClass.classLoader.getResourceAsStream("valid_licence_test.pdf")!!
                )
            val approveApplicationJsonMultipart = MockMultipartFile(
                "approveFormData",
                "approveFormData",
                MediaType.APPLICATION_JSON_VALUE,
                ObjectMapper().writeValueAsBytes(ApproveApplicationRequestDTOFixture.ApproveApplicationRequestWithFiles)
            )
            mockMvc
                .perform(
                    multipart("/api/application/FC222002954/approve").file(mockLicenceFile)
                        .file(approveApplicationJsonMultipart)
                )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value("Application FC222002954 cannot be approved"))
        }

        @Test
        fun `should return ValidationException if licence number already exists`() {
            val mockLicenceFile =
                MockMultipartFile(
                    "licenceFile",
                    javaClass.classLoader.getResourceAsStream("valid_licence_test.pdf")!!
                )
            val approveApplicationJsonMultipart = MockMultipartFile(
                "approveFormData",
                "approveFormData",
                MediaType.APPLICATION_JSON_VALUE,
                ObjectMapper().writeValueAsBytes(
                    ApproveApplicationRequestDTOFixture.ApproveApplicationRequestWithFiles.copy(
                        licenceNumber = "FC222002Licence"
                    )
                )
            )
            mockMvc
                .perform(
                    multipart("/api/application/FC222002/approve").file(mockLicenceFile)
                        .file(approveApplicationJsonMultipart)
                )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value(ExceptionControllerAdvice.VALIDATION_ERROR_MESSAGE))
                .andExpect(jsonPath("$.errors[0].pointer").value("licenceNumber"))
                .andExpect(jsonPath("$.errors[0].message").value(ExceptionControllerAdvice.LICENCE_EXIST_MESSAGE))
        }

        @Test
        fun `should return 404 for invalid endpoint`() {
            val mockLicenceFile =
                MockMultipartFile(
                    "licenceFile",
                    javaClass.classLoader.getResourceAsStream("invalid_licence_file_type.docx")!!
                )
            val approveApplicationJsonMultipart = MockMultipartFile(
                "approveFormData",
                "approveFormData",
                MediaType.APPLICATION_JSON_VALUE,
                ObjectMapper().writeValueAsBytes(ApproveApplicationRequestDTOFixture.ApproveApplicationRequestWithFiles)
            )
            mockMvc
                .perform(
                    multipart("/api/applications/FC222002/approve").file(mockLicenceFile)
                        .file(approveApplicationJsonMultipart)
                )
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    inner class SendRFAForApplication {

        @Test
        fun `should return 404 for invalid endpoint`() {
            mockMvc.perform(
                get("/api/application/FC222002954/sendRFAs")
            ).andExpect(status().isNotFound)
        }
    }
}
