package com.example.jira.api

import com.example.jira.settings.JiraSettingsState
import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.Logger
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.Base64

class JiraApiClient(private val settings: JiraSettingsState) {
    private val log = Logger.getInstance(JiraApiClient::class.java)
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    fun fetchDefaultIssues(limit: Int): List<JiraIssue> {
        val jql = settings.defaultJql.takeIf { it.isNotBlank() }
            ?: "order by updated desc"
        return executeSearch(jql, limit)
    }

    fun searchIssues(searchTerm: String, limit: Int): List<JiraIssue> {
        val sanitized = searchTerm.trim()
        if (sanitized.isEmpty()) {
            return fetchDefaultIssues(limit)
        }

        val searchJql = if (sanitized.matches(Regex("[A-Za-z][A-Za-z0-9_]*-\\d+"))) {
            "issuekey = ${sanitized.uppercase()}"
        } else {
            val escaped = sanitized.replace("\"", "\\\"")
            "summary ~ \"$escaped\" OR description ~ \"$escaped\""
        }

        val combinedJql = listOfNotNull(
            settings.defaultJql.takeIf { it.isNotBlank() }?.let { "($it)" },
            "($searchJql)"
        ).joinToString(" AND ")

        return executeSearch(combinedJql.ifBlank { searchJql }, limit)
    }

    private fun executeSearch(jql: String, limit: Int): List<JiraIssue> {
        val baseUrl = settings.baseUrl.trimEnd('/')
        if (baseUrl.isEmpty()) return emptyList()

        val encodedJql = URLEncoder.encode(jql, StandardCharsets.UTF_8)
        val path = "rest/api"
        val errors = mutableListOf<String>()

        for (version in listOf("3", "2")) {
            val uri = URI.create("$baseUrl/$path/$version/search?jql=$encodedJql&maxResults=$limit")
            val request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .GET()
                .header("Accept", "application/json")
                .apply {
                    val token = settings.apiToken
                    if (settings.email.isNotBlank()) {
                        val auth = Base64.getEncoder()
                            .encodeToString("${settings.email}:${token}".toByteArray(StandardCharsets.UTF_8))
                        header("Authorization", "Basic $auth")
                    } else {
                        header("Authorization", "Bearer ${token}")
                    }
                }
                .build()

            try {
                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() in 200..299) {
                    return parseIssues(response.body())
                }
                val message = "Jira API v$version search failed: HTTP ${response.statusCode()}"
                log.warn(message)
                errors += message
            } catch (ex: Exception) {
                val message = "Jira API v$version search failed: ${ex.message}"
                log.warn(message, ex)
                errors += message
            }
        }

        if (errors.isNotEmpty()) {
            log.warn("All Jira search attempts failed: ${errors.joinToString(", ")}")
        }
        return emptyList()
    }

    private fun parseIssues(body: String): List<JiraIssue> {
        val root = JsonParser.parseString(body).asJsonObject
        val issuesArray = root.getAsJsonArray("issues") ?: return emptyList()
        return issuesArray.mapNotNull { element ->
            val issueObj = element.asJsonObject
            val key = issueObj.get("key")?.asString ?: return@mapNotNull null
            val fields = issueObj.getAsJsonObject("fields") ?: return@mapNotNull null
            val summary = fields.get("summary")?.asString ?: "(no summary)"
            val status = fields.getAsJsonObject("status")?.get("name")?.asString ?: "Unknown"
            JiraIssue(key, summary, status)
        }
    }
}
