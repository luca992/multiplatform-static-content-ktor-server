package io.eqoty.server.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import okio.Path.Companion.toPath

fun Application.configureRouting() {
    routing {
        route("/") {
            val rootFolder = "src/commonMain/resources/static".toPath()
            staticRootFolder = rootFolder
            fileSystem.listRecursively(rootFolder).filter { path ->
                fileSystem.metadata(path).isRegularFile
            }.forEach { path ->
                val relativePath = path.relativeTo(rootFolder).toString()
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
