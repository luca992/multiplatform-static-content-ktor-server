package io.eqoty.server.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import okio.Path.Companion.toPath

fun Application.configureRouting() {
    routing {
        route("/") {
            staticRootFolder = "src/commonMain/resources/static".toPath()
            file("index.html", "index.html")
            default("index.html")

            route("assets") {
                files("css")
                files("js")
            }
        }
    }
}
