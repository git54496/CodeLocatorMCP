package com.bytedance.tools.codelocator.adapter

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlin.math.max

object SnapshotMapper {

    fun map(meta: GrabMeta, appJsonRaw: String, screenshotRef: String?): GrabSnapshot {
        val app = Jsons.parseObject(appJsonRaw)
        val activity = getObj(app, "b7", "mActivity")
        val roots = getArray(activity, "cj", "mDecorViews")
            ?: getArray(app, "cj", "mDecorViews")
            ?: JsonArray()

        val tree = roots.mapNotNull { parseView(it.asJsonObject) }
        val index = linkedMapOf<String, ViewIndexItem>()
        val composeIndex = linkedMapOf<String, ComposeIndexItem>()
        tree.forEach { fillIndex(it, index) }
        tree.forEach { fillComposeIndex(it, composeIndex) }

        return GrabSnapshot(
            meta = meta,
            uiTree = tree,
            screenshotRef = screenshotRef,
            indexes = index,
            composeIndexes = composeIndex
        )
    }

    fun detectPackage(appJsonRaw: String): String? {
        return runCatching { getString(Jsons.parseObject(appJsonRaw), "bd", "mPackageName") }.getOrNull()
    }

    fun detectActivity(appJsonRaw: String): String? {
        return runCatching {
            val app = Jsons.parseObject(appJsonRaw)
            val activity = getObj(app, "b7", "mActivity")
            getString(activity, "ag", "mClassName")
        }.getOrNull()
    }

    private fun fillIndex(node: ViewNodeDto, out: MutableMap<String, ViewIndexItem>) {
        out[node.memAddr] = ViewIndexItem(
            memAddr = node.memAddr,
            className = node.className,
            idStr = node.idStr,
            text = node.text,
            left = node.left,
            top = node.top,
            width = node.width,
            height = node.height
        )
        node.children.forEach { fillIndex(it, out) }
    }

    private fun fillComposeIndex(node: ViewNodeDto, out: MutableMap<String, ComposeIndexItem>) {
        node.composeNodes.forEach { composeNode ->
            fillComposeNodeIndex(node.memAddr, composeNode, out)
        }
        node.children.forEach { fillComposeIndex(it, out) }
    }

    private fun fillComposeNodeIndex(hostMemAddr: String, node: ComposeNodeDto, out: MutableMap<String, ComposeIndexItem>) {
        val composeKey = "$hostMemAddr:${node.nodeId}"
        out[composeKey] = ComposeIndexItem(
            composeKey = composeKey,
            hostMemAddr = hostMemAddr,
            nodeId = node.nodeId,
            left = node.left,
            top = node.top,
            right = node.right,
            bottom = node.bottom,
            text = node.text,
            contentDescription = node.contentDescription,
            testTag = node.testTag,
            clickable = node.clickable,
            enabled = node.enabled,
            focused = node.focused,
            visibleToUser = node.visibleToUser,
            selected = node.selected,
            checkable = node.checkable,
            checked = node.checked,
            focusable = node.focusable,
            actions = node.actions
        )
        node.children.forEach { child -> fillComposeNodeIndex(hostMemAddr, child, out) }
    }

    private fun parseView(viewObj: JsonObject): ViewNodeDto? {
        val memAddr = getString(viewObj, "af", "mMemAddr") ?: return null
        val className = getString(viewObj, "ag", "mClassName") ?: "UnknownView"
        val idStr = getString(viewObj, "ac", "mIdStr")
        val text = getString(viewObj, "aq", "mText")

        val left = getInt(viewObj, "d", "mLeft") ?: 0
        val top = getInt(viewObj, "f", "mTop") ?: 0
        val right = getInt(viewObj, "e", "mRight") ?: left
        val bottom = getInt(viewObj, "g", "mBottom") ?: top
        val width = max(0, right - left)
        val height = max(0, bottom - top)

        val visibility = getString(viewObj, "ab", "mVisibility")
        val visible = visibility?.let { it != "8" } ?: true
        val alpha = getDouble(viewObj, "ae", "mAlpha") ?: 1.0

        val composeArray = getArray(viewObj, "b5", "mComposeNodes") ?: JsonArray()
        val composeNodes = composeArray.mapNotNull { compose ->
            if (compose.isJsonObject) parseComposeNode(compose.asJsonObject) else null
        }

        val childrenArray = getArray(viewObj, "a", "mChildren") ?: JsonArray()
        val children = childrenArray.mapNotNull { child ->
            if (child.isJsonObject) parseView(child.asJsonObject) else null
        }

        return ViewNodeDto(
            memAddr = memAddr,
            className = className,
            idStr = idStr,
            text = text,
            left = left,
            top = top,
            width = width,
            height = height,
            visible = visible,
            alpha = alpha,
            composeNodes = composeNodes,
            children = children,
            raw = Jsons.elementToAny(viewObj) as? Map<String, Any?> ?: emptyMap()
        )
    }

