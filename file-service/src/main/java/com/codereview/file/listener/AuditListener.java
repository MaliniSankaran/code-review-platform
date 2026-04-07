package com.codereview.file.listener;

import com.codereview.file.event.CommentAddedEvent;
import com.codereview.file.event.PRCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditListener {

    @EventListener
    public void auditPRCreated(PRCreatedEvent event){
        log.info("[AUDIT] PR created — id={}, author={}, repo={}",
                event.getPrId(),
                event.getAuthorId(),
                event.getRepositoryId()
        );
    }
    @EventListener
    public void auditCommentAdded(CommentAddedEvent event) {
        log.info("[AUDIT] Comment added — id={}, pr={}, author={}, lineComment={}",
                event.getCommentId(),
                event.getPullRequestId(),
                event.getAuthorId(),
                event.isLineComment()
        );
    }
}
