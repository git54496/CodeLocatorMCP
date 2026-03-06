package com.bytedance.tools.codelocator.adapter

object AdapterCli {
    fun run(args: Array<String>, service: AdapterService, viewerManager: ViewerManager): Int {
        return try {
            val commandArgs = if (args.isEmpty()) listOf("live") else args.toList()
            when (commandArgs[0]) {
                "version", "-v", "--version" -> {
                    println(BuildInfo.version)
                    0
                }
                "help", "-h", "--help" -> {
                    printUsage()
                    0
                }
                "mcp" -> {
                    McpStdioServer(service).run()
                    0
                }

                // Backward compatibility: keep support for "grab grab live".
                "grab" -> if (commandArgs.size == 1) runGrab(listOf("live"), service) else runGrab(commandArgs.drop(1), service)
                "live", "file" -> runGrab(commandArgs, service)
                "list" -> runGrabs(emptyList(), service)
                "grabs" -> runGrabs(commandArgs.drop(1), service)
                "viewer" -> runViewer(commandArgs.drop(1), service, viewerManager)
                "inspect" -> runInspect(commandArgs.drop(1), service)
                else -> {
                    printUsage()
                    1
                }
            }
        } catch (e: AdapterException) {
            println(Jsons.toJson(ToolResult<Any>(success = false, error = McpError(e.code, e.message, e.details))))
            1
        } catch (t: Throwable) {
            println(Jsons.toJson(ToolResult<Any>(success = false, error = McpError("INTERNAL_ERROR", t.message ?: "error"))))
            1
        }
    }

    private fun runGrab(args: List<String>, service: AdapterService): Int {
        if (args.isEmpty()) {
            printUsage()
            return 1
        }
        return when (args[0]) {
            "help", "-h", "--help" -> {
                printUsage()
                0
            }
            "live" -> {
                val device = readOption(args, "--device-serial")
                val result = service.grabLive(device)
                println(Jsons.toJson(result))
                if (result.success) 0 else 1
            }

            "file" -> {
                val path = readOption(args, "--path")
                val result = service.grabFromFile(path)
                println(Jsons.toJson(result))
                if (result.success) 0 else 1
            }

            else -> {
                printUsage()
                1
            }
        }
    }

    private fun runGrabs(args: List<String>, service: AdapterService): Int {
        if (args.isNotEmpty() && args.firstOrNull() != "list") {
            printUsage()
            return 1
        }
        val result = service.listGrabs()
        println(Jsons.toJson(result))
        return if (result.success) 0 else 1
    }

    private fun runViewer(args: List<String>, service: AdapterService, viewerManager: ViewerManager): Int {
        if (args.isEmpty()) {
            printUsage()
            return 1
        }
        return when (args[0]) {
            "open" -> {
                val grabId = readOption(args, "--grab-id")
                val result = service.openViewer(grabId)
                println(Jsons.toJson(result))
                if (result.success) 0 else 1
            }

            "serve" -> {
                val port = readOption(args, "--port")?.toIntOrNull() ?: 17333
                viewerManager.serve(port)
                0
            }

            else -> {
                printUsage()
                1
            }
        }
    }

    private fun runInspect(args: List<String>, service: AdapterService): Int {
        if (args.isEmpty()) {
            printUsage()
            return 1
        }
        return when (args[0]) {
            "help", "-h", "--help" -> {
                printUsage()
                0
            }

            "view-data" -> {
                val grabId = requireOpt(args, "--grab-id")
                val memAddr = requireOpt(args, "--mem-addr")
                val result = service.getViewData(grabId, memAddr)
                println(Jsons.toJson(result))
                if (result.success) 0 else 1
            }

            "class-info" -> {
                val grabId = requireOpt(args, "--grab-id")
                val memAddr = requireOpt(args, "--mem-addr")
                val result = service.getViewClassInfo(grabId, memAddr)
                println(Jsons.toJson(result))
                if (result.success) 0 else 1
            }

            "touch" -> {
                val grabId = requireOpt(args, "--grab-id")
                val result = service.traceTouch(grabId)
                println(Jsons.toJson(result))
                if (result.success) 0 else 1
            }

            "compose-node" -> {
                val grabId = requireOpt(args, "--grab-id")
                val nodeId = requireOpt(args, "--node-id")
                val result = service.getComposeNode(grabId, nodeId)
                println(Jsons.toJson(result))
                if (result.success) 0 else 1
            }

            else -> {
                printUsage()
                1
            }
        }
    }

    private fun readOption(args: List<String>, key: String): String? {
        val idx = args.indexOf(key)
        if (idx < 0 || idx + 1 >= args.size) return null
        return args[idx + 1]
    }

    private fun requireOpt(args: List<String>, key: String): String {
        return readOption(args, key) ?: throw AdapterException("INVALID_ARGUMENT", "Missing option: $key")
    }

    private fun printUsage() {
        println(
            """
            grab usage:
              grab                            # default: grab live
              grab --version
              grab --help
              grab help
              grab live --device-serial <optional>
              grab file --path <optional>
              grab list
              grab viewer open --grab-id <id>
              grab viewer serve --port <port>
              grab inspect view-data --grab-id <id> --mem-addr <addr>
              grab inspect class-info --grab-id <id> --mem-addr <addr>
              grab inspect touch --grab-id <id>
              grab inspect compose-node --grab-id <id> --node-id <compose_node_id_or_compose_key>
              grab mcp

            legacy compatibility:
              grab grab live
              grab grabs list
            """.trimIndent()
        )
    }
}