    private fun parseComposeNode(nodeObj: JsonObject): ComposeNodeDto? {
        val nodeId = getString(nodeObj, "a", "nodeId") ?: return null
        val left = getInt(nodeObj, "b", "left") ?: 0
        val top = getInt(nodeObj, "c", "top") ?: 0
        val right = getInt(nodeObj, "d", "right") ?: left
        val bottom = getInt(nodeObj, "e", "bottom") ?: top
        val text = getString(nodeObj, "f", "text")
        val contentDescription = getString(nodeObj, "g", "contentDescription")
        val testTag = getString(nodeObj, "h", "testTag")
        val clickable = getBoolean(nodeObj, "i", "clickable") ?: false
        val enabled = getBoolean(nodeObj, "j", "enabled") ?: true
        val focused = getBoolean(nodeObj, "k", "focused") ?: false
        val visibleToUser = getBoolean(nodeObj, "l", "visibleToUser") ?: true
        val selected = getBoolean(nodeObj, "m", "selected") ?: false
        val checkable = getBoolean(nodeObj, "n", "checkable") ?: false
        val checked = getBoolean(nodeObj, "o", "checked") ?: false
        val focusable = getBoolean(nodeObj, "p", "focusable") ?: false

        val actionArray = getArray(nodeObj, "q", "actions") ?: JsonArray()
        val actions = actionArray.mapNotNull { action ->
            if (action.isJsonPrimitive) runCatching { action.asJsonPrimitive.asString }.getOrNull() else null
        }

        val childArray = getArray(nodeObj, "r", "children") ?: JsonArray()
        val children = childArray.mapNotNull { child ->
            if (child.isJsonObject) parseComposeNode(child.asJsonObject) else null
        }

        return ComposeNodeDto(
            nodeId = nodeId,
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            text = text,
            contentDescription = contentDescription,
            testTag = testTag,
            clickable = clickable,
            enabled = enabled,
            focused = focused,
            visibleToUser = visibleToUser,
            selected = selected,
            checkable = checkable,
            checked = checked,
            focusable = focusable,
            actions = actions,
            children = children,
            raw = Jsons.elementToAny(nodeObj) as? Map<String, Any?> ?: emptyMap()
        )
    }

    private fun getObj(obj: JsonObject?, vararg keys: String): JsonObject? {
        if (obj == null) return null
        keys.forEach { key ->
            if (obj.has(key) && obj[key].isJsonObject) return obj[key].asJsonObject
        }
        return null
    }

    private fun getArray(obj: JsonObject?, vararg keys: String): JsonArray? {
        if (obj == null) return null
        keys.forEach { key ->
            if (obj.has(key) && obj[key].isJsonArray) return obj[key].asJsonArray
        }
        return null
    }

    private fun getString(obj: JsonObject?, vararg keys: String): String? {
        if (obj == null) return null
        keys.forEach { key ->
            val ele: JsonElement = obj.get(key) ?: return@forEach
            if (ele.isJsonNull) return@forEach
            if (ele.isJsonPrimitive) return ele.asJsonPrimitive.asString
        }
        return null
    }

    private fun getInt(obj: JsonObject?, vararg keys: String): Int? {
        if (obj == null) return null
        keys.forEach { key ->
            val ele = obj.get(key) ?: return@forEach
            if (ele.isJsonPrimitive) {
                return runCatching { ele.asJsonPrimitive.asInt }.getOrNull()
            }
        }
        return null
    }

    private fun getDouble(obj: JsonObject?, vararg keys: String): Double? {
        if (obj == null) return null
        keys.forEach { key ->
            val ele = obj.get(key) ?: return@forEach
            if (ele.isJsonPrimitive) {
                return runCatching { ele.asJsonPrimitive.asDouble }.getOrNull()
            }
        }
        return null
    }

    private fun getBoolean(obj: JsonObject?, vararg keys: String): Boolean? {
        if (obj == null) return null
        keys.forEach { key ->
            val ele = obj.get(key) ?: return@forEach
            if (!ele.isJsonPrimitive) return@forEach
            val p = ele.asJsonPrimitive
            if (p.isBoolean) return p.asBoolean
            if (p.isNumber) return runCatching { p.asInt != 0 }.getOrNull()
            if (p.isString) {
                return when (p.asString.trim().lowercase()) {
                    "true", "1", "y", "yes" -> true
                    "false", "0", "n", "no" -> false
                    else -> null
                }
            }
        }
        return null
    }
}
