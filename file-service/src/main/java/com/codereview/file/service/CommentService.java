package com.codereview.file.service;

import com.codereview.file.dto.CommentDTO;
import com.codereview.file.dto.CreateCommentRequest;
import com.codereview.file.entity.CodeFile;
import com.codereview.file.entity.Comment;
import com.codereview.file.entity.PullRequest;
import com.codereview.file.entity.User;
import com.codereview.file.event.CommentAddedEvent;
import com.codereview.file.exception.ResourceNotFoundException;
import com.codereview.file.repository.CodeFileRepository;
import com.codereview.file.repository.CommentRepository;
import com.codereview.file.repository.PullRequestRepository;
import com.codereview.file.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final PullRequestRepository pullRequestRepository;
    private final CodeFileRepository codeFileRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    //Create method
    @Transactional
    public CommentDTO createComment(Long prId, Long authorId, CreateCommentRequest request){

        log.info("Creating comment for PR {} by user {}", prId, authorId);

        PullRequest pr = pullRequestRepository.findById(prId)
                .orElseThrow(()->new ResourceNotFoundException(" Pull request not found with id: " + prId));

        User author = userRepository.findById(authorId)
                .orElseThrow(()->new ResourceNotFoundException(" User not found with id: " + authorId));

        //Build comment with required fields
        Comment comment = Comment.builder()
                .content(request.getContent())
                .pullRequest(pr)
                .author(author)
                .build();

        //Set optional fields if provided
        if(request.getCodeFileId()!=null){
            CodeFile codeFile = codeFileRepository.findById(request.getCodeFileId())
                    .orElseThrow(()->new ResourceNotFoundException(" Code file not found with id: " + request.getCodeFileId()));
            comment.setCodeFile(codeFile);
            comment.setLineNumber(request.getLineNumber());
        }

        comment = commentRepository.save(comment);

        log.info("Comment created with id: {}", comment.getId());

        eventPublisher.publishEvent(new CommentAddedEvent(
                comment.getId(),
                prId,
                authorId,
                comment.getAuthor().getUsername(),
                comment.getLineNumber() != null
        ));

        return mapToDTO(comment);
    }

    //Read and delete methods
    public List<CommentDTO> getCommentsByPR(Long prId){
        log.info("Fetching comments for PR {}", prId);

        pullRequestRepository.findById(prId)
                .orElseThrow(()->new ResourceNotFoundException(" Pull request not found with id: " + prId));

        return commentRepository.findByPullRequestId(prId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<CommentDTO> getCommentsByFile(Long fileId){
        log.info("Fetching comments for file {}", fileId);

        codeFileRepository.findById(fileId)
                .orElseThrow(()->new ResourceNotFoundException(" Code file not found with id: " + fileId));

        return commentRepository.findByCodeFileId(fileId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId){

        log.info("Deleting comment {} by user {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()->new ResourceNotFoundException(" Comment not found with id: " + commentId));

        if(!comment.getAuthor().getId().equals(userId)){
            throw new AccessDeniedException("You don't have permission to delete this comment");
        }
        commentRepository.delete(comment);

        log.info("Comment {} deleted", commentId);
    }

    private CommentDTO mapToDTO(Comment comment){
        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .pullRequestId(comment.getPullRequest().getId())
                .codeFileId(comment.getCodeFile() != null ? comment.getCodeFile().getId() : null)
                .codeFileName(comment.getCodeFile() != null ? comment.getCodeFile().getFileName() : null)
                .lineNumber(comment.getLineNumber())
                .authorId(comment.getAuthor().getId())
                .authorUserName(comment.getAuthor().getUsername())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
