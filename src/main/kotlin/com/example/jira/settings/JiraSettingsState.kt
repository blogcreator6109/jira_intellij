package com.example.jira.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "JiraCommitAssistantSettings", storages = [Storage("jiraCommitAssistant.xml")])
class JiraSettingsState : PersistentStateComponent<JiraSettingsState.State> {

    data class State(
        var baseUrl: String = "",
        var username: String = "",
        var apiToken: String = "",
        var jqlQuery: String = "",
        var maxResults: Int = 10
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var baseUrl: String
        get() = state.baseUrl
        set(value) {
            state = state.copy(baseUrl = value)
        }

    var username: String
        get() = state.username
        set(value) {
            state = state.copy(username = value)
        }

    var apiToken: String
        get() = state.apiToken
        set(value) {
            state = state.copy(apiToken = value)
        }

    var jqlQuery: String
        get() = state.jqlQuery
        set(value) {
            state = state.copy(jqlQuery = value)
        }

    var maxResults: Int
        get() = state.maxResults
        set(value) {
            state = state.copy(maxResults = value.coerceIn(1, 50))
        }

    fun isConfigured(): Boolean =
        baseUrl.isNotBlank() && username.isNotBlank() && apiToken.isNotBlank() && jqlQuery.isNotBlank()

    companion object {
        fun getInstance(): JiraSettingsState =
            ApplicationManager.getApplication().getService(JiraSettingsState::class.java)
    }
}
