package com.discussion.ryu.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;

import java.time.LocalDateTime;

// document/DiscussionPostDocument.java
@Document(indexName = "discussion_posts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionPostDocument {

    @Id
    private String id;          // ES의 _id (DB의 Long id를 String으로 변환)

    @Field(type = FieldType.Long)
    private Long postId;        // MySQL DB의 실제 PK

    @Field(type = FieldType.Text, analyzer = "nori") // 한국어 형태소 분석기
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;

    @Field(type = FieldType.Keyword)
    private String authorName;

    @Field(type = FieldType.Long)
    private Long agreeCount;

    @Field(type = FieldType.Long)
    private Long disagreeCount;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime deletedAt;
}