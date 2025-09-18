package com.example.jira.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class JiraSettingsConfigurable : Configurable {
    private val settingsState = JiraSettingsState.getInstance()
    private var component: JiraSettingsComponent? = null

    override fun getDisplayName(): String = "Jira Commit Assistant"

    override fun createComponent(): JComponent {
        val comp = JiraSettingsComponent()
        component = comp
        reset()
        return comp.panel
    }

    override fun isModified(): Boolean {
        val comp = component ?: return false
        return comp.baseUrlField.text != settingsState.baseUrl ||
            comp.emailField.text != settingsState.email ||
            String(comp.apiTokenField.password) != settingsState.apiToken ||
            comp.defaultJqlArea.text != settingsState.defaultJql
    }

    override fun apply() {
        val comp = component ?: return
        settingsState.baseUrl = comp.baseUrlField.text.trim()
        settingsState.email = comp.emailField.text.trim()
        settingsState.apiToken = String(comp.apiTokenField.password).trim()
        settingsState.defaultJql = comp.defaultJqlArea.text.trim()
    }

    override fun reset() {
        val comp = component ?: return
        comp.baseUrlField.text = settingsState.baseUrl
        comp.emailField.text = settingsState.email
        comp.apiTokenField.text = settingsState.apiToken
        comp.defaultJqlArea.text = settingsState.defaultJql
    }

    override fun disposeUIResources() {
        component = null
    }

    override fun getPreferredFocusedComponent(): JComponent? = component?.getPreferredFocusedComponent()
}
