package com.discussion.ryu.config;

import com.discussion.ryu.entity.DiscussionPostDocument;
import com.discussion.ryu.repository.DiscussionPostEsRepository;
import com.discussion.ryu.repository.DiscussionPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class EsIndexingRunner implements ApplicationRunner {

    private static final int BATCH_SIZE = 1000;

    private final DiscussionPostRepository jpaRepository;
    private final DiscussionPostEsRepository esRepository;

    @Override
    public void run(ApplicationArguments args) {
        // 이미 색인된 경우 스킵
        if (esRepository.count() > 0) {
            log.info("[ES Indexing] 이미 색인된 데이터가 존재합니다. 스킵합니다. (count={})", esRepository.count());
            return;
        }

        log.info("[ES Indexing] 배치 인덱싱 시작 (배치 사이즈: {})", BATCH_SIZE);

        int pageNum = 0;
        long totalIndexed = 0;

        while (true) {
            Page<com.discussion.ryu.entity.DiscussionPost> page =
                    jpaRepository.findAll(PageRequest.of(pageNum, BATCH_SIZE));

            if (!page.hasContent()) break;

            List<DiscussionPostDocument> docs = page.getContent().stream()
                    .map(post -> DiscussionPostDocument.builder()
                            .id(post.getId().toString())
                            .postId(post.getId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .agreeCount(post.getAgreeCount())
                            .disagreeCount(post.getDisagreeCount())
                            .createdAt(post.getCreatedAt())
                            .build())
                    .toList();

            esRepository.saveAll(docs);
            totalIndexed += docs.size();

            log.info("[ES Indexing] {}페이지 완료 - 누적 {}건 / 전체 {}건",
                    pageNum + 1, totalIndexed, page.getTotalElements());

            if (page.isLast()) break;
            pageNum++;
        }

        log.info("[ES Indexing] 완료. 총 {}건 색인됨.", totalIndexed);
    }
}
