package com.discussion.ryu.dto.discussion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter
@Builder
public class DiscussionSearchDto {

    @Schema(description = "검색 키워드 (제목 또는 내용)", example = "환경")
    private String keyword;

    @Schema(description = "작성자 닉네임", example = "홍길동")
    private String authorName;

    @Schema(description = "검색 타입 (ALL: 전체, TITLE: 제목, CONTENT: 내용)", example = "ALL", defaultValue = "ALL")
    private SearchType searchType;

    public enum SearchType {
        ALL,        // 제목 + 내용
        TITLE,      // 제목만
        CONTENT     // 내용만
    }

    // 기본값 설정
    public SearchType getSearchType() {
        return searchType != null ? searchType : SearchType.ALL;
    }
}
