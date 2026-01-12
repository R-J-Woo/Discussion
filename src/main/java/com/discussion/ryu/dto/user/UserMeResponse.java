package com.discussion.ryu.dto.user;

import java.time.ZonedDateTime;

public record UserMeResponse (
    String username,
    String name,
    String email,
    String grade,
    ZonedDateTime created_at,
    ZonedDateTime updated_at,
    ZonedDateTime deleted_at
) {}
