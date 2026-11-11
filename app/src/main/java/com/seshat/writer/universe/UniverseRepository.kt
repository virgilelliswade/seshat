package com.seshat.writer.universe

import android.content.Context
import org.json.JSONObject
import java.io.File

object UniverseRepository {

    private const val UNIVERSE_DIR = "blended_universe_projects"

    private fun getProjectsDir(context: Context? = null): File {
        val base = context?.filesDir ?: File(System.getProperty("java.io.tmpdir"), "seshat_files")
        val dir = File(base, UNIVERSE_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getSafeFileName(title: String): String {
        val safe = title.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return if (safe.isBlank()) "untitled_universe.universe.json" else "$safe.universe.json"
    }

    fun saveProject(context: Context? = null, project: BlendedUniverseProject): Boolean {
        return try {
            val dir = getProjectsDir(context)
            val file = File(dir, getSafeFileName(project.title))
            val updated = project.copy(lastModified = System.currentTimeMillis())
            file.writeText(updated.toJson().toString(2))
            true
        } catch (_: Exception) {
            false
        }
    }

    fun loadProject(context: Context? = null, title: String): BlendedUniverseProject? {
        return try {
            val dir = getProjectsDir(context)
            val file = File(dir, getSafeFileName(title))
            if (file.exists() && file.canRead()) {
                BlendedUniverseProject.fromJson(JSONObject(file.readText()))
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun listProjects(context: Context? = null): List<BlendedUniverseProject> {
        return try {
            val dir = getProjectsDir(context)
            val files = dir.listFiles { _, name -> name.endsWith(".universe.json") } ?: emptyArray()
            files.mapNotNull { f ->
                try {
                    BlendedUniverseProject.fromJson(JSONObject(f.readText()))
                } catch (_: Exception) {
                    null
                }
            }.sortedByDescending { it.lastModified }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
