package sg.gov.tech.molbagencyportalbackend.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.slf4j.LoggerFactory

object MaskingUtil {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private const val ID_TYPE = "id_type"
    private const val EDH_SHAREHOLDERLIST = "edh_shareholderlist"
    private const val EDH_APPOINTMENTS = "edh_appointments"
    private const val NRIC = "nric"
    private const val SUBADDABLE = "subaddable"

    /**
     * mask PII fields in licence data fields.
     * encryption and decryption methods can be passed as arguments.
     * no need for return value in function since we are manipulating the json nodes.
     */
    fun JsonNode.maskLicenceDataField(
        formMetaData: JsonNode,
        maskingFunc: String.() -> String
    ) {
        // key: key of section; value: list of id fields, with type and key associated to the id field
        val sectionWithIdKeyPair = getSectionWithIdKeyPair(formMetaData)

        // done iterating through formMetaData

        val licenceDataFields = this // for readability

        sectionWithIdKeyPair
            // filter away keys that belongs to the subaddable section
            .filter { (section, _) -> licenceDataFields.hasNonNull(section) }
            // for each section with id fields
            .forEach { (section, list) ->
                if (licenceDataFields.get(section).isObject) {
                    iterateIdTypeKeyPairList(licenceDataFields.get(section), list, maskingFunc)
                } else if (licenceDataFields.get(section).isArray) {
                    // section is addable
                    licenceDataFields.get(section).forEach {
                        iterateIdTypeKeyPairList(it, list, maskingFunc, sectionWithIdKeyPair)
                    }
                } else {
                    // something is wrong, log it down
                    logger.error("section $section is not object or array: ${licenceDataFields.get(section).nodeType}")
                }
            }
    }

    private fun getSectionWithIdKeyPair(formMetaData: JsonNode): Map<String, List<Pair<String, String>>> {

        val typeToMask = listOf(ID_TYPE, NRIC, EDH_SHAREHOLDERLIST, EDH_APPOINTMENTS, SUBADDABLE)
        val sectionWithIdKeyPair = mutableMapOf<String, List<Pair<String, String>>>()

        val applicationDetailSections = formMetaData.get("schema").get("applicationDetail")

        applicationDetailSections.forEach { section ->
            val sectionKey = section.get("key").asText()
            val fields = section.get("fields")
            val listOfFields = fields
                .filter { it.get("type").asText() in typeToMask }
                .map {
                    val fieldType = it.get("type").asText()
                    if (fieldType == SUBADDABLE) {
                        // subaddable to use id for comparison instead
                        val subaddableId = it.get("attr").get("subAddableSections").get("id").asText()
                        val subaddableNode =
                            applicationDetailSections.single { node -> node.get("id").asText() == subaddableId }
                        Pair(fieldType, subaddableNode.get("key").asText())
                    } else {
                        Pair(fieldType, it.get("key").asText())
                    }
                }

            if (listOfFields.isNotEmpty()) {
                sectionWithIdKeyPair[sectionKey] = listOfFields
            }
        }

        return sectionWithIdKeyPair
    }

    private fun iterateIdTypeKeyPairList(
        node: JsonNode,
        idTypeKeyPairList: List<Pair<String, String>>,
        maskingFunc: String.() -> String,
        sectionWithIdKeyPair: Map<String, List<Pair<String, String>>> = mapOf(),
    ) {
        idTypeKeyPairList.forEach {
            if (it.first == SUBADDABLE) {
                val subaddableIdTypeKeyPair = sectionWithIdKeyPair[it.second] ?: listOf()
                iterateSubaddableNode(node.get(it.second), subaddableIdTypeKeyPair, maskingFunc)
            } else {
                editNodeValue(node, it, maskingFunc)
            }
        }
    }

    private fun iterateSubaddableNode(
        subaddableNode: JsonNode,
        idKeyPair: List<Pair<String, String>>,
        maskingFunc: String.() -> String
    ) {
        subaddableNode.forEach { subaddableRowNode ->
            // get the subaddable key pair section using subaddable key
            idKeyPair.forEach { subaddableKeyPair ->
                editNodeValue(subaddableRowNode, subaddableKeyPair, maskingFunc)
            }
        }
    }

    private fun editNodeValue(
        node: JsonNode,
        idTypeKeyPair: Pair<String, String>,
        maskingFunc: String.() -> String
    ) {
        when (idTypeKeyPair.first) {
            ID_TYPE, EDH_SHAREHOLDERLIST, EDH_APPOINTMENTS -> {
                node.get(idTypeKeyPair.second).let {
                    val value = it.get("idNumber").textValue().maskingFunc()
                    (it as ObjectNode).put("idNumber", value)
                }
            }
            NRIC -> {
                val value = node.get(idTypeKeyPair.second).textValue().maskingFunc()
                (node as ObjectNode).put(idTypeKeyPair.second, value)
            }
            else -> {
                // shouldn't reach here, but log just in case
                logger.error("field ${idTypeKeyPair.second} type not in type to mask: ${idTypeKeyPair.first}")
            }
        }
    }
}
