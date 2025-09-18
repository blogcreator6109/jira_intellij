package com.example.jira.commit

import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.changes.ui.CheckinHandler
import com.intellij.openapi.vcs.changes.ui.CheckinHandlerFactory

class JiraCommitHandlerFactory : CheckinHandlerFactory() {
    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return JiraCommitHandler(panel)
    }
}
