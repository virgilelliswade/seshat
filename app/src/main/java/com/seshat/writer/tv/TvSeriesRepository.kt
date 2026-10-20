package com.seshat.writer.tv

import android.content.Context
import org.json.JSONObject
import java.io.File

object TvSeriesRepository {

    private const val TV_PROJECTS_DIR = "tv_series_projects"

    private fun getProjectsDir(context: Context): File {
        val dir = File(context.filesDir, TV_PROJECTS_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getSafeFileName(title: String): String {
        val safe = title.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return if (safe.isBlank()) "untitled_tv_series.tv.json" else "$safe.tv.json"
    }

    fun saveProject(context: Context, project: TvSeriesProject): Boolean {
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

    fun loadProject(context: Context, title: String): TvSeriesProject? {
        return try {
            val dir = getProjectsDir(context)
            val file = File(dir, getSafeFileName(title))
            if (file.exists() && file.canRead()) {
                val json = JSONObject(file.readText())
                TvSeriesProject.fromJson(json)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun listProjects(context: Context): List<TvSeriesProject> {
        return try {
            val dir = getProjectsDir(context)
            val files = dir.listFiles { _, name -> name.endsWith(".tv.json") } ?: emptyArray()
            files.mapNotNull { f ->
                try {
                    val json = JSONObject(f.readText())
                    TvSeriesProject.fromJson(json)
                } catch (_: Exception) {
                    null
                }
            }.sortedByDescending { it.lastModified }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun deleteProject(context: Context, title: String): Boolean {
        return try {
            val dir = getProjectsDir(context)
            val file = File(dir, getSafeFileName(title))
            if (file.exists()) file.delete() else true
        } catch (_: Exception) {
            false
        }
    }
}
