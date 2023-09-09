package io.eqoty.server.plugins

import okio.FileSystem

actual val fileSystem: FileSystem
    get() = FileSystem.SYSTEM