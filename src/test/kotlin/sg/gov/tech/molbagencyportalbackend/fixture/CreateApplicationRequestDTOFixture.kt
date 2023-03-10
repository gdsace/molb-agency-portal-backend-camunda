package sg.gov.tech.molbagencyportalbackend.fixture

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import sg.gov.tech.molbagencyportalbackend.dto.l1t.AddressDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.ApplicantDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.ApplicationDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.CompanyDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.CreateApplicationRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.FilerDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.GeneralDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.IdDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.PaymentDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.ProfileDTO
import sg.gov.tech.molbagencyportalbackend.dto.l1t.VersionDTO

class GeneralDTOFixture private constructor() {
    companion object {
        fun makeGeneralDTO(applicationNumber: String = "A1234567") = GeneralDTO(
            applicationNumber = applicationNumber,
            dateSent = "27/05/2022 18:19:25",
            licenceName = "Test Licence",
            licenceID = "1",
            transactionType = "new"
        )
    }
}

class ApplicationDTOFixture private constructor() {
    companion object {
        fun makeApplicationDTO(
            generalDTO: GeneralDTO,
            company: CompanyDTO,
            filer: FilerDTO,
            profileDTO: ProfileDTO,
            applicant: ApplicantDTO
        ) =
            ApplicationDTO(
                general = generalDTO,
                profile = profileDTO,
                applicant = applicant,
                company = company,
                filer = filer,
                licence = ObjectMapper().readValue(ApplicationFixture.licenceFile, JsonNode::class.java)
            )
    }
}

object CreateApplicationRequestDTOFixture {
    val profileAppliantDTO = ProfileDTO(applyAs = "As an applicant")
    val profileBehalfDTO = ProfileDTO(applyAs = "On behalf of applicant")

    val applicant = ApplicantDTO(
        salutation = "Mr",
        name = "John",
        id = IdDTO(
            idType = "NRIC",
            idNumber = "SX  XXXXXXA"
        ),
        email = "john@gmail.com",
        contactNumber = "+6563333333",
        address = AddressDTO(
            postalCode = "117438",
            blockNo = "10",
            streetName = "Pasir Panjang Road",
            buildingName = "Mapletree Business City",
            floor = "10",
            unit = ""
        )
    )

    val filer = FilerDTO(
        salutation = "Mr",
        name = "Peter",
        id = IdDTO(
            idType = "NRIC",
            idNumber = "S1234567A"
        ),
        email = "peter@gmail.com",
        contactNumber = "+6512345678"
    )
    val applicant_add_null = ApplicantDTO(
        salutation = "Mr",
        name = "John",
        id = IdDTO(
            idType = "NRIC",
            idNumber = "SX  XXXXXXA"
        ),
        email = "john@gmail.com",
        contactNumber = "+6563333333",
        address = null
    )

    val filer_null = FilerDTO(
        salutation = null,
        name = null,
        id = IdDTO(
            idType = "",
            idNumber = ""
        ),
        email = null,
        contactNumber = null
    )

    val company_null = CompanyDTO(
        companyName = null,
        uen = null,
        entityType = null,
        registeredAddress = null
    )

    val company = CompanyDTO(
        companyName = "Test Company",
        uen = "1234567",
        entityType = "Business",
        registeredAddress = AddressDTO(
            postalCode = "117438",
            blockNo = "10",
            streetName = "Pasir Panjang Road",
            buildingName = "Mapletree Business City",
            floor = "10",
            unit = ""
        )
    )

    val applicationDTO_ApplicationNumberBlank = ApplicationDTOFixture.makeApplicationDTO(
        GeneralDTOFixture.makeGeneralDTO(""),
        company_null,
        filer_null,
        profileAppliantDTO,
        applicant
    )

    val applicationDTO_SingpassBehalfFilerNull = ApplicationDTOFixture.makeApplicationDTO(
        GeneralDTOFixture.makeGeneralDTO(),
        company_null,
        filer_null,
        profileBehalfDTO,
        applicant
    )

    val applicationDTO_SingpassSelf = ApplicationDTOFixture.makeApplicationDTO(
        GeneralDTOFixture.makeGeneralDTO(),
        company_null,
        filer_null,
        profileAppliantDTO,
        applicant
    )

    val applicationDTO_SingpassSelfAddressNull = ApplicationDTOFixture.makeApplicationDTO(
        GeneralDTOFixture.makeGeneralDTO(),
        company_null,
        filer_null,
        profileAppliantDTO,
        applicant_add_null
    )

    val applicationDTO_CorppassSelf = ApplicationDTOFixture.makeApplicationDTO(
        GeneralDTOFixture.makeGeneralDTO(),
        company,
        filer_null,
        profileAppliantDTO,
        applicant
    )

    val paymentDTO = PaymentDTO(
        paymentMode = "Credit Card",
        paymentDate = "17/02/2020 18 =19 =25",
        paymentAmount = "168.80",
        transactionNumber = "ABCD123456",
        receiptNumber = "ABCD123456"
    )

    val versionDTO = VersionDTO(
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

    val createApplicationRequest = CreateApplicationRequestDTO(
        operation = "createApplication",
        application = applicationDTO_SingpassSelf,
        payment = paymentDTO,
        version = versionDTO
    )

    val createApplicationRequest_BlankApplicationNumber = CreateApplicationRequestDTO(
        operation = "createApplication",
        application = applicationDTO_ApplicationNumberBlank,
        payment = paymentDTO,
        version = versionDTO
    )

    val createApplicationRequest_NullAddress = CreateApplicationRequestDTO(
        operation = "createApplication",
        application = applicationDTO_SingpassSelfAddressNull,
        payment = paymentDTO,
        version = versionDTO
    )

    val createApplicationRequest_BlankFilerOnbehalf = CreateApplicationRequestDTO(
        operation = "createApplication",
        application = applicationDTO_SingpassBehalfFilerNull,
        payment = paymentDTO,
        version = versionDTO
    )
}
