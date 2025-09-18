package com.example.jira.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.CredentialAttributesKt
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.PasswordSafe
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
        get() {
            val password = PasswordSafe.instance.getPassword(credentialAttributes())
            return password ?: ""
        }
        set(value) {
            if (value.isBlank()) {
                PasswordSafe.instance.set(credentialAttributes(), null)
            } else {
                PasswordSafe.instance.set(credentialAttributes(), Credentials(null, value))
            }
        }

    var defaultJql: String
        get() = state.defaultJql
        set(value) {
            state = state.copy(defaultJql = value)
        }

    private fun credentialAttributes(): CredentialAttributes {
        val serviceName = CredentialAttributesKt.generateServiceName(
            "JiraCommitAssistant",
            "ApiToken"
        )
        return CredentialAttributes(serviceName)
    }

    fun isConfigured(): Boolean = baseUrl.isNotBlank() && apiToken.isNotBlank()

    companion object {
        fun getInstance(): JiraSettingsState = ApplicationManager.getApplication()
            .getService(JiraSettingsState::class.java)
    }
}
