package com.example.jira.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class JiraSettingsConfigurable : Configurable {

    private var settingsComponent: JiraSettingsComponent? = null

    override fun getDisplayName(): String = "Jira Commit Assistant"

    override fun getPreferredFocusedComponent(): JComponent? = settingsComponent?.getPreferredFocusedComponent()

    override fun createComponent(): JComponent {
        val component = JiraSettingsComponent()
        settingsComponent = component
        return component.panel
    }

    override fun isModified(): Boolean {
        val settings = JiraSettingsState.getInstance()
        val component = settingsComponent ?: return false
        return component.baseUrl != settings.baseUrl ||
            component.username != settings.username ||
            component.apiToken != settings.apiToken ||
            component.jqlQuery != settings.jqlQuery ||
            component.maxResults != settings.maxResults
    }

    override fun apply() {
        val settings = JiraSettingsState.getInstance()
        val component = settingsComponent ?: return
        settings.baseUrl = component.baseUrl
        settings.username = component.username
        settings.apiToken = component.apiToken
        settings.jqlQuery = component.jqlQuery
        settings.maxResults = component.maxResults
    }

    override fun reset() {
        val settings = JiraSettingsState.getInstance()
        val component = settingsComponent ?: return
        component.baseUrl = settings.baseUrl
        component.username = settings.username
        component.apiToken = settings.apiToken
        component.jqlQuery = settings.jqlQuery
        component.maxResults = settings.maxResults
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}
