package zircle

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import zircle.api.*
import java.io.File
import kotlin.test.assertTrue

object TestConfig {
    var atomPubUrl = "http://127.0.0.1:8081/alfresco/api/-default-/public/cmis/versions/1.1/atom"
    var user = "admin"
    var password = "admin"
}

class RepositoryApiSpec {

    @Autowired var appConfig: QAppConfig? = null;

    fun getApi(): QRepositoryApi {
        var config = QRepositoryConfig(
                user = TestConfig.user,
                password = TestConfig.password,
                atomPubUrl = TestConfig.atomPubUrl)
        var api = QRepositoryApi(config)
        return api
    }

    fun getResourceFile(name: String): File {
        var path = this.javaClass.classLoader.getResource(name).path
        return File(path)
    }

    @Test fun showInjectConfigurationProperty() {
        assertTrue { appConfig != null }
    }

    @Test fun shoudGetFolderByPath() {
        var path = "/"
        var api = this.getApi()
        var folder = api.getFolder(path)
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

    @Test fun shouldAddCustomAspect() {
        var path = "/wk"
        var api = this.getApi()
        var aspect = QAspect("P:jw:info", arrayOf(
                        QProperty("jw:firstName", "Jannine"),
                        QProperty("jw:lastName", "Weigel") ))
        api.addAspectToFolder(path, aspect)
    }

    @Test fun shouldCreateNewDocument() {
        var path = "/wk"
        var document = QDocument(
                name = "1234.txt",
                title = "Hello World 2015 - Title",
                description = "Hello World 2015 - Description",
                aspects = arrayOf(QAspect("P:jw:info", arrayOf(
                        QProperty("jw:firstName", "Jannine"),
                        QProperty("jw:lastName", "Weigel")
                ))),
                contentType = "text/plain")

        var file = this.getResourceFile("Hello.txt")
        var content = file.readBytes()
        var api = this.getApi()
        api.createDocument(path, document, content.toTypedArray())
    }
}
