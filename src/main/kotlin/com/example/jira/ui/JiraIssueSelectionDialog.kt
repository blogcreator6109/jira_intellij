package com.example.jira.ui

import com.example.jira.model.JiraIssue
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class JiraIssueSelectionDialog(
    project: Project,
    private val issues: List<JiraIssue>,
    private val jql: String
) : DialogWrapper(project) {

    private val issueList = JBList(issues)
    private var commitWithoutIssue = false

    init {
        title = "Select Jira Issue"
        issueList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        issueList.cellRenderer = object : ColoredListCellRenderer<JiraIssue>() {
            override fun customizeCellRenderer(
                list: javax.swing.JList<out JiraIssue>,
                value: JiraIssue?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                if (value != null) {
                    append(value.key, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    if (value.summary.isNotBlank()) {
                        append("  ")
                        append(value.summary, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    }
                }
            }
        }
        issueList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2 && isOKActionEnabled) {
                    okAction.actionPerformed(null)
                }
            }
        })
        issueList.emptyText.text = "No issues found for the configured JQL."
        if (issues.isNotEmpty()) {
            issueList.selectedIndex = 0
        }
        okAction.isEnabled = issues.isNotEmpty()
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(0, 8))
        panel.add(JBLabel("Select a Jira ticket to prefix your commit message"), BorderLayout.NORTH)
        panel.add(JBScrollPane(issueList), BorderLayout.CENTER)
        panel.add(JBLabel("JQL: $jql").apply { setCopyable(true) }, BorderLayout.SOUTH)
        return panel
    }

    override fun getPreferredFocusedComponent(): JComponent = issueList

    override fun createActions(): Array<Action> {
        val skipAction = object : DialogWrapperAction("Commit Without Ticket") {
            override fun doAction(e: ActionEvent) {
                commitWithoutIssue = true
                close(COMMIT_WITHOUT_TICKET_EXIT_CODE)
            }
        }
        return arrayOf(okAction, skipAction, cancelAction)
    }

    fun selectedIssue(): JiraIssue? = issueList.selectedValue

    fun isCommitWithoutIssue(): Boolean = commitWithoutIssue

    companion object {
        const val COMMIT_WITHOUT_TICKET_EXIT_CODE = NEXT_USER_EXIT_CODE
    }
}
