package com.example.jira.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(name = "JiraSettingsState", storages = [Storage("jiraCommitAssistant.xml")])
class JiraSettingsState : PersistentStateComponent<JiraSettingsState.State> {
    data class State(
        var baseUrl: String = "",
        var email: String = "",
        var apiToken: String = "",
        var defaultJql: String = "project = MYPROJECT ORDER BY updated DESC"
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

    var email: String
        get() = state.email
        set(value) {
            state = state.copy(email = value)
        }

    var apiToken: String
        get() = state.apiToken
        set(value) {
            state = state.copy(apiToken = value)
        }

    var defaultJql: String
        get() = state.defaultJql
        set(value) {
            state = state.copy(defaultJql = value)
        }

    fun isConfigured(): Boolean = baseUrl.isNotBlank() && apiToken.isNotBlank()

    companion object {
        fun getInstance(): JiraSettingsState = ApplicationManager.getApplication()
            .getService(JiraSettingsState::class.java)
    }
}
