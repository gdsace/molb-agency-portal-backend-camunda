package sg.gov.tech.molbagencyportalbackend.configuration

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import sg.gov.tech.integration.properties.AwsProperties
import sg.gov.tech.molbagencyportalbackend.util.aws.AwsSqsClient

@Configuration
class AwsConfiguration {

    @Bean
    @ConfigurationProperties("aws")
    fun awsProperties(): AwsProperties {
        return AwsProperties()
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "aws",
        name = ["accessKey", "secretKey"],
        havingValue = "",
        matchIfMissing = false
    )
    fun awsSqsClient(awsProperties: AwsProperties): AwsSqsClient {
        return AwsSqsClient(awsProperties)
    }
}
