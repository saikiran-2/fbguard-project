-- Demo seed data for FBGuard.
-- Runs automatically on startup (see application.yml: spring.sql.init.mode=always
-- and spring.jpa.defer-datasource-initialization=true, which makes this run
-- AFTER Hibernate creates the tables from the @Entity classes).
--
-- Safe to re-run: every INSERT is guarded so it won't duplicate rows on restart.
-- The real admin account is seeded separately by DataSeeder.java from env vars
-- (ADMIN_USERNAME/ADMIN_PASSWORD) - it is NOT created here.

-- Demo users: alice / Demo1234!  and  bob / Demo1234!
INSERT INTO users (uid, username, password, email, gender, country, phoneno, role, created_at)
SELECT 1001, 'alice', '$2a$10$GXN/bMv7.asloWXD1j0P1O8yC6V6GAty5nrrr1Z5BGB4seD.qUZNC',
       'alice@example.com', 'Female', 'India', '9000000001', 'USER', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'alice');

INSERT INTO users (uid, username, password, email, gender, country, phoneno, role, created_at)
SELECT 1002, 'bob', '$2a$10$GXN/bMv7.asloWXD1j0P1O8yC6V6GAty5nrrr1Z5BGB4seD.qUZNC',
       'bob@example.com', 'Male', 'India', '9000000002', 'USER', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'bob');

-- Blacklist entries the admin has already flagged
INSERT INTO blacklist_entries (mid, malicious, added_at, added_by)
SELECT 2001, 'free-facebook-coins.tk', NOW(), 'admin'
WHERE NOT EXISTS (SELECT 1 FROM blacklist_entries WHERE malicious = 'free-facebook-coins.tk');

INSERT INTO blacklist_entries (mid, malicious, added_at, added_by)
SELECT 2002, 'fb-secure-login-verify.xyz', NOW(), 'admin'
WHERE NOT EXISTS (SELECT 1 FROM blacklist_entries WHERE malicious = 'fb-secure-login-verify.xyz');

-- App submissions covering all three statuses, so the gallery/admin panel/charts
-- all have something to show on first load.

-- LOW risk, auto-licensed
INSERT INTO app_submissions
  (aid, appname, appid, appurl, submitted_by, status, risk_score, risk_level, risk_signals, created_at, decided_at)
SELECT 3001, 'Word Puzzle Daily', 'wpd-2024', 'https://apps.facebook.com/wordpuzzledaily',
       1001, 'LICENSED', 5, 'LOW',
       'Local Blacklist: Not present on blacklist | URL Heuristics: No suspicious URL patterns found | Google Safe Browsing: Safe Browsing not configured (no API key) | Domain Age (RDAP): Domain is 1200 days old |',
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_submissions WHERE aid = 3001);

-- MEDIUM risk, left pending for a human admin to review
INSERT INTO app_submissions
  (aid, appname, appid, appurl, submitted_by, status, risk_score, risk_level, risk_signals, created_at)
SELECT 3002, 'Quiz Master Pro', 'qmp-9', 'http://quiz-master-pro-app.xyz/play',
       1002, 'PENDING', 42, 'MEDIUM',
       'Local Blacklist: Not present on blacklist | URL Heuristics: uses a commonly-abused free TLD; not served over HTTPS; not hosted on an official facebook.com domain; | Google Safe Browsing: Safe Browsing not configured (no API key) | Domain Age (RDAP): Domain registered 45 days ago |',
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_submissions WHERE aid = 3002);

-- HIGH risk, auto-rejected (matches a blacklist entry above)
INSERT INTO app_submissions
  (aid, appname, appid, appurl, submitted_by, status, risk_score, risk_level, risk_signals, created_at, decided_at)
SELECT 3003, 'Free Coins Generator', 'fcg-1', 'http://free-facebook-coins.tk/claim',
       1001, 'REJECTED', 87, 'HIGH',
       'Local Blacklist: URL matches an entry on the admin blacklist | URL Heuristics: uses a commonly-abused free TLD; not served over HTTPS; not hosted on an official facebook.com domain; | Google Safe Browsing: Safe Browsing not configured (no API key) | Domain Age (RDAP): Domain registered 6 days ago |',
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_submissions WHERE aid = 3003);

-- An accepted friend connection so the Friends/Messages pages aren't empty
INSERT INTO friend_requests (id, from_user_id, to_user_id, status, created_at)
SELECT 4001, 1001, 1002, 'ACCEPTED', NOW()
WHERE NOT EXISTS (SELECT 1 FROM friend_requests WHERE from_user_id = 1001 AND to_user_id = 1002);

-- A couple of demo messages between them
INSERT INTO messages (mid, sender_id, receiver_id, msg, sent_at)
SELECT 5001, 1001, 1002, 'Hey! Did you see the app I just submitted?', NOW()
WHERE NOT EXISTS (SELECT 1 FROM messages WHERE mid = 5001);

INSERT INTO messages (mid, sender_id, receiver_id, msg, sent_at)
SELECT 5002, 1002, 1001, 'Yeah, looks safe - nice score on that one.', NOW()
WHERE NOT EXISTS (SELECT 1 FROM messages WHERE mid = 5002);
