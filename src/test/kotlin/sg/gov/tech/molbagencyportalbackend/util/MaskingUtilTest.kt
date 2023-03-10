package sg.gov.tech.molbagencyportalbackend.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sg.gov.tech.molbagencyportalbackend.util.MaskingUtil.maskLicenceDataField
import sg.gov.tech.testing.MolbUnitTesting
import sg.gov.tech.utils.maskForDisplay

@MolbUnitTesting
internal class MaskingUtilTest {

    private val mapper = ObjectMapper()
    private val licenceJsonString = """
        {
            "section": {
                "nric": "S1111111N",
                "id": {
                  "idNumber": "S1111111N"
                },
                "shareholders": {
                  "idNumber": "S1111111N"
                },
                "appointments": {
                  "idNumber": "S1111111N"
                }
            },
            "addable": [
                {
                    "nric": "S1111111N",
                    "id": {
                      "idNumber": "S1111111N"
                    },
                    "shareholders": {
                      "idNumber": "S1111111N"
                    },
                    "appointments": {
                      "idNumber": "S1111111N"
                    },
                    "subaddable": [
                        {
                            "nric": "S1111111N",
                            "id": {
                              "idNumber": "S1111111N"
                            },
                            "shareholders": {
                              "idNumber": "S1111111N"
                            },
                            "appointments": {
                              "idNumber": "S1111111N"
                            }
                        }
                    ]
                }
            ]
        }
    """.trimIndent()
    private val formMetaDataJsonString = """
        {
            "schema": {
                "applicationDetail": [
                    {
                        "id": "xxx001",
                        "key": "section",
                        "fields": [
                            {
                                "key": "nric",
                                "type": "nric"
                            },
                            {
                                "key": "id",
                                "type": "id_type"
                            },
                            {
                                "key": "shareholders",
                                "type": "edh_shareholderlist"
                            },
                            {
                                "key": "appointments",
                                "type": "edh_appointments"
                            }
                            ],
                        "header": "Section"
                    },
                    {
                        "id": "xxx002",
                        "key": "addable",
                        "fields": [
                            {
                                "key": "nric",
                                "type": "nric"
                            },
                            {
                                "key": "id",
                                "type": "id_type"
                            },
                            {
                                "key": "shareholders",
                                "type": "edh_shareholderlist"
                            },
                            {
                                "key": "appointments",
                                "type": "edh_appointments"
                            },
                            {
                            "key": "subAddable",
                            "attr": {
                                "subAddableSections": {
                                    "id": "xxx003",
                                    "key": "subaddable"
                                }
                            },
                                "type": "subaddable"
                            }
                        ],
                    "header": "Section",
                    "maximumDuplication": 3
                    },
                    {
                        "id": "xxx003",
                        "key": "subaddable",
                        "fields": [
                            {
                                "key": "nric",
                                "type": "nric"
                            },
                            {
                                "key": "id",
                                "type": "id_type"
                            },
                            {
                                "key": "shareholders",
                                "type": "edh_shareholderlist"
                            },
                            {
                                "key": "appointments",
                                "type": "edh_appointments"
                            }
                        ],
                        "header": "subaddable"
                    }
                ]
            }
        }
    """.trimIndent()

    @Test
    fun `should return licence data fields with id masked for nric field type`() {
        val licenceDataFields: JsonNode = mapper.readTree(licenceJsonString)

        val formMetaData: JsonNode = mapper.readTree(formMetaDataJsonString)

        licenceDataFields.maskLicenceDataField(formMetaData, String::maskForDisplay)

        assertEquals("*****111N", licenceDataFields.get("section").get("nric").textValue())
        assertEquals("*****111N", licenceDataFields.get("section").get("id").get("idNumber").textValue())
        assertEquals("*****111N", licenceDataFields.get("section").get("shareholders").get("idNumber").textValue())
        assertEquals("*****111N", licenceDataFields.get("section").get("appointments").get("idNumber").textValue())

        assertEquals("*****111N", licenceDataFields.get("addable").get(0).get("nric").textValue())
        assertEquals("*****111N", licenceDataFields.get("addable").get(0).get("id").get("idNumber").textValue())
        assertEquals(
            "*****111N",
            licenceDataFields.get("addable").get(0).get("shareholders").get("idNumber").textValue()
        )
        assertEquals(
            "*****111N",
            licenceDataFields.get("addable").get(0).get("appointments").get("idNumber").textValue()
        )

        assertEquals(
            "*****111N",
            licenceDataFields.get("addable").get(0).get("subaddable").get(0).get("nric").textValue()
        )
        assertEquals(
            "*****111N",
            licenceDataFields.get("addable").get(0).get("subaddable").get(0).get("id").get("idNumber").textValue()
        )
        assertEquals(
            "*****111N",
            licenceDataFields.get("addable").get(0).get("subaddable").get(0).get("shareholders").get("idNumber")
                .textValue()
        )
        assertEquals(
            "*****111N",
            licenceDataFields.get("addable").get(0).get("subaddable").get(0).get("appointments").get("idNumber")
                .textValue()
        )
    }
}
