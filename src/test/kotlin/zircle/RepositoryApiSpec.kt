package zircle

import zircle.api.RepositoryApi
import zircle.api.RepositoryConfig
import org.junit.Test

object TestConfig {
    var atomPubUrl = "http://127.0.0.1:8081/alfresco/api/-default-/public/cmis/versions/1.0/atom"
    var user = "admin"
    var password = "admin"
}

class RepositoryApiSpec {

    @Test fun shoudGetFolderByPath() {
        var path = "/"
        var config = RepositoryConfig(
                user = TestConfig.user,
                password = TestConfig.password,
                atomPubUrl = TestConfig.atomPubUrl)
        var api = RepositoryApi(config)
        var folder = api.getFilder(path)

    }
}
