package com.codereview.platform.controller;

import com.codereview.platform.dto.CommentDTO;
import com.codereview.platform.dto.CreateCommentRequest;
import com.codereview.platform.security.UserPrincipal;
import com.codereview.platform.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pulls/{prId}/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Long prId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
            ){
        CommentDTO comment = commentService.createComment(prId, userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping
    public ResponseEntity<List<CommentDTO>> getCommentsByPR(@PathVariable Long prId){
        List<CommentDTO> comments = commentService.getCommentsByPR(prId);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        commentService.deleteComment(commentId, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }


}
