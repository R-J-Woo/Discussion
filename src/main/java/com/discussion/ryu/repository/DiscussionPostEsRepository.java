package com.discussion.ryu.repository;

import com.discussion.ryu.entity.DiscussionPostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussionPostEsRepository extends ElasticsearchRepository<DiscussionPostDocument, String> {
}
