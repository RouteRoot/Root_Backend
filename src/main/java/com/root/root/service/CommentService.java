package com.root.root.service;

import com.root.root.dto.CommentRequestDto;
import com.root.root.dto.CommentResponseDto;
import com.root.root.entity.Comment;
import com.root.root.entity.Post;
import com.root.root.entity.User;
import com.root.root.repository.CommentRepository;
import com.root.root.repository.PostRepository;
import com.root.root.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long postId) {
        return commentRepository.findByPostId(postId)
                .stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponseDto createComment(String loginId, CommentRequestDto requestDto) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        Post post = postRepository.findById(requestDto.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        Comment comment = Comment.builder()
                .content(requestDto.getContent())
                .author(user)
                .post(post)
                .build();

        return new CommentResponseDto(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto requestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 없습니다."));

        comment.update(requestDto.getContent());
        return new CommentResponseDto(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 없습니다."));

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getMyComments(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        return commentRepository.findByAuthorId(user.getId())
                .stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }
}