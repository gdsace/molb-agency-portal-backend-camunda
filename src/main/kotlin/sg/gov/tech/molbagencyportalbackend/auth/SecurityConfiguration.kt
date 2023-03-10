package sg.gov.tech.molbagencyportalbackend.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import sg.gov.tech.molbagencyportalbackend.annotation.ExcludeFromGeneratedCoverageTest
import sg.gov.tech.molbagencyportalbackend.configuration.SignatureVerificationConfiguration
import sg.gov.tech.molbagencyportalbackend.integration.job.JobConfig

@Configurable
@EnableWebSecurity
@ExcludeFromGeneratedCoverageTest
class SecurityConfiguration {

    @Autowired
    private lateinit var signatureVerificationConfiguration: SignatureVerificationConfiguration

    @Autowired
    private lateinit var introspector: JwtOpaqueTokenIntrospector

    @Value("\${spring.security.job.username}")
    private var jobUsername: String = ""

    @Value("\${spring.security.job.password}")
    private var jobPassword: String = "AUDITOR_PLEASE_NOTE_THIS_IS_NOT_THE_REAL_PASSWORD"

    @Bean
    @Order(1)
    @Profile(value = ["local", "dev", "qa", "staging", "production"])
    fun apiFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.cors()
            .and()
            .csrf().disable()
            .antMatcher("/api/**")
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer()
            .opaqueToken()
            .introspector(introspector)
        return http.build()
    }

    @Bean
    fun resourceFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .cors()
        .and()
        .csrf().disable()
        .antMatcher("/resource/**")
        .addFilterBefore(
            AuthHeaderFilter(signatureVerificationConfiguration),
            UsernamePasswordAuthenticationFilter::class.java
        ).build()

    @Bean
    fun userDetailsService(): InMemoryUserDetailsManager {
        val user: UserDetails = User
            .withUsername(jobUsername)
            .password(passwordEncoder().encode(jobPassword))
            .roles("JOB")
            .build()
        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun jobfilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("${JobConfig.PATH_PREFIX}/**")
            .authenticated()
            .and()
            .httpBasic()
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
