package io.eqoty.server.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.buffered
import kotlinx.io.files.FileMetadata
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator


// source:
// https://slack-chats.kotlinlang.org/t/2527462/how-do-i-setup-static-routing-with-the-native-version-of-kto
// https://gist.github.com/Stexxe/4867bbd9b44339f9f9adc39e166894ca
fun Route.files(folder: String) {
    val dir = staticRootFolder?.let { Path(it, folder) } ?: return
    val pathParameter = "static-content-path-parameter"
    get("{$pathParameter...}") {
        val relativePath =
            call.parameters.getAll(pathParameter)?.joinToString(SystemPathSeparator.toString()) ?: return@get
        val file = Path(dir, relativePath)
        call.respondStatic(file)
    }
}

fun Route.default(localPath: String) {
    val path = staticRootFolder?.let { Path(it, localPath) } ?: return
    get {
        call.respondStatic(path)
    }
}

private val staticRootFolderKey = AttributeKey<Path>("BaseFolder")

var Route.staticRootFolder: Path?
    get() = attributes.getOrNull(staticRootFolderKey) ?: parent?.staticRootFolder
    set(value) {
        if (value != null) {
            attributes.put(staticRootFolderKey, value)
        } else {
            attributes.remove(staticRootFolderKey)
        }
    }

fun Route.file(remotePath: String, localPath: String) {
    val path = staticRootFolder?.let { Path(it, localPath) } ?: return

    get(remotePath) {
        call.respondStatic(path)
    }
}

suspend inline fun ApplicationCall.respondStatic(path: Path) {
    if (SystemFileSystem.exists(path)) {
        respond(LocalFileContent(path, ContentType.defaultForFile(path)))
    }
}

fun ContentType.Companion.defaultForFile(path: Path): ContentType =
    ContentType.fromFileExtension(path.name.substringAfter('.', path.name)).selectDefault()

fun List<ContentType>.selectDefault(): ContentType {
    val contentType = firstOrNull() ?: ContentType.Application.OctetStream
    return when {
        contentType.contentType == "text" && contentType.charset() == null -> contentType.withCharset(Charsets.UTF_8)
        else -> contentType
    }
}

class LocalFileContent(
    private val path: Path, override val contentType: ContentType = ContentType.defaultForFile(path)
) : OutgoingContent.WriteChannelContent() {

    override val contentLength: Long get() = stat()?.size ?: -1
    override suspend fun writeTo(channel: ByteWriteChannel) {
        val source = SystemFileSystem.source(path)
        source.use { fileSource ->
            fileSource.buffered().use { bufferedFileSource ->
                val buf = ByteArray(4 * 1024)
                while (true) {
                    val read = bufferedFileSource.readAtMostTo(buf)
                    if (read <= 0) break
                    channel.writeFully(buf, 0, read)
                }
            }
        }
    }

    init {
        if (!SystemFileSystem.exists(path)) {
            throw IllegalStateException("No such file $path")
        }

//        stat().lastModifiedAtMillis?.let {
//            versions += LastModifiedVersion(GMTDate(it))
//        }
    }

    private fun stat(): FileMetadata? {
        return SystemFileSystem.metadataOrNull(path)
    }
}