package zircle.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties( locations = arrayOf("classpath:application.properties"), ignoreUnknownFields = false, prefix = "app")
@Configuration
@EnableConfigurationProperties
open class QAppConfig() {
    var alfrescoUrl: String = ""
    var atomPubUrl: String = ""
    var user: String = ""
    var password: String = ""
}

data class QRepositoryConfig(
        val atomPubUrl: String,
        val user: String,
        val password: String)
