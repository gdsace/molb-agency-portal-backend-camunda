package sg.gov.tech.molbagencyportalbackend.controller

import org.hamcrest.Matchers
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.util.UriComponentsBuilder
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.CreateUserRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.EditUserRequestDTO
import sg.gov.tech.molbagencyportalbackend.exception.ExceptionControllerAdvice
import sg.gov.tech.molbagencyportalbackend.jsonContent
import sg.gov.tech.testing.MolbIntegrationTesting

@MolbIntegrationTesting
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = ["classpath:fixtures/sql/seed_user_test_data.sql"])
internal class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Nested
    inner class GetUsers {
        @Test
        fun `should get only non-deleted records`() {
            mockMvc
                .perform(
                    get("/api/users?page=0&sortField=name&sortOrder=desc&limit=10")
                )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.totalCount").value(5))
        }

        @Test
        fun `should get 200 OK and valid response when endpoint and parameters passed are valid`() {
            mockMvc
                .perform(
                    get("/api/users?page=0&sortField=name&sortOrder=desc&limit=1")
                )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data[0].name").value("test4"))
                .andExpect(jsonPath("$.data[0].accountStatus").value("Active"))
        }

        @Test
        fun `should get only 2 records when limit is 2 and page is 0`() {
            mockMvc
                .perform(
                    get("/api/users?page=0&sortField=name&sortOrder=desc&limit=2")
                )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.totalCount").value(5))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("test4"))
                .andExpect(jsonPath("$.data[1].name").value("test3"))
        }

        @Test
        fun `should get only 1 records when limit is 2 and page is 2`() {
            mockMvc
                .perform(
                    get("/api/users?page=2&sortField=name&sortOrder=desc&limit=2")
                )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.totalCount").value(5))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("test0"))
        }

        @Test
        fun `should get 404 not found when the endpoint is invalid`() {
            mockMvc
                .perform(
                    get("/api/users?page=0&sortField=name&sortOrder=desc&limit=1")
                )
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should get 400 Bad Request when invalid limit parameter passed`() {
            mockMvc
                .perform(
                    get("/api/users?page=0&sortField=name&sortOrder=desc&limit=0")
                )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value(ExceptionControllerAdvice.VALIDATION_ERROR_MESSAGE))
                .andExpect(jsonPath("$.errors[0].pointer").value("limit"))
                .andExpect(jsonPath("$.errors[0].message").value(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE))
        }

        @Test
        fun `should get 400 Bad Request when invalid page parameter passed`() {
            mockMvc
                .perform(
                    get("/api/users?page=-1&sortField=name&sortOrder=desc&limit=1")
                )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value(ExceptionControllerAdvice.VALIDATION_ERROR_MESSAGE))
                .andExpect(jsonPath("$.errors[0].pointer").value("page"))
                .andExpect(jsonPath("$.errors[0].message").value(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE))
        }

        @Test
        fun `should get 400 Bad Request when invalid sort order parameter passed`() {
            mockMvc
                .perform(
                    get("/api/users?page=1&sortField=name&sortOrder=de&limit=1")
                )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value(ExceptionControllerAdvice.VALIDATION_ERROR_MESSAGE))
                .andExpect(jsonPath("$.errors[0].pointer").value("sortOrder"))
                .andExpect(jsonPath("$.errors[0].message").value(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE))
        }

        /**
         * page = 1, limit = 9 number of records in DB = 5
         */
        @Test
        fun `should get empty array when page and limit value is more than the records present in DB`() {
            mockMvc
                .perform(
                    get("/api/users?page=1&sortField=name&sortOrder=desc&limit=9")
                )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data").isEmpty)
        }
    }

    @Nested
    inner class GetSingleUser {
        @Test
        fun `should get 200 OK and valid response when user id is valid`() {
            mockMvc
                .perform(
                    get("/api/user/1")
                )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("test0"))
        }

        @Test
        fun `should return 404 if the user is not found`() {
            val userId = 7
            mockMvc
                .perform(delete("/api/user/$userId"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return 403 if user is not in the same agency as principal`() {
            val userId = "6"
            mockMvc
                .perform(delete("/api/user/$userId"))
                .andExpect(status().isForbidden)
                .andExpect(
                    jsonPath("$.message")
                        .value("User is not in the same agency as Principal")
                )
        }
    }

    @Nested
    inner class CreateUser {
        @Test
        fun `should throw 400 error due to empty mandatory fields`() {
            val uri = UriComponentsBuilder.fromUriString("/api/user").build().toUri()

            val requestDTO = CreateUserRequestDTO(
                name = "",
                email = "",
                role = "",
                accountStatus = ""
            )

            mockMvc
                .perform(post(uri).jsonContent(requestDTO))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value(ExceptionControllerAdvice.VALIDATION_ERROR_MESSAGE))
                .andExpect(
                    jsonPath(
                        "$.errors[*].pointer",
                        Matchers.hasItems(
                            "name",
                            "email",
                            "role",
                            "accountStatus"
                        )
                    )
                )
                .andExpect(jsonPath("$.errors[0].message").value(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE))
                .andExpect(jsonPath("$.errors[1].message").value(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE))
                .andExpect(jsonPath("$.errors[2].message").value(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE))
                .andExpect(jsonPath("$.errors[3].message").value(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE))
        }

        @Test
        fun `should throw 400 error due to invalid values`() {
            val uri = UriComponentsBuilder.fromUriString("/api/user").build().toUri()

            val requestDTO = CreateUserRequestDTO(
                name = "Test",
                email = "test_create@tech.gov.sg",
                role = "invalid_role",
                accountStatus = "invalid_status"
            )

            mockMvc
                .perform(post(uri).jsonContent(requestDTO))
                .andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath(
                        "$.errors[*].message",
                        Matchers.containsInAnyOrder(
                            "role value is invalid",
                            "accountStatus value is invalid"
                        )
                    )
                )
        }

        @Test
        fun `should throw 400 error due to user email already exists`() {
            val uri = UriComponentsBuilder.fromUriString("/api/user").build().toUri()

            val requestDTO = CreateUserRequestDTO(
                name = "Test",
                email = "test0@test.com",
                role = "agency_officer",
                accountStatus = "ACTIVE"
            )

            mockMvc
                .perform(post(uri).jsonContent(requestDTO))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0].message").value(ExceptionControllerAdvice.EMAIL_EXIST_MESSAGE))
        }

        @Test
        fun `should throw 400 error due to user email already exists even if in different case`() {
            val uri = UriComponentsBuilder.fromUriString("/api/user").build().toUri()

            val requestDTO = CreateUserRequestDTO(
                name = "Test",
                email = "TEST0@TEST.COM",
                role = "agency_officer",
                accountStatus = "ACTIVE"
            )

            mockMvc
                .perform(post(uri).jsonContent(requestDTO))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0].message").value(ExceptionControllerAdvice.EMAIL_EXIST_MESSAGE))
        }

        @Test
        fun `should throw 400 error due to user email not is supported domain`() {
            val uri = UriComponentsBuilder.fromUriString("/api/user").build().toUri()

            val requestDTO = CreateUserRequestDTO(
                name = "Test",
                email = "test_create@test.com",
                role = "agency_officer",
                accountStatus = "ACTIVE"
            )

            mockMvc
                .perform(post(uri).jsonContent(requestDTO))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value(ExceptionControllerAdvice.VALIDATION_ERROR_MESSAGE))
                .andExpect(jsonPath("$.errors[0].pointer").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE))
        }

        @Test
        fun `should throw 400 error due to principal and user not in the same agency`() {
            val uri = UriComponentsBuilder.fromUriString("/api/user").build().toUri()

            val requestDTO = CreateUserRequestDTO(
                name = "Test",
                email = "test_create@test.gov.sg",
                role = "agency_officer",
                accountStatus = "ACTIVE"
            )

            mockMvc
                .perform(post(uri).jsonContent(requestDTO))
                .andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath("$.errors[0].message")
                        .value(ExceptionControllerAdvice.ADD_ONLY_SAME_AGENCY_MESSAGE)
                )
        }

        @Test
        fun `should throw 404 error due to principal not found`() {
            val uri = UriComponentsBuilder.fromUriString("/api/user").build().toUri()

            val requestDTO = CreateUserRequestDTO(
                name = "Test",
                email = "test_create@moe.gov.sg",
                role = "agency_officer",
                accountStatus = "ACTIVE"
            )

            mockMvc
                .perform(post(uri).jsonContent(requestDTO))
                .andExpect(status().isBadRequest)
                .andExpect(
                    jsonPath("$.errors[0].message")
                        .value(ExceptionControllerAdvice.ADD_ONLY_SAME_AGENCY_MESSAGE)
                )
        }

        @Test
        fun `should return status 201 and create the user if valid payload is passed`() {
            val uri = UriComponentsBuilder.fromUriString("/api/user").build().toUri()

            val requestDTO = CreateUserRequestDTO(
                name = "Test",
                email = "test_create@tech.gov.sg",
                role = "agency_officer",
                accountStatus = "INACTIVE"
            )

            mockMvc
                .perform(post(uri).jsonContent(requestDTO))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.email").value("test_create@tech.gov.sg"))
                .andExpect(jsonPath("$.role.id").value(4))
                .andExpect(jsonPath("$.role.code").value("agency_officer"))
                .andExpect(jsonPath("$.role.name").value("Agency Officer"))
                .andExpect(jsonPath("$.accountStatus").value("Inactive"))
        }

        @Test
        fun `should return status 201 and create the user with lowercase email`() {
            val uri = UriComponentsBuilder.fromUriString("/api/user").build().toUri()

            val requestDTO = CreateUserRequestDTO(
                name = "Test Case",
                email = "TEST_CREATE_CASE@TECH.GOV.SG",
                role = "agency_officer",
                accountStatus = "INACTIVE"
            )

            mockMvc
                .perform(post(uri).jsonContent(requestDTO))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.name").value("Test Case"))
                .andExpect(jsonPath("$.email").value("test_create_case@tech.gov.sg"))
                .andExpect(jsonPath("$.role.id").value(4))
                .andExpect(jsonPath("$.role.code").value("agency_officer"))
                .andExpect(jsonPath("$.role.name").value("Agency Officer"))
                .andExpect(jsonPath("$.accountStatus").value("Inactive"))
        }
    }

    @Nested
    inner class EditUser {
        @Test
        fun `should throw 400 error due to empty mandatory fields`() {
            val userId = "1"
            val uri = UriComponentsBuilder.fromUriString("/api/user/$userId").build().toUri()

            val requestDTO = EditUserRequestDTO(
                name = "",
                role = "",
                accountStatus = ""
            )

            mockMvc
                .perform(put(uri).jsonContent(requestDTO))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value(ExceptionControllerAdvice.VALIDATION_ERROR_MESSAGE))
                .andExpect(
                    jsonPath(
                        "$.errors[*].pointer",
                        Matchers.hasItems(
                            "name",
                            "role",
                            "accountStatus"
                        )
                    )
                )
                .andExpect(jsonPath("$.errors[0].message").value(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE))
                .andExpect(jsonPath("$.errors[1].message").value(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE))
                .andExpect(jsonPath("$.errors[2].message").value(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE))
        }

        @Test
        fun `should throw 400 error due to invalid values`() {
            val userId = "1"
            val uri = UriComponentsBuilder.fromUriString("/api/user/$userId").build().toUri()

            val requestDTO = EditUserRequestDTO(
                name = "Test",
                role = "invalid_role",
                accountStatus = "invalid_status"
            )

            mockMvc
                .perform(put(uri).jsonContent(requestDTO))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value(ExceptionControllerAdvice.VALIDATION_ERROR_MESSAGE))
                .andExpect(
                    jsonPath(
                        "$.errors[*].pointer",
                        Matchers.hasItems(
                            "role",
                            "accountStatus"
                        )
                    )
                )
                .andExpect(jsonPath("$.errors[0].message").value(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE))
                .andExpect(jsonPath("$.errors[1].message").value(ExceptionControllerAdvice.INVALID_VALUE_MESSAGE))
        }

        @Test
        fun `should throw 403 if user is not in the same agency as principal`() {
            val userId = "6"
            val uri = UriComponentsBuilder.fromUriString("/api/user/$userId").build().toUri()

            val requestDTO = EditUserRequestDTO(
                name = "Test",
                role = "agency_officer",
                accountStatus = "ACTIVE"
            )

            mockMvc
                .perform(put(uri).jsonContent(requestDTO))
                .andExpect(status().isForbidden)
                .andExpect(
                    jsonPath("$.message")
                        .value(ExceptionControllerAdvice.ACC_NO_PERMISSION_MESSAGE)
                )
        }

        @Test
        fun `should return status 200 and update the user if valid payload is passed`() {
            val userId = "1"
            val uri = UriComponentsBuilder.fromUriString("/api/user/$userId").build().toUri()

            val requestDTO = EditUserRequestDTO(
                name = "Test",
                role = "agency_officer",
                accountStatus = "INACTIVE"
            )

            mockMvc
                .perform(put(uri).jsonContent(requestDTO))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.email").value("test0@test.com"))
                .andExpect(jsonPath("$.role.id").value(4))
                .andExpect(jsonPath("$.role.code").value("agency_officer"))
                .andExpect(jsonPath("$.role.name").value("Agency Officer"))
                .andExpect(jsonPath("$.accountStatus").value("Inactive"))
        }
    }

    @Nested
    inner class DeleteUser {
        @Test
        fun `should delete the user if valid id is passed`() {
            val userId = 4
            mockMvc
                .perform(delete("/api/user/$userId"))
                .andExpect(status().isNoContent)
        }

        @Test
        fun `should return 404 if the user is not found`() {
            val userId = 7
            mockMvc
                .perform(delete("/api/user/$userId"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should return 400 if invalid userId is passed`() {
            val userId = "c"
            mockMvc
                .perform(delete("/api/user/$userId"))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 403 if user is not in the same agency as principal`() {
            val userId = "6"
            mockMvc
                .perform(delete("/api/user/$userId"))
                .andExpect(status().isForbidden)
                .andExpect(
                    jsonPath("$.message")
                        .value("User is not in the same agency as Principal")
                )
        }
    }
}
