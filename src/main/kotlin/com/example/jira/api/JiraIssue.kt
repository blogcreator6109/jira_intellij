package com.example.jira.api

data class JiraIssue(
    val key: String,
    val summary: String,
    val status: String
) {
    override fun toString(): String = "[$key] $summary ($status)"
}
