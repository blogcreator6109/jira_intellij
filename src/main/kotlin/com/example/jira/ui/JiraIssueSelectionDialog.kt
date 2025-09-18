package com.example.jira.ui

import com.example.jira.api.JiraApiClient
import com.example.jira.api.JiraIssue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants
import javax.swing.JList

class JiraIssueSelectionDialog(
    private val project: Project,
    initialIssues: List<JiraIssue>,
    private val apiClient: JiraApiClient,
    private val defaultJql: String
) : DialogWrapper(project, true) {

    private val listModel = DefaultListModel<JiraIssue>()
    private val issuesList = JBList(listModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        cellRenderer = object : ColoredListCellRenderer<JiraIssue>() {
            override fun customizeCellRenderer(
                list: JList<out JiraIssue>,
                value: JiraIssue?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                if (value != null) {
                    append(value.key, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    append("  ")
                    append(value.summary)
                    append("  ")
                    append(value.status, SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES)
                }
            }
        }
        visibleRowCount = 10
        fixedCellHeight = JBUI.scale(24)
        preferredSize = Dimension(JBUI.scale(520), JBUI.scale(260))
    }

    private val searchField = JBTextField()
    private val searchButton = JButton("Search")
    var selectedIssue: JiraIssue? = null
        private set

    init {
        title = "Select Jira Ticket"
        issuesList.addListSelectionListener {
            selectedIssue = issuesList.selectedValue
        }
        searchButton.addActionListener { performSearch() }
        searchField.addActionListener { performSearch() }
        init()
        updateIssues(initialIssues)
        if (listModel.size() > 0) {
            issuesList.selectedIndex = 0
        }
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(JBUI.scale(8), JBUI.scale(8)))
        val header = JLabel("Using default JQL: ${defaultJql.ifBlank { "(none)" }}", SwingConstants.LEFT)
        header.border = JBUI.Borders.empty(4)
        panel.add(header, BorderLayout.NORTH)

        panel.add(JBScrollPane(issuesList), BorderLayout.CENTER)

        val searchPanel = JPanel(GridBagLayout())
        val constraints = GridBagConstraints().apply {
            insets = Insets(JBUI.scale(4), JBUI.scale(4), JBUI.scale(4), JBUI.scale(4))
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
        }
        searchPanel.add(searchField, constraints)

        constraints.gridx = 1
        constraints.weightx = 0.0
        constraints.fill = GridBagConstraints.NONE
        searchPanel.add(searchButton, constraints)

        val searchHint = JLabel("Search by key or text to refine results.")
        val hintConstraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 2
            anchor = GridBagConstraints.WEST
            insets = Insets(0, JBUI.scale(4), 0, JBUI.scale(4))
        }
        searchPanel.add(searchHint, hintConstraints)

        panel.add(searchPanel, BorderLayout.SOUTH)
        return panel
    }

    override fun doOKAction() {
        selectedIssue = issuesList.selectedValue
        super.doOKAction()
    }

    private fun performSearch() {
        val query = searchField.text
        searchButton.isEnabled = false
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Searching Jira issues", false) {
            private var result: List<JiraIssue> = emptyList()

            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Contacting Jira..."
                result = apiClient.searchIssues(query, 20)
            }

            override fun onSuccess() {
                updateIssues(result)
                searchButton.isEnabled = true
                if (listModel.size() > 0) {
                    issuesList.selectedIndex = 0
                }
            }

            override fun onThrowable(error: Throwable) {
                searchButton.isEnabled = true
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(
                        project,
                        "Failed to search Jira issues: ${error.message}",
                        "Jira Search"
                    )
                }
            }
        })
    }

    private fun updateIssues(issues: List<JiraIssue>) {
        listModel.clear()
        issues.forEach { listModel.addElement(it) }
    }
}
