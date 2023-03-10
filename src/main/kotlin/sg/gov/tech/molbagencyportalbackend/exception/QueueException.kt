package sg.gov.tech.molbagencyportalbackend.exception

class QueueException(override val message: String, override val cause: Throwable? = null) :
    RuntimeException(message, cause)
