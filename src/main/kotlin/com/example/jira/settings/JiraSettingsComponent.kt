package com.example.jira.settings

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class JiraSettingsComponent {

    private val baseUrlField = JBTextField()
    private val usernameField = JBTextField()
    private val apiTokenField = JBPasswordField()
    private val jqlField = JBTextArea(5, 0)
    private val maxResultsSpinner = JSpinner(SpinnerNumberModel(10, 1, 50, 1))

    val panel: JPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent(JBLabel("Jira Base URL"), baseUrlField, 1, false)
        .addLabeledComponent(JBLabel("Username / Email"), usernameField, 1, false)
        .addLabeledComponent(JBLabel("API Token"), apiTokenField, 1, false)
        .addLabeledComponent(JBLabel("JQL"), jqlField, 1, false)
        .addLabeledComponent(JBLabel("Max Results"), maxResultsSpinner, 1, false)
        .addComponentFillVertically(JPanel(), 0)
        .panel

    init {
        jqlField.lineWrap = true
        jqlField.wrapStyleWord = true
        jqlField.minimumSize = Dimension(200, 80)
    }

    fun getPreferredFocusedComponent(): JComponent = baseUrlField

    var baseUrl: String
        get() = baseUrlField.text.trim()
        set(value) {
            baseUrlField.text = value
        }

    var username: String
        get() = usernameField.text.trim()
        set(value) {
            usernameField.text = value
        }

    var apiToken: String
        get() = String(apiTokenField.password)
        set(value) {
            apiTokenField.text = value
        }

    var jqlQuery: String
        get() = jqlField.text.trim()
        set(value) {
            jqlField.text = value
        }

    var maxResults: Int
        get() = (maxResultsSpinner.value as? Int) ?: 10
        set(value) {
            maxResultsSpinner.value = value
        }
}
