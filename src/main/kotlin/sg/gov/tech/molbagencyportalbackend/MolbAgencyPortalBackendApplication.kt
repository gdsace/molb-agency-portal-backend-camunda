package sg.gov.tech.molbagencyportalbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@ComponentScan(basePackages = ["sg.gov.tech"])
@EnableJpaRepositories(basePackages = ["sg.gov.tech"])
@EntityScan(basePackages = ["sg.gov.tech"])
@ConfigurationPropertiesScan
@EnableFeignClients
class MolbAgencyPortalBackendApplication

@SuppressWarnings("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<MolbAgencyPortalBackendApplication>(*args)
}
