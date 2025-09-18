package com.example.jira.model

data class JiraIssue(
    val key: String,
    val summary: String
) {
    override fun toString(): String = "[$key] $summary"
}
