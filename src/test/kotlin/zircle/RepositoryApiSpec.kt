package zircle

import zircle.api.QRepositoryApi
import zircle.api.QRepositoryConfig
import org.junit.Test
import zircle.api.QAspect
import zircle.api.QProperty

object TestConfig {
    var atomPubUrl = "http://127.0.0.1:8081/alfresco/api/-default-/public/cmis/versions/1.0/atom"
    var user = "admin"
    var password = "admin"
}

class RepositoryApiSpec {

    fun getApi(): QRepositoryApi {
        var config = QRepositoryConfig(
                user = TestConfig.user,
                password = TestConfig.password,
                atomPubUrl = TestConfig.atomPubUrl)
        var api = QRepositoryApi(config)
        return api
    }

    @Test fun shoudGetFolderByPath() {
        var path = "/"
        var api = this.getApi()
        var folder = api.getFilder(path)
    }

    @Test fun shouldRemoveAspectFromFolder() {
        var path = "/wk"
        var api = this.getApi()
        var uuid = api.removeAspectFromFolder(path, "P:cm:titled")
    }

    @Test fun shouldAddAspectToFolder() {
        var path = "/wk"
        var api = this.getApi()
        var aspect = QAspect("P:cm:titled", arrayOf(
                QProperty("cm:title", "Hello, world!"),
                QProperty("cm:description", "What's your name?")
        ))
        api.addAspectToFolder(path, aspect)
    }
}
