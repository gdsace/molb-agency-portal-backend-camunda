package sg.gov.tech.molbagencyportalbackend.util

import io.mockk.impl.annotations.InjectMockKs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.fixture.ApplicationFixture
import sg.gov.tech.molbagencyportalbackend.fixture.CreateApplicationRequestDTOFixture
import sg.gov.tech.molbagencyportalbackend.integration.dds.DDSRequestHelper
import sg.gov.tech.testing.MolbUnitTesting

@MolbUnitTesting
internal class DDSRequestHelperTest {
    @InjectMockKs
    private lateinit var ddsRequestHelper: DDSRequestHelper

    @Test
    fun `should create dds upload metadata  with applicant info`() {
        val applicationWithNoFiler = ApplicationFixture.createApplication_SingpassSelf

        val uploadMetadata = ddsRequestHelper.getDDSRequestBodyMetaData("testLic123", applicationWithNoFiler)
        assertEquals(uploadMetadata.owners[0]?.uinfin, applicationWithNoFiler.applicant.id.idNumber)
        assertEquals(uploadMetadata.owners.size, 1)
        assertEquals(uploadMetadata.agency, applicationWithNoFiler.agency.code)
        assertEquals(uploadMetadata.domainReferenceId, "testLic123")
    }

    @Test
    fun `should create dds upload metadata with applicant and filer info`() {
        val applicationWithApplicantAndFiler = ApplicationFixture.createApplication_SingpassSelf.copy(
            filer = CreateApplicationRequestDTOFixture.filer
        )

        val uploadMetadata = ddsRequestHelper.getDDSRequestBodyMetaData("testLic123", applicationWithApplicantAndFiler)
        assertEquals(uploadMetadata.owners[0]?.uinfin, applicationWithApplicantAndFiler.applicant.id.idNumber)
        assertEquals(uploadMetadata.owners[1]?.uinfin, applicationWithApplicantAndFiler.filer.id.idNumber)
        assertEquals(uploadMetadata.owners.size, 2)
        assertEquals(uploadMetadata.agency, applicationWithApplicantAndFiler.agency.code)
        assertEquals(uploadMetadata.domainReferenceId, "testLic123")
    }
}
