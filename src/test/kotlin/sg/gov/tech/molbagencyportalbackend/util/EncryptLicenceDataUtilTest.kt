package util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.dto.l1t.VersionDTO
import sg.gov.tech.molbagencyportalbackend.util.EncryptLicenceDataUtil
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.utils.Masking
import sg.gov.tech.utils.ObjectMapperConfigurer

@MolbUnitTesting
internal class EncryptLicenceDataUtilTest {
    @MockK
    private lateinit var masking: Masking

    @InjectMockKs
    private lateinit var encryptLicenceDataUtil: EncryptLicenceDataUtil

    private val maskedValue = "maskedValue"
    private val unmaskedValue = "unmaskedValue"
    private val dummyIdNumber = "S1234565A"

    private val idTypeField = mapOf<String, Any>("key" to "idField", "type" to "id_type")
    private val nricField = mapOf<String, Any>("key" to "nricField", "type" to "nric")
    private val textField = mapOf<String, Any>("key" to "textField", "type" to "textfield")
    private val subaddableField = mapOf(
        "key" to "subaddable1", "type" to "subaddable",
        "attr" to mapOf(
            "subAddableSections" to mapOf(
                "id" to "xxx001",
                "key" to "subaddable1"
            )
        )
    )
    private val normalSection =
        mapOf("id" to "xxx003", "key" to "normalSection", "fields" to listOf(idTypeField, nricField))

    private val versionDTO = VersionDTO(
        agencyCode = "abc",
        agencyLicenceType = "20220727",
        agencyName = "ABC Company",
        agencyOperationType = "Business",
        formName = "Application for example application Form",
        id = "34gfd34",
        licenceName = "Application for example application",
        operationTypeId = "12345-HGFDCV034RF-W34F",
        schema = emptyMap(),
        settings = emptyMap(),
        status = "LIVE",
    )

    @BeforeEach
    fun setup() {
        every { masking.mask(any()) } returns maskedValue
        every { masking.unmask(any()) } returns unmaskedValue
    }

    @Test
    fun `should encrypt sensitive info`() {
        val licenceDataField: JsonNode =
            ObjectMapperConfigurer.GlobalObjectMapper.convertValue(
                mapOf(
                    "normalSection" to mapOf(
                        "idField" to mapOf("idNumber" to dummyIdNumber, "idType" to "nric"),
                        "nricField" to dummyIdNumber
                    )
                )
            )
        versionDTO.schema = mapOf<String, Any>("applicationDetail" to listOf(normalSection))
        val formMetaDataScheme: JsonNode = ObjectMapperConfigurer.GlobalObjectMapper.convertValue(versionDTO)

        val result = encryptLicenceDataUtil.encryptLicenceNode(licenceDataField, formMetaDataScheme, true)

        assertEquals(result["normalSection"]["nricField"].asText(), maskedValue)
        assertEquals(result["normalSection"]["idField"]["idNumber"].asText(), maskedValue)
    }

    @Test
    fun `should encrypt sensitive info in addable and subaddable`() {
        val subaddableKeyField =
            mapOf("id" to "xxx001", "key" to "subaddable1", "fields" to listOf(idTypeField, nricField))
        val addableSection =
            mapOf(
                "id" to "xxx002",
                "key" to "addableSection",
                "fields" to listOf(idTypeField, nricField, subaddableField),
                "maximumDuplication" to 3
            )
        val schema = mapOf<String, Any>("applicationDetail" to listOf(addableSection, subaddableKeyField))

        versionDTO.schema = schema
        val formMetaDataScheme: JsonNode = ObjectMapperConfigurer.GlobalObjectMapper.convertValue(versionDTO)
        val addableSectionData = listOf(
            mapOf(
                "idField" to mapOf("idNumber" to dummyIdNumber, "idType" to "nric"),
                "nricField" to dummyIdNumber,
                "subaddable1" to listOf(
                    mapOf(
                        "idField" to mapOf("idNumber" to dummyIdNumber, "idType" to "nric"),
                        "nricField" to dummyIdNumber
                    )
                )
            )
        )
        val licenceDataField: JsonNode =
            ObjectMapperConfigurer.GlobalObjectMapper.convertValue(mapOf("addableSection" to addableSectionData))

        val result = encryptLicenceDataUtil.encryptLicenceNode(licenceDataField, formMetaDataScheme, true)

        assertEquals(result["addableSection"][0]["nricField"].asText(), maskedValue)
        assertEquals(result["addableSection"][0]["idField"]["idNumber"].asText(), maskedValue)
        assertEquals(result["addableSection"][0]["subaddable1"][0]["nricField"].asText(), maskedValue)
        assertEquals(result["addableSection"][0]["subaddable1"][0]["idField"]["idNumber"].asText(), maskedValue)
    }

