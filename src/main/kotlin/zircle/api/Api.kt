package zircle.api

import org.alfresco.cmis.client.AlfrescoDocument
import org.alfresco.cmis.client.AlfrescoFolder
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session
import org.apache.chemistry.opencmis.client.runtime.PropertyImpl
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl
import org.apache.chemistry.opencmis.commons.PropertyIds
import org.apache.chemistry.opencmis.commons.SessionParameter
import org.apache.chemistry.opencmis.commons.enums.BindingType
import org.apache.chemistry.opencmis.commons.enums.VersioningState
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl
import org.springframework.boot.context.properties.ConfigurationProperties
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.util.*

object QConstants {
    val CmTitle = "cm:title"
    val CmDescription = "cm:description"
    val CmisFolderWithTitle = "cmis:folder, P:cm:titled"
    var CmisDocumentWithTitle = "cmis:document, P:cm:titled"
    val CmisObjectId = "cmis:objectId"
}



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
        val properties: Array<QProperty>)

class QRepositoryApi(config: QRepositoryConfig) {

    private var session: Session

    init {
        var factory = SessionFactoryImpl.newInstance();
        var properties = hashMapOf(
                SessionParameter.USER to config.user,
                SessionParameter.PASSWORD to config.password,
                SessionParameter.ATOMPUB_URL to config.atomPubUrl,
                SessionParameter.BINDING_TYPE to BindingType.ATOMPUB.value(),
                SessionParameter.OBJECT_FACTORY_CLASS to "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl")
        var repository = factory.getRepositories(properties).first()
        this.session = repository.createSession()
    }

    fun getAllAspects() {

    }

    fun removeAspectFromFolder(path: String, aspect: String): String {
        var document = session.getObjectByPath(path) as AlfrescoFolder
        document.removeAspect(aspect)
        var uuid = document.getProperty<String>(QConstants.CmisObjectId) as PropertyImpl
        return uuid.getValue()
    }

    fun addAspectToFolder(path: String, aspect: QAspect): Unit {
        var document = session.getObjectByPath(path) as AlfrescoFolder
        var map = HashMap<String, Any>()
        aspect.properties.forEach { map.put(it.name, it.value) }
        document.addAspect(aspect.name, map)
    }

    fun getFolder(path: String): AlfrescoFolder {
        var folder = session.getObjectByPath(path)
        return folder as AlfrescoFolder
    }

    fun createFolder(path: String, info: QFolder): AlfrescoFolder {
        var baseFolder = path.substring(0, path.indexOf('/') + 1)
        var folderName = path.substring(path.lastIndexOf("/") + 1)
        var targetBaseFolder = session.getObjectByPath(baseFolder) as Folder
        var properties = hashMapOf(
                PropertyIds.OBJECT_TYPE_ID to QConstants.CmisFolderWithTitle,
                PropertyIds.NAME to folderName,
                QConstants.CmTitle to info.title,
                QConstants.CmDescription to info.description)
        var folder = targetBaseFolder.createFolder(properties) as AlfrescoFolder
        return folder
    }

    fun createDocument(path: String, document: QDocument, content: Array<Byte>): AlfrescoDocument {
        var aspects = document.aspects.map { it.name }.joinToString (separator = ", ")
        var property = hashMapOf(
                PropertyIds.NAME to document.name,
                PropertyIds.OBJECT_TYPE_ID to QConstants.CmisDocumentWithTitle + ", " + aspects,
                PropertyIds.CONTENT_STREAM_MIME_TYPE to document.contentType,
                QConstants.CmDescription to document.description,
                QConstants.CmTitle to document.title
        )
        document.aspects.forEach {
            it.properties.forEach {
                property.put(it.name, "$it.value")
            }
        }
        var folder = this.getFolder(path)
        var length = BigInteger(content.size.toString())
        var stream = ByteArrayInputStream(content.toByteArray())
        var contentStream = ContentStreamImpl(document.name, length, document.contentType, stream)
        var ecmDocument = folder.createDocument(property, contentStream, VersioningState.MAJOR) as AlfrescoDocument
        return ecmDocument
    }
}

