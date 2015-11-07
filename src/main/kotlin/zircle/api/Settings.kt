package zircle.api

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl
import org.apache.chemistry.opencmis.commons.PropertyIds
import org.apache.chemistry.opencmis.commons.SessionParameter
import org.apache.chemistry.opencmis.commons.enums.BindingType

object Constants {
    val CM_TITLE = "cm:title"
    val CM_DESCRIPTION = "cm:description"
    var CM_FOLDER_WITH_TITLE = "cm:folder, P:cm:titled"
}

data class AppConfig(
        val alfrescoUrl: String,
        val user: String,
        val password: String)

data class RepositoryConfig(
        val atomPubUrl: String,
        val user: String,
        val password: String)

data class FolderInfo(
    var name: String,
    var title: String,
    var description: String)

class RepositoryApi(config: RepositoryConfig) {

    private var session: Session

    init {
            var factory = SessionFactoryImpl.newInstance();
            var properties = hashMapOf(
                    SessionParameter.USER to config.user,
                    SessionParameter.PASSWORD to config.password,
                    SessionParameter.ATOMPUB_URL to config.atomPubUrl,
                    SessionParameter.BINDING_TYPE to BindingType.ATOMPUB.value(),
                    SessionParameter.OBJECT_FACTORY_CLASS to "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl" )
            var repository = factory.getRepositories(properties).first()
            this.session = repository.createSession()
    }

    fun getFilder(path: String): Folder {
       var folder = session.getObjectByPath(path)
        return folder as Folder
    }

    fun createFolder(path: String, info: FolderInfo): Folder {
        var baseFolder = path.substring(0, path.indexOf('/') + 1)
        var folderName = path.substring(path.lastIndexOf("/") + 1)
        var targetBaseFolder = session.getObjectByPath(baseFolder) as Folder
        var properties = hashMapOf(
                PropertyIds.OBJECT_TYPE_ID to Constants.CM_FOLDER_WITH_TITLE,
                PropertyIds.NAME to folderName,
                Constants.CM_TITLE to info.title,
                Constants.CM_DESCRIPTION to info.description)
        var folder = targetBaseFolder.createFolder(properties)
        return folder
    }
}

