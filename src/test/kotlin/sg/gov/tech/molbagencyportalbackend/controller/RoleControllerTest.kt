package sg.gov.tech.molbagencyportalbackend.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import sg.gov.tech.testing.MolbIntegrationTesting

@MolbIntegrationTesting
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = ["classpath:fixtures/sql/seed_role_test_data.sql"])
internal class RoleControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should get 200 OK and valid response when endpoint is valid`() {

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/roles")
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(4))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Helpdesk (L2)"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].code").value("helpdesk"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Agency Supervisor"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].code").value("agency_supervisor"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].name").value("Agency Officer"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].code").value("agency_officer"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].name").value("Agency Officer (Read Only)"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].code").value("agency_officer_ro"))
    }

    @Test
    fun `should return 404 when invalid endpoint`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/role")
            )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}
