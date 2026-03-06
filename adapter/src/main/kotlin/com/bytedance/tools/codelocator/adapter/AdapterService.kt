package com.bytedance.tools.codelocator.adapter

class AdapterService(
    private val store: GrabStore,
    private val adb: AdbGateway,
    private val viewerManager: ViewerManager
) {

    fun grabLive(deviceSerial: String?): ToolResult<GrabMeta> {
        return adb.grabUiState(deviceSerial)
    }

    fun grabFromFile(path: String?): ToolResult<GrabMeta> {
        val target = if (!path.isNullOrBlank()) {
            path
        } else {
            val latest = store.latestHistoryFile()
                ?: throw AdapterException("INVALID_ARGUMENT", "No .codeLocator file found in ~/.codeLocator_main/historyFile")
            latest.absolutePath
        }
        return store.importFromCodeLocatorFile(target)
    }

    fun listGrabs(): ToolResult<List<GrabMeta>> {
        return ToolResult(success = true, data = store.listGrabs())
    }

    fun openViewer(grabId: String?): ToolResult<ViewerOpenResult> {
        val id = grabId ?: store.latestGrabId()
        val open = viewerManager.open(id)
        return ToolResult(success = true, data = open, grabId = id)
    }

    fun getViewData(grabId: String, memAddr: String): ToolResult<Map<String, Any?>> {
        val snapshot = store.loadSnapshot(grabId)
        return if (snapshot.meta.source == "live") {
            adb.getViewData(grabId, memAddr)
        } else {
            val raw = store.getViewRaw(grabId, memAddr)
            if (raw == null) {
                ToolResult(
                    success = false,
                    error = McpError("VIEW_NOT_FOUND", "mem_addr not found"),
                    grabId = grabId
                )
            } else {
                ToolResult(
                    success = true,
                    data = linkedMapOf("structured" to raw),
                    grabId = grabId
                )
            }
        }
    }

    fun getViewClassInfo(grabId: String, memAddr: String): ToolResult<Map<String, Any?>> {
        val snapshot = store.loadSnapshot(grabId)
        return if (snapshot.meta.source == "live") {
            adb.getViewClassInfo(grabId, memAddr)
        } else {
            val raw = store.getViewRaw(grabId, memAddr)
            if (raw == null) {
                ToolResult(
                    success = false,
                    error = McpError("VIEW_NOT_FOUND", "mem_addr not found"),
                    grabId = grabId
                )
            } else {
                ToolResult(
                    success = true,
                    data = linkedMapOf(
                        "class_name" to raw["ag"],
                        "raw" to raw
                    ),
                    grabId = grabId
                )
            }
        }
    }

    fun traceTouch(grabId: String): ToolResult<Map<String, Any?>> {
        val snapshot = store.loadSnapshot(grabId)
        return if (snapshot.meta.source == "live") {
            adb.traceTouch(grabId)
        } else {
            ToolResult(
                success = true,
                data = linkedMapOf(
                    "view_ids" to emptyList<String>(),
                    "count" to 0,
                    "note" to "trace_touch requires live grab"
                ),
                grabId = grabId
            )
        }
    }

    fun getComposeNode(grabId: String, nodeIdOrKey: String): ToolResult<Map<String, Any?>> {
        val composeIndex = store.getComposeIndex(grabId)
        if (composeIndex.isEmpty()) {
            return ToolResult(
                success = false,
                error = McpError("COMPOSE_NOT_FOUND", "No compose nodes found in snapshot"),
                grabId = grabId
            )
        }

        val exact = composeIndex[nodeIdOrKey]
        if (exact != null) {
            return ToolResult(
                success = true,
                data = linkedMapOf(
                    "compose_key" to exact.composeKey,
                    "host_mem_addr" to exact.hostMemAddr,
                    "node_id" to exact.nodeId,
                    "structured" to exact
                ),
                grabId = grabId
            )
        }

        val suffixMatches = composeIndex.values.filter { it.nodeId == nodeIdOrKey || it.composeKey.endsWith(":$nodeIdOrKey") }
        if (suffixMatches.isEmpty()) {
            return ToolResult(
                success = false,
                error = McpError("COMPOSE_NOT_FOUND", "compose node not found: $nodeIdOrKey"),
                grabId = grabId
            )
        }
        if (suffixMatches.size > 1) {
            return ToolResult(
                success = false,
                error = McpError(
                    "COMPOSE_NODE_AMBIGUOUS",
                    "compose node id is ambiguous, use compose_key",
                    mapOf(
                        "query" to nodeIdOrKey,
                        "candidates" to suffixMatches.take(20).map { it.composeKey }
                    )
                ),
                grabId = grabId
            )
        }

        val node = suffixMatches.first()
        return ToolResult(
            success = true,
            data = linkedMapOf(
                "compose_key" to node.composeKey,
                "host_mem_addr" to node.hostMemAddr,
                "node_id" to node.nodeId,
                "structured" to node
            ),
            grabId = grabId
        )
    }
}
