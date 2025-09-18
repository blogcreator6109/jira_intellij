package com.example.jira.commit

import com.example.jira.api.JiraApiClient
import com.example.jira.api.JiraIssue
import com.example.jira.settings.JiraSettingsState
import com.example.jira.ui.JiraIssueSelectionDialog
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.intellij.openapi.vcs.checkin.CheckinMetaHandler
import com.intellij.openapi.vcs.checkin.CommitContext
import com.intellij.util.ThrowableComputable

class JiraCommitHandlerFactory : CheckinHandlerFactory() {
    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return JiraCommitHandler(panel)
    }
}

private class JiraCommitHandler(private val panel: CheckinProjectPanel) : CheckinHandler(), CheckinMetaHandler {
    private val keyPattern = Regex("^\\[[A-Za-z][A-Za-z0-9_]*-\\d+]\\s?.*")

    override fun runCheckinHandlers(runnable: Runnable) {
        val settings = JiraSettingsState.getInstance()
        if (!settings.isConfigured()) {
            runnable.run()
            return
        }

        val existingMessage = panel.commitMessage ?: ""
        if (keyPattern.containsMatchIn(existingMessage.trimStart())) {
            runnable.run()
            return
        }

        val project = panel.project
        val apiClient = JiraApiClient(settings)
        val issues = loadIssues(project, apiClient)

        if (issues.isEmpty()) {
            val choice = Messages.showYesNoDialog(
                project,
                "No Jira issues were found for the configured JQL. Do you want to commit without a ticket?",
                "Jira Commit Assistant",
                "Commit Anyway",
                "Cancel",
                Messages.getWarningIcon()
            )
            if (choice == Messages.YES) {
                runnable.run()
            }
            return
        }

        val dialog = JiraIssueSelectionDialog(project, issues, apiClient, settings.defaultJql)
        if (!dialog.showAndGet()) {
            return
        }

        val selected = dialog.selectedIssue ?: return
        val sanitizedMessage = existingMessage.trimStart()
        val newMessage = buildString {
            append("[")
            append(selected.key)
            append("]")
            if (sanitizedMessage.isNotBlank()) {
                append(' ')
                append(sanitizedMessage)
            }
        }
        panel.setCommitMessage(newMessage)
        runnable.run()
    }

    private fun loadIssues(project: Project, apiClient: JiraApiClient): List<JiraIssue> {
        return try {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                ThrowableComputable<List<JiraIssue>, RuntimeException> {
                    apiClient.fetchDefaultIssues(10)
                },
                "Loading Jira Issues",
                true,
                project
            ) ?: emptyList()
        } catch (ex: Exception) {
            Messages.showErrorDialog(
                project,
                "Failed to load Jira issues: ${ex.message}",
                "Jira Commit Assistant"
            )
            emptyList()
        }
    }
}
