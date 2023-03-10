package sg.gov.tech.molbagencyportalbackend.configuration

import org.hibernate.envers.AuditReader
import org.hibernate.envers.AuditReaderFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.model.User
import sg.gov.tech.molbagencyportalbackend.service.UserService
import java.util.Optional
import javax.persistence.EntityManagerFactory

@Configuration
@EnableJpaAuditing
@ExcludeFromGeneratedCoverageTest
class AuditConfiguration(
    private val entityManagerFactory: EntityManagerFactory,
    private val userService: UserService
) {
    @Bean
    fun auditorProvider(): AuditorAware<String> = AuditorAwareImpl(userService)

    @Bean
    fun auditReader(): AuditReader = AuditReaderFactory.get(entityManagerFactory.createEntityManager())
}

@ExcludeFromGeneratedCoverageTest
class AuditorAwareImpl(private val userService: UserService) : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val userPrincipal = when (authentication.principal) {
            is OAuth2AuthenticatedPrincipal ->
                (authentication.principal as OAuth2AuthenticatedPrincipal).name // Bearer Token
            else -> "SYSTEM"
        }
        if (!userService.existsByEmailAndIsDeletedFalse(userPrincipal)) {
            return Optional.of(userService.getUserByEmailAndIsDeletedFalse("system_user@system.com")?.id!!.toString())
        }
        val user: User? = userService.getUserByEmailAndIsDeletedFalse(userPrincipal)
        return Optional.ofNullable(user?.id.toString())
    }
}
