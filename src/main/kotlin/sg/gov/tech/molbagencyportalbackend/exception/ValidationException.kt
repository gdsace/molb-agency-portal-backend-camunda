package sg.gov.tech.molbagencyportalbackend.exception

import sg.gov.tech.common.exception.MolbException
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest

@ExcludeFromGeneratedCoverageTest
class ValidationException(val responseMessage: String, logMessage: String) : MolbException(message = logMessage)
