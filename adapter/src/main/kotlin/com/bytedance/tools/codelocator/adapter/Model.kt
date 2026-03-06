package com.bytedance.tools.codelocator.adapter

data class GrabMeta(
    val grabId: String,
    val source: String,
    val deviceSerial: String? = null,
    val packageName: String? = null,
    val activity: String? = null,
    val grabTime: Long,
    val deviceNotice: String? = null
)

data class ViewNodeDto(
    val memAddr: String,
    val className: String,
    val idStr: String? = null,
    val text: String? = null,
    val left: Int = 0,
    val top: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
    val visible: Boolean = true,
    val alpha: Double = 1.0,
    val composeNodes: List<ComposeNodeDto> = emptyList(),
    val children: List<ViewNodeDto> = emptyList(),
    val raw: Map<String, Any?> = emptyMap()
)

data class GrabSnapshot(
    val meta: GrabMeta,
    val uiTree: List<ViewNodeDto>,
    val screenshotRef: String? = null,
    val indexes: Map<String, ViewIndexItem> = emptyMap(),
    val composeIndexes: Map<String, ComposeIndexItem> = emptyMap()
)

data class ViewIndexItem(
    val memAddr: String,
    val className: String,
    val idStr: String? = null,
    val text: String? = null,
    val left: Int = 0,
    val top: Int = 0,
    val width: Int = 0,
    val height: Int = 0
)

data class ComposeNodeDto(
    val nodeId: String,
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
    val text: String? = null,
    val contentDescription: String? = null,
    val testTag: String? = null,
    val clickable: Boolean = false,
    val enabled: Boolean = true,
    val focused: Boolean = false,
    val visibleToUser: Boolean = true,
    val selected: Boolean = false,
    val checkable: Boolean = false,
    val checked: Boolean = false,
    val focusable: Boolean = false,
    val actions: List<String> = emptyList(),
    val children: List<ComposeNodeDto> = emptyList(),
    val raw: Map<String, Any?> = emptyMap()
)

data class ComposeIndexItem(
    val composeKey: String,
    val hostMemAddr: String,
    val nodeId: String,
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
    val text: String? = null,
    val contentDescription: String? = null,
    val testTag: String? = null,
    val clickable: Boolean = false,
    val enabled: Boolean = true,
    val focused: Boolean = false,
    val visibleToUser: Boolean = true,
    val selected: Boolean = false,
    val checkable: Boolean = false,
    val checked: Boolean = false,
    val focusable: Boolean = false,
    val actions: List<String> = emptyList()
)

data class McpError(
    val code: String,
    val message: String,
    val details: Map<String, Any?> = emptyMap()
)

data class ToolResult<T>(
    val success: Boolean,
    val data: T? = null,
    val error: McpError? = null,
    val grabId: String? = null
)

data class ViewerOpenResult(
    val url: String,
    val port: Int,
    val pid: Long?,
    val grabId: String?
)

data class ParsedCodeLocatorFile(
    val appJson: String,
    val imageBytes: ByteArray,
    val version: String
)

data class BroadcastDecodedResult(
    val encoded: String,
    val json: String,
    val rawOutput: String
)