    @Test
    fun `should not encrypt non-sensitive info`() {
        versionDTO.schema = mapOf<String, Any>(
            "applicationDetail" to listOf(
                mapOf(
                    "id" to "xxx002",
                    "key" to "addableField",
                    "fields" to listOf(textField, nricField, subaddableField),
                    "maximumDuplication" to 3
                ),
                mapOf("id" to "xxx001", "key" to "subaddable1", "fields" to listOf(textField, nricField)),
                mapOf("id" to "xxx003", "key" to "normalSection", "fields" to listOf(textField, nricField))
            )
        )

        val licenceDataField: JsonNode =
            ObjectMapperConfigurer.GlobalObjectMapper.convertValue(
                mapOf(
                    "addableField" to listOf(
                        mapOf(
                            "textField" to "someTextValue",
                            "nricField" to dummyIdNumber,
                            "subaddable1" to listOf(
                                mapOf(
                                    "textField" to "someTextValue",
                                    "nricField" to dummyIdNumber
                                )
                            )
                        )
                    ),
                    "normalSection" to mapOf(
                        "textField" to "someTextValue",
                        "nricField" to dummyIdNumber
                    )
                )
            )
        val formMetaDataScheme: JsonNode = ObjectMapperConfigurer.GlobalObjectMapper.convertValue(versionDTO)
        val result = encryptLicenceDataUtil.encryptLicenceNode(licenceDataField, formMetaDataScheme, true)

        assertEquals(result["normalSection"]["textField"].asText(), "someTextValue")
        assertEquals(result["addableField"][0]["textField"].asText(), "someTextValue")
        assertEquals(result["addableField"][0]["subaddable1"][0]["textField"].asText(), "someTextValue")
        assertEquals(result["addableField"][0]["subaddable1"][0]["nricField"].asText(), maskedValue)
    }

    @Test
    fun `should decrypt sensitive info in normal, addable and subaddable`() {
        every { masking.unmask(any()) } returns unmaskedValue

        val subaddableKeyField =
            mutableMapOf("id" to "xxx001", "key" to "subaddable1", "fields" to listOf(idTypeField, nricField))
        val addableSection =
            mapOf(
                "id" to "xxx002",
                "key" to "addableSection",
                "fields" to listOf(idTypeField, nricField, subaddableField),
                "maximumDuplication" to 3
            )
        val schema =
            mutableMapOf<String, Any>("applicationDetail" to listOf(normalSection, addableSection, subaddableKeyField))

        versionDTO.schema = schema

        val normalSectionData = mapOf(
            "idField" to mapOf("idNumber" to maskedValue, "idType" to "nric"),
            "nricField" to maskedValue
        )
        val subaddableFieldData = listOf(
            mapOf(
                "idField" to mapOf("idNumber" to unmaskedValue, "idType" to "nric"),
                "nricField" to unmaskedValue
            )
        )
        val addableSectionData = listOf(
            mapOf(
                "idField" to mapOf("idNumber" to unmaskedValue, "idType" to "nric"),
                "nricField" to unmaskedValue,
                "subaddable1" to subaddableFieldData
            )
        )

        val licenceDataField: JsonNode =
            ObjectMapperConfigurer.GlobalObjectMapper.convertValue(
                mapOf(
                    "addableSection" to addableSectionData,
                    "normalSection" to normalSectionData
                )
            )
        val formMetaDataScheme: JsonNode = ObjectMapperConfigurer.GlobalObjectMapper.convertValue(versionDTO)
        val result = encryptLicenceDataUtil.encryptLicenceNode(licenceDataField, formMetaDataScheme, false)

        assertEquals(result["normalSection"]["nricField"].asText(), unmaskedValue)
        assertEquals(result["normalSection"]["idField"]["idNumber"].asText(), unmaskedValue)
        assertEquals(result["addableSection"][0]["nricField"].asText(), unmaskedValue)
        assertEquals(result["addableSection"][0]["idField"]["idNumber"].asText(), unmaskedValue)
        assertEquals(result["addableSection"][0]["subaddable1"][0]["nricField"].asText(), unmaskedValue)
        assertEquals(result["addableSection"][0]["subaddable1"][0]["idField"]["idNumber"].asText(), unmaskedValue)
    }

    @Test
    fun `should not decrypt non-sensitive info`() {
        val subaddableKeyField =
            mutableMapOf("id" to "xxx001", "key" to "subaddable1", "fields" to listOf(textField, nricField))
        val normalSection = mapOf("id" to "xxx002", "key" to "normalSection", "fields" to listOf(textField, nricField))
        val addableField =
            mapOf(
                "id" to "xxx003",
                "key" to "addableField",
                "fields" to listOf(textField, nricField, subaddableField),
                "maximumDuplication" to 3
            )

        versionDTO.schema =
            mapOf<String, Any>("applicationDetail" to listOf(addableField, subaddableKeyField, normalSection))

        val normalSectionData = mapOf(
            "textField" to "someTextValue",
            "nricField" to "S1234565A"
        )
        val addableSectionData = listOf(
            mapOf(
                "textField" to "someTextValue",
                "nricField" to "S1234565A",
                "subaddable1" to listOf(
                    mapOf(
                        "textField" to "someTextValue",
                        "nricField" to "S1234565A"
                    )
                )
            )
        )

        val licenceDataField: JsonNode =
            ObjectMapperConfigurer.GlobalObjectMapper.convertValue(
                mapOf(
                    "addableField" to addableSectionData,
                    "normalSection" to normalSectionData
                )
            )
        val formMetaDataScheme: JsonNode = ObjectMapperConfigurer.GlobalObjectMapper.convertValue(versionDTO)
        val result = encryptLicenceDataUtil.encryptLicenceNode(licenceDataField, formMetaDataScheme, false)

        assertEquals(result["normalSection"]["textField"].asText(), "someTextValue")
        assertEquals(result["addableField"][0]["textField"].asText(), "someTextValue")
        assertEquals(result["addableField"][0]["subaddable1"][0]["textField"].asText(), "someTextValue")
        assertEquals(result["addableField"][0]["subaddable1"][0]["nricField"].asText(), unmaskedValue)
    }
}
