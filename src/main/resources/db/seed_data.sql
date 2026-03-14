-- ============================================================
-- Test Data Seed Script
-- Users: 1,000 | Posts: 100,000 | Opinions: 1,000,000
-- ============================================================
-- PowerShell:
--   Get-Content src/main/resources/db/seed_data.sql | docker exec -i discussion-mysql mysql -u jewoo2000 -pjh^^030203 discussion
-- ============================================================
-- Login Info: username=user_1~user_1000 / password=Test1234!
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;
SET autocommit = 0;

-- ============================================================
-- 1. TRUNCATE (uncomment if needed)
-- ============================================================
-- TRUNCATE TABLE opinion_reactions;
-- TRUNCATE TABLE opinions;
-- TRUNCATE TABLE discussion_votes;
-- TRUNCATE TABLE discussion_posts;
-- TRUNCATE TABLE users;

-- ============================================================
-- 2. Procedures
-- ============================================================

DROP PROCEDURE IF EXISTS insert_users;
DROP PROCEDURE IF EXISTS insert_posts;
DROP PROCEDURE IF EXISTS insert_opinions;

DELIMITER $$

CREATE PROCEDURE insert_users()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 1000 DO
        INSERT INTO users (username, password, name, email, provider, provider_id, created_at, updated_at)
        VALUES (
            CONCAT('user_', i),
            '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
            CONCAT('testuser', i),
            CONCAT('user', i, '@test.com'),
            'LOCAL',
            CONCAT('local_', i),
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY),
            NOW()
        );
        SET i = i + 1;
    END WHILE;
    COMMIT;
END$$

CREATE PROCEDURE insert_posts()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE batch_size INT DEFAULT 5000;
    DECLARE total INT DEFAULT 100000;

    WHILE i <= total DO
        INSERT INTO discussion_posts
            (title, content, user_id, agree_count, disagree_count, created_at, updated_at)
        VALUES (
            CONCAT('Discussion #', i, ': Topic ', (i MOD 5) + 1),
            CONCAT('This is the content of discussion #', i, '. ',
                   REPEAT('Additional content for padding. ', 3)),
            1 + (i MOD 1000),
            FLOOR(RAND() * 500),
            FLOOR(RAND() * 500),
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 730) DAY),
            NOW()
        );

        IF i MOD batch_size = 0 THEN
            COMMIT;
        END IF;

        SET i = i + 1;
    END WHILE;
    COMMIT;
END$$

CREATE PROCEDURE insert_opinions()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE batch_size INT DEFAULT 5000;
    DECLARE total INT DEFAULT 1000000;

    WHILE i <= total DO
        INSERT INTO opinions
            (user_id, post_id, content, stance, like_count, dislike_count, created_at, updated_at)
        VALUES (
            1 + (i MOD 1000),
            1 + (i MOD 100000),
            CONCAT('Opinion #', i, ': ',
                ELT(1 + (i MOD 4),
                    'I agree with this topic for various reasons.',
                    'I disagree because there are too many practical constraints.',
                    'From a neutral perspective, both sides have valid points.',
                    'There are additional factors that should be considered.'
                )
            ),
            ELT(1 + (i MOD 3), 'AGREE', 'DISAGREE', 'NEUTRAL'),
            FLOOR(RAND() * 200),
            FLOOR(RAND() * 50),
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 730) DAY),
            NOW()
        );

        IF i MOD batch_size = 0 THEN
            COMMIT;
        END IF;

        SET i = i + 1;
    END WHILE;
    COMMIT;
END$$

DELIMITER ;

-- ============================================================
-- 3. Execute
-- ============================================================

CALL insert_users();
SELECT CONCAT('users done: ', COUNT(*)) AS status FROM users;

CALL insert_posts();
SELECT CONCAT('posts done: ', COUNT(*)) AS status FROM discussion_posts;

CALL insert_opinions();
SELECT CONCAT('opinions done: ', COUNT(*)) AS status FROM opinions;

-- ============================================================
-- 4. Cleanup
-- ============================================================
DROP PROCEDURE IF EXISTS insert_users;
DROP PROCEDURE IF EXISTS insert_posts;
DROP PROCEDURE IF EXISTS insert_opinions;

SET FOREIGN_KEY_CHECKS = 1;
SET UNIQUE_CHECKS = 1;
SET autocommit = 1;

-- ============================================================
-- 5. Final count
-- ============================================================
SELECT 'users'            AS tbl, COUNT(*) AS cnt FROM users
UNION ALL
SELECT 'discussion_posts' AS tbl, COUNT(*) AS cnt FROM discussion_posts
UNION ALL
SELECT 'opinions'         AS tbl, COUNT(*) AS cnt FROM opinions;
