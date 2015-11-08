package zircle.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import zircle.api.QAppConfig
import zircle.api.QRepositoryConfig

@RestController
class RepositoryController {

    @Autowired
    var config: QAppConfig? = null

    fun createRespositoryConfig() : QRepositoryConfig {
        return QRepositoryConfig(user = config!!.user, password = config!!.password, atomPubUrl = config!!.atomPubUrl)
    }

    @RequestMapping("/tconfig")
    fun testConfig():  QRepositoryConfig {
        return this.createRespositoryConfig()
    }
}