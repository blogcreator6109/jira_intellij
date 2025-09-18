package com.example.jira.commit

import com.example.jira.api.JiraApiClient
import com.example.jira.model.JiraIssue
import com.example.jira.settings.JiraSettingsState
import com.example.jira.ui.JiraIssueSelectionDialog
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.ui.CheckinHandler

class JiraCommitHandler(private val panel: CheckinProjectPanel) : CheckinHandler() {

    override fun beforeCheckin(): ReturnResult {
        val settings = JiraSettingsState.getInstance()
        if (!settings.isConfigured()) {
            return ReturnResult.COMMIT
        }

        val project: Project = panel.project
        val issues = try {
            fetchIssues(project, settings)
        } catch (e: Exception) {
            val decision = Messages.showYesNoDialog(
                project,
                "Failed to load Jira issues. ${e.message?.trim()}\nDo you want to commit without selecting a ticket?",
                "Jira Commit Assistant",
                "Commit Without Ticket",
                "Cancel",
                null
            )
            return if (decision == Messages.YES) ReturnResult.COMMIT else ReturnResult.CANCEL
        }

        val dialog = JiraIssueSelectionDialog(project, issues, settings.jqlQuery)
        dialog.show()

        return when {
            dialog.exitCode == JiraIssueSelectionDialog.COMMIT_WITHOUT_TICKET_EXIT_CODE || dialog.isCommitWithoutIssue() -> {
                ReturnResult.COMMIT
            }

            dialog.exitCode == DialogWrapper.OK_EXIT_CODE -> {
                val selected = dialog.selectedIssue()
                if (selected != null) {
                    applyIssueToCommitMessage(selected)
                    ReturnResult.COMMIT
                } else {
                    Messages.showWarningDialog(
                        project,
                        "You must select a Jira ticket or choose to commit without one.",
                        "Jira Commit Assistant"
                    )
                    ReturnResult.CANCEL
                }
            }

            else -> ReturnResult.CANCEL
        }
    }

    private fun fetchIssues(project: Project, settings: JiraSettingsState): List<JiraIssue> {
        val apiClient = JiraApiClient(settings)
        return ProgressManager.getInstance().runProcessWithProgressSynchronously(
            Computable { apiClient.fetchIssues() },
            "Loading Jira Issues",
            true,
            project
        )
    }

    private fun applyIssueToCommitMessage(issue: JiraIssue) {
        val currentMessage = panel.commitMessage ?: ""
        val sanitized = currentMessage.replaceFirst(COMMIT_PREFIX_REGEX, "").trimStart()
        val suffix = sanitized.takeIf { it.isNotEmpty() } ?: ""
        val newMessage = buildString {
            append("[")
            append(issue.key)
            append("]")
            if (suffix.isNotEmpty()) {
                append(' ')
                append(suffix)
            }
        }
        panel.commitMessage = newMessage
    }

    companion object {
        private val COMMIT_PREFIX_REGEX = Regex("^\\[[A-Z][A-Z0-9_-]+]\\s*")
    }
}
