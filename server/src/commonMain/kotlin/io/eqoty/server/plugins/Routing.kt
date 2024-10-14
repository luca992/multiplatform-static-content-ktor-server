package io.eqoty.server.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

fun Application.configureRouting() {
    routing {
        route("/") {
            val rootFolder = Path("src/commonMain/resources/static")
            staticRootFolder = rootFolder
            SystemFileSystem.list(rootFolder).filter { path ->
                SystemFileSystem.metadataOrNull(path)?.isRegularFile == true
            }.forEach { path ->
                val relativePath = path.toString().removePrefix(rootFolder.toString())
                file(relativePath, relativePath)
            }
            default("index.html")

            route("assets") {
                files("css")
                files("js")
            }
        }
    }
}
