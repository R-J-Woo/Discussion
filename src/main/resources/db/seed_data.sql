-- ============================================================
-- Test Data Seed Script (No Procedure Version)
-- Users: 1,000 | Posts: 100,000 | Opinions: 1,000,000
-- Login Info: username=user_1~user_1000 / password=Test1234!
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET SESSION cte_max_recursion_depth = 1000000;

-- ============================================================
-- 1. TRUNCATE
-- ============================================================
TRUNCATE TABLE opinion_reactions;
TRUNCATE TABLE opinions;
TRUNCATE TABLE discussion_votes;
TRUNCATE TABLE discussion_posts;
TRUNCATE TABLE users;

-- ============================================================
-- 2. Insert users (1,000)
-- ============================================================
INSERT INTO users (username, password, name, email, provider, provider_id, created_at, updated_at)
WITH RECURSIVE seq AS (
    SELECT 1 AS i
    UNION ALL
    SELECT i + 1 FROM seq WHERE i < 1000
)
SELECT
    CONCAT('user_', i),
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    CONCAT('testuser', i),
    CONCAT('user', i, '@test.com'),
    'LOCAL',
    CONCAT('local_', i),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY),
    NOW()
FROM seq;

SELECT CONCAT('users done: ', COUNT(*)) AS status FROM users;

-- ============================================================
-- 3. Insert discussion_posts (100,000)
-- ============================================================
INSERT INTO discussion_posts (title, content, user_id, agree_count, disagree_count, created_at, updated_at)
WITH RECURSIVE seq AS (
    SELECT 1 AS i
    UNION ALL
    SELECT i + 1 FROM seq WHERE i < 100000
)
SELECT
    CONCAT('Discussion #', i, ': Topic ', (i MOD 5) + 1),
    CONCAT('This is the content of discussion #', i, '. Additional content for padding. Additional content for padding. Additional content for padding.'),
    1 + (i MOD 1000),
    FLOOR(RAND() * 500),
    FLOOR(RAND() * 500),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 730) DAY),
    NOW()
FROM seq;

SELECT CONCAT('posts done: ', COUNT(*)) AS status FROM discussion_posts;

-- ============================================================
-- 4. Insert opinions (1,000,000) - 25만씩 4번
-- ============================================================
INSERT INTO opinions (user_id, post_id, content, stance, like_count, dislike_count, created_at, updated_at)
WITH RECURSIVE seq AS (
    SELECT 1 AS i
    UNION ALL
    SELECT i + 1 FROM seq WHERE i < 250000
)
SELECT
    1 + (i MOD 1000),
    1 + (i MOD 100000),
    CONCAT('Opinion #', i, ': ', ELT(1 + (i MOD 4),
        'I agree with this topic for various reasons.',
        'I disagree because there are too many practical constraints.',
        'From a neutral perspective, both sides have valid points.',
        'There are additional factors that should be considered.'
    )),
    ELT(1 + (i MOD 3), 'AGREE', 'DISAGREE', 'NEUTRAL'),
    FLOOR(RAND() * 200),
    FLOOR(RAND() * 50),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 730) DAY),
    NOW()
FROM seq;

SELECT CONCAT('opinions 1/4 done: ', COUNT(*)) AS status FROM opinions;

INSERT INTO opinions (user_id, post_id, content, stance, like_count, dislike_count, created_at, updated_at)
WITH RECURSIVE seq AS (
    SELECT 250001 AS i
    UNION ALL
    SELECT i + 1 FROM seq WHERE i < 500000
)
SELECT
    1 + (i MOD 1000),
    1 + (i MOD 100000),
    CONCAT('Opinion #', i, ': ', ELT(1 + (i MOD 4),
        'I agree with this topic for various reasons.',
        'I disagree because there are too many practical constraints.',
        'From a neutral perspective, both sides have valid points.',
        'There are additional factors that should be considered.'
    )),
    ELT(1 + (i MOD 3), 'AGREE', 'DISAGREE', 'NEUTRAL'),
    FLOOR(RAND() * 200),
    FLOOR(RAND() * 50),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 730) DAY),
    NOW()
FROM seq;

SELECT CONCAT('opinions 2/4 done: ', COUNT(*)) AS status FROM opinions;

INSERT INTO opinions (user_id, post_id, content, stance, like_count, dislike_count, created_at, updated_at)
WITH RECURSIVE seq AS (
    SELECT 500001 AS i
    UNION ALL
    SELECT i + 1 FROM seq WHERE i < 750000
)
SELECT
    1 + (i MOD 1000),
    1 + (i MOD 100000),
    CONCAT('Opinion #', i, ': ', ELT(1 + (i MOD 4),
        'I agree with this topic for various reasons.',
        'I disagree because there are too many practical constraints.',
        'From a neutral perspective, both sides have valid points.',
        'There are additional factors that should be considered.'
    )),
    ELT(1 + (i MOD 3), 'AGREE', 'DISAGREE', 'NEUTRAL'),
    FLOOR(RAND() * 200),
    FLOOR(RAND() * 50),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 730) DAY),
    NOW()
FROM seq;

SELECT CONCAT('opinions 3/4 done: ', COUNT(*)) AS status FROM opinions;

INSERT INTO opinions (user_id, post_id, content, stance, like_count, dislike_count, created_at, updated_at)
WITH RECURSIVE seq AS (
    SELECT 750001 AS i
    UNION ALL
    SELECT i + 1 FROM seq WHERE i < 1000000
)
SELECT
    1 + (i MOD 1000),
    1 + (i MOD 100000),
    CONCAT('Opinion #', i, ': ', ELT(1 + (i MOD 4),
        'I agree with this topic for various reasons.',
        'I disagree because there are too many practical constraints.',
        'From a neutral perspective, both sides have valid points.',
        'There are additional factors that should be considered.'
    )),
    ELT(1 + (i MOD 3), 'AGREE', 'DISAGREE', 'NEUTRAL'),
    FLOOR(RAND() * 200),
    FLOOR(RAND() * 50),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 730) DAY),
    NOW()
FROM seq;

-- ============================================================
-- 5. 환경 복구 및 최종 확인
-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;

SELECT 'users'            AS tbl, COUNT(*) AS cnt FROM users
UNION ALL
SELECT 'discussion_posts' AS tbl, COUNT(*) AS cnt FROM discussion_posts
UNION ALL
SELECT 'opinions'         AS tbl, COUNT(*) AS cnt FROM opinions;
