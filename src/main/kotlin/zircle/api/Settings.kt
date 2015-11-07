package zircle.api

import org.alfresco.cmis.client.AlfrescoDocument
import org.alfresco.cmis.client.AlfrescoFolder
import org.alfresco.cmis.client.impl.AlfrescoDocumentImpl
import org.alfresco.cmis.client.impl.AlfrescoFolderImpl
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl
import org.apache.chemistry.opencmis.commons.PropertyIds
import org.apache.chemistry.opencmis.commons.SessionParameter
import org.apache.chemistry.opencmis.commons.enums.BindingType
import org.apache.chemistry.opencmis.commons.enums.VersioningState
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl
import java.io.ByteArrayInputStream
import java.math.BigInteger
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

data class QFolder(
    val name: String,
    val title: String,
    val description: String)

data class QDocument(
        val name: String,
        val title: String,
        val description: String,
        val contentType: String,
        val aspects: Array<QAspect>
)

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
        var document = session.getObjectByPath(path) as AlfrescoFolder
        document.removeAspect(aspect)
        var uuid = document.getProperty<String>(QConstants.CMIS_OBJECT_ID) as PropertyImpl
        return uuid.getValue()
    }

    fun addAspectToFolder(path: String, aspect: QAspect) : Unit {
        var document = session.getObjectByPath(path) as AlfrescoFolder
        var map = HashMap<String, Any>()
        aspect.properties.forEach { map.put(it.name, it.value) }
        document.addAspect(aspect.name, map)
    }

    fun getFolder(path: String): AlfrescoFolderImpl {
       var folder = session.getObjectByPath(path)
        return folder as AlfrescoFolderImpl
    }

    fun createFolder(path: String, info: QFolder): AlfrescoFolder {
        var baseFolder = path.substring(0, path.indexOf('/') + 1)
        var folderName = path.substring(path.lastIndexOf("/") + 1)
        var targetBaseFolder = session.getObjectByPath(baseFolder) as Folder
        var properties = hashMapOf(
                PropertyIds.OBJECT_TYPE_ID to QConstants.CM_FOLDER_WITH_TITLE,
                PropertyIds.NAME to folderName,
                QConstants.CM_TITLE to info.title,
                QConstants.CM_DESCRIPTION to info.description)
        var folder = targetBaseFolder.createFolder(properties) as AlfrescoFolder
        return folder
    }

    fun createDocument(path: String, document: QDocument, content: Array<Byte>): AlfrescoDocument {
        var property = hashMapOf(
                PropertyIds.NAME to document.name,
                PropertyIds.OBJECT_TYPE_ID to "cmis:document,P:cm:titled",
                PropertyIds.CONTENT_STREAM_MIME_TYPE to document.contentType,
                QConstants.CM_DESCRIPTION to document.description,
                QConstants.CM_TITLE to document.title
        )
        var folder = this.getFolder(path)
        var length = BigInteger(content.size.toString())
        var stream = ByteArrayInputStream(content.toByteArray())
        var contentStream = ContentStreamImpl(document.name, length, document.contentType, stream)
        var ecmDocument = folder.createDocument(property, contentStream, VersioningState.MAJOR) as AlfrescoDocument
        return ecmDocument
    }
}

