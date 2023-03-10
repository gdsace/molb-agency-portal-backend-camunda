package sg.gov.tech.molbagencyportalbackend.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.utils.Masking

@Component
@ExcludeFromGeneratedCoverageTest
class EncryptLicenceDataUtil(
    private val masking: Masking
) {
    companion object {
        val typeToEncrypt = listOf("nric", "id_type", "edh_shareholderlist", "edh_appointments")
        val typeWithIdNumber = listOf("id_type", "edh_shareholderlist", "edh_appointments")
    }

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private fun tryUnmask(value: String, warningMessage: () -> String) = try {
        masking.unmask(value)
    } catch (e: IllegalArgumentException) {
        logger.warn(warningMessage())
        value
    }

    private fun encryptNodeValue(sectionValue: JsonNode, field: JsonNode, isEncryption: Boolean) {
        val toEncrypt: String
        if (typeWithIdNumber.contains(field.get("type").asText())) {
            toEncrypt = sectionValue.get(field.get("key").asText()).get("idNumber").textValue()
            val encryptedValue = if (isEncryption) {
                masking.mask(toEncrypt)
            } else {
                tryUnmask(toEncrypt) {
                    "Unmasked value found \"${field.get("key").asText()}\""
                }
            }
            (sectionValue.get(field.get("key").asText()) as ObjectNode).put("idNumber", encryptedValue)
        } else {
            toEncrypt = sectionValue.get(field.get("key").asText()).textValue()
            val encryptedValue = if (isEncryption) {
                masking.mask(toEncrypt)
            } else {
                tryUnmask(toEncrypt) {
                    "Unmasked value found \"${field.get("key").asText()}\""
                }
            }
            (sectionValue as ObjectNode).put(field.get("key").asText(), encryptedValue)
        }
    }

    private fun findSubaddableKeys(formMetaData: JsonNode): HashMap<String, String> {
        val subaddableKeysHashMap = hashMapOf<String, String>()
        val formMetaDataScheme = formMetaData.get("schema").get("applicationDetail")

        formMetaDataScheme.forEach { section ->
            section.get("fields")
                .filter { it.get("type").asText() == "subaddable" }
                .filter { it.get("attr")["subAddableSections"] != null }
                .forEach { field ->
                    val subaddableId =
                        field.get("attr")["subAddableSections"]["id"].asText()
                    val subaddableMetadata = formMetaDataScheme.single { it.get("id").asText() == subaddableId }
                    val subaddableKey = subaddableMetadata.get("key")
                    subaddableKeysHashMap[subaddableKey.asText()] = section["key"].asText()
                }
        }
        return subaddableKeysHashMap
    }

    private fun handleSubaddable(arrayItem: JsonNode, section: JsonNode, field: JsonNode, toEncrypt: Boolean) {
        val subaddableFields = arrayItem.get(section.get("key").asText())
        if (subaddableFields.isArray) {
            for (subaddableField in subaddableFields) {
                encryptNodeValue(subaddableField, field, toEncrypt)
            }
        }
    }

    private fun handleAddable(
        getSection: JsonNode,
        isSubaddable: Boolean,
        section: JsonNode,
        field: JsonNode,
        toEncrypt: Boolean
    ) {
        for (arrayItem in getSection) {
            // to handle Sub-addable
            if (isSubaddable) {
                handleSubaddable(arrayItem, section, field, toEncrypt)
            } else {
                encryptNodeValue(arrayItem, field, toEncrypt)
            }
        }
    }

    fun encryptLicenceNode(licenceDataValue: JsonNode, formMetaData: JsonNode, toEncrypt: Boolean): JsonNode {
        val subaddableKeysHashMap = findSubaddableKeys(formMetaData)
        val formMetaDataScheme = formMetaData.get("schema").get("applicationDetail")

        formMetaDataScheme.forEach { section ->
            section.get("fields")
                .filter { typeToEncrypt.contains(it.get("type").asText()) }
                .forEach { field ->
                    val isSubaddable = subaddableKeysHashMap.keys.contains(section.get("key").asText())
                    val getSection = if (isSubaddable) {
                        licenceDataValue.get(subaddableKeysHashMap[section.get("key").asText()])
                    } else {
                        licenceDataValue.get(section.get("key").asText())
                    }

                    if (getSection.isArray) {
                        handleAddable(getSection, isSubaddable, section, field, toEncrypt)
                    } else {
                        encryptNodeValue(getSection, field, toEncrypt)
                    }
                }
        }
        return licenceDataValue
    }
}
