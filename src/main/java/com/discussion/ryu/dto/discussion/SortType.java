package com.discussion.ryu.dto.discussion;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토론글 정렬 기준")
public enum SortType {
    
    @Schema(description = "최신순 (작성일 기준)")
    LATEST,
    
    @Schema(description = "인기순 (총 투표수 기준)")
    POPULAR,
    
    @Schema(description = "찬성 많은 순")
    MOST_AGREED,
    
    @Schema(description = "반대 많은 순")
    MOST_DISAGREED
}
