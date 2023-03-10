package sg.gov.tech.molbagencyportalbackend.exception

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureException
import sg.gov.tech.common.exception.MolbException
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest

@ExcludeFromGeneratedCoverageTest
class AuthException : MolbException {
    constructor(message: String) : super(
        message = message
    )

    constructor(ex: JwtException) : this(
        message = when (ex) {
            is SignatureException -> "JWT Signature is invalid"
            is MalformedJwtException -> "JWT Token is invalid"
            is ExpiredJwtException -> "JWT Token is expired"
            is NotWhitelistedException -> "JWT Token is not found in whitelist"
            else -> "Unknown error"
        }
    )
}

@ExcludeFromGeneratedCoverageTest
class NotWhitelistedException(errorMessage: String) : JwtException(errorMessage)
