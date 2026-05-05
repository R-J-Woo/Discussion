package com.discussion.ryu.config;

import com.discussion.ryu.entity.DiscussionPostDocument;
import com.discussion.ryu.repository.DiscussionPostEsRepository;
import com.discussion.ryu.repository.DiscussionPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class EsIndexingRunner implements ApplicationRunner {

    private final DiscussionPostRepository jpaRepository;
    private final DiscussionPostEsRepository esRepository;

    @Override
    public void run(ApplicationArguments args) {
        // 이미 색인된 경우 스킵 (운영에서는 조건 추가 필요)
        List<DiscussionPostDocument> docs = jpaRepository.findAll().stream()
                .map(post -> DiscussionPostDocument.builder()
                        .id(post.getId().toString())
                        .postId(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .agreeCount(post.getAgreeCount())
                        .disagreeCount(post.getDisagreeCount())
                        .createdAt(post.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        esRepository.saveAll(docs);
    }
}
