package sg.gov.tech.molbagencyportalbackend.exception

import sg.gov.tech.common.exception.MolbException
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest

@ExcludeFromGeneratedCoverageTest
class NotFoundException(message: String) : MolbException(message = message)
