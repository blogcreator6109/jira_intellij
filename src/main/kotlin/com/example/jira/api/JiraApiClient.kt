package com.example.jira.api

import com.example.jira.model.JiraIssue
import com.example.jira.settings.JiraSettingsState
import com.google.gson.JsonParser
import com.intellij.util.io.HttpRequests
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class JiraApiClient(private val settings: JiraSettingsState) {

    fun fetchIssues(): List<JiraIssue> {
        val baseUrl = settings.baseUrl.trim().trimEnd('/')
        if (baseUrl.isEmpty()) return emptyList()
        val encodedJql = URLEncoder.encode(settings.jqlQuery, StandardCharsets.UTF_8)
        val url = "$baseUrl/rest/api/3/search?jql=$encodedJql&maxResults=${settings.maxResults}&fields=summary"

        val authValue = buildAuthHeader(settings.username, settings.apiToken)
        val response = HttpRequests
            .request(url)
            .throwStatusCodeException(true)
            .tuner { connection ->
                connection.setRequestProperty("Authorization", authValue)
                connection.setRequestProperty("Accept", "application/json")
            }
            .readString(null)

        val root = JsonParser.parseString(response).asJsonObject
        val issuesJson = root.getAsJsonArray("issues") ?: return emptyList()

        return issuesJson.mapNotNull { element ->
            val issueObject = element.asJsonObject
            val key = issueObject.get("key")?.asString ?: return@mapNotNull null
            val fields = issueObject.getAsJsonObject("fields")
            val summary = fields?.get("summary")?.asString ?: ""
            JiraIssue(key = key, summary = summary)
        }
    }

    private fun buildAuthHeader(username: String, apiToken: String): String {
        val token = "$username:$apiToken"
        val encoded = Base64.getEncoder().encodeToString(token.toByteArray(StandardCharsets.UTF_8))
        return "Basic $encoded"
    }
}
