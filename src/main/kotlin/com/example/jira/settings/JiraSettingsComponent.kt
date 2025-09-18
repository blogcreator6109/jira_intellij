package com.example.jira.settings

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane

class JiraSettingsComponent {
    val panel: JPanel = JPanel(BorderLayout(0, JBUI.scale(12))).apply {
        val form = JPanel(GridBagLayout())
        val constraints = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            insets = Insets(JBUI.scale(4), JBUI.scale(4), JBUI.scale(4), JBUI.scale(4))
        }

        fun addRow(row: Int, label: String, component: java.awt.Component) {
            constraints.gridx = 0
            constraints.gridy = row
            constraints.weightx = 0.0
            constraints.fill = GridBagConstraints.NONE
            form.add(JLabel(label), constraints)

            constraints.gridx = 1
            constraints.weightx = 1.0
            constraints.fill = GridBagConstraints.HORIZONTAL
            form.add(component, constraints)
        }

        addRow(0, "Jira Base URL", baseUrlField)
        addRow(1, "User Email (for API token)", emailField)
        addRow(2, "API Token", apiTokenField)

        constraints.gridx = 0
        constraints.gridy = 3
        constraints.weightx = 0.0
        constraints.weighty = 0.0
        constraints.fill = GridBagConstraints.NONE
        form.add(JLabel("Default JQL"), constraints)

        constraints.gridx = 1
        constraints.weightx = 1.0
        constraints.weighty = 1.0
        constraints.fill = GridBagConstraints.BOTH
        defaultJqlArea.lineWrap = true
        defaultJqlArea.wrapStyleWord = true
        defaultJqlArea.border = JBUI.Borders.empty(4)
        form.add(JScrollPane(defaultJqlArea), constraints)

        add(form, BorderLayout.NORTH)
        add(JLabel("Credentials are stored securely in IntelliJ's settings."), BorderLayout.SOUTH)
    }

    val baseUrlField = JBTextField()
    val emailField = JBTextField()
    val apiTokenField = JBPasswordField()
    val defaultJqlArea = JBTextArea(3, 20).apply {
        emptyText.text = "project = KEY ORDER BY updated DESC"
        background = JBColor.PanelBackground
    }

    fun getPreferredFocusedComponent() = baseUrlField
}
