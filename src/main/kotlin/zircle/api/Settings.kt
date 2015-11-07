package zircle.api

import org.alfresco.cmis.client.impl.AlfrescoFolderImpl
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl
import org.apache.chemistry.opencmis.commons.PropertyIds
import org.apache.chemistry.opencmis.commons.SessionParameter
import org.apache.chemistry.opencmis.commons.enums.BindingType
import java.util.*

object QConstants {
    val CM_TITLE = "cm:title"
    val CM_DESCRIPTION = "cm:description"
    val CM_FOLDER_WITH_TITLE = "cm:folder, P:cm:titled"
    val CMIS_OBJECT_ID = "cmis:objectId"
}

data class QAppConfig(
        val alfrescoUrl: String,
        val user: String,
        val password: String)

data class QRepositoryConfig(
        val atomPubUrl: String,
        val user: String,
        val password: String)

data class QFolderInfo(
    var name: String,
    var title: String,
    var description: String)

data class QProperty(
        val name: String,
        val value: Any)

data class QAspect(
        val name: String,
        val properties:Array<QProperty>)

class QRepositoryApi(config: QRepositoryConfig) {

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

    fun removeAspectFromFolder(path: String, aspect: String): String {
        var document = session.getObjectByPath(path) as AlfrescoFolderImpl
        document.removeAspect(aspect)
        var uuid = document.getProperty<String>(QConstants.CMIS_OBJECT_ID) as PropertyImpl
        return uuid.getValue()
    }

    fun addAspectToFolder(path: String, aspect: QAspect) : Unit {
        var document = session.getObjectByPath(path) as AlfrescoFolderImpl
        var map = HashMap<String, Any>()
        aspect.properties.forEach { map.put(it.name, it.value) }
        document.addAspect(aspect.name, map)
    }

    fun getFilder(path: String): Folder {
       var folder = session.getObjectByPath(path)
        return folder as Folder
    }

    fun createFolder(path: String, info: QFolderInfo): Folder {
        var baseFolder = path.substring(0, path.indexOf('/') + 1)
        var folderName = path.substring(path.lastIndexOf("/") + 1)
        var targetBaseFolder = session.getObjectByPath(baseFolder) as Folder
        var properties = hashMapOf(
                PropertyIds.OBJECT_TYPE_ID to QConstants.CM_FOLDER_WITH_TITLE,
                PropertyIds.NAME to folderName,
                QConstants.CM_TITLE to info.title,
                QConstants.CM_DESCRIPTION to info.description)
        var folder = targetBaseFolder.createFolder(properties)
        return folder
    }
}

