package com.bytedance.tools.codelocator.adapter

import java.util.Properties

object BuildInfo {
    val version: String by lazy {
        val manifestVersion = BuildInfo::class.java.`package`?.implementationVersion?.trim()
        if (!manifestVersion.isNullOrEmpty()) {
            return@lazy manifestVersion
        }

        val props = Properties()
        BuildInfo::class.java.getResourceAsStream("/codelocator-adapter.properties")?.use { input ->
            props.load(input)
        }
        props.getProperty("version")?.trim()?.takeIf { it.isNotEmpty() } ?: "dev"
    }
}
