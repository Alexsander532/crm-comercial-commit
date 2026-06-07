-- ============================================================
-- V7__fix_admin_password_hash.sql
-- Corrige o hash BCrypt dos usuários seed para 'admin123'
-- ============================================================

UPDATE users
SET password_hash = '$2a$10$3ami2.k/35etGTCDudQwx.sWLUXYl1r0b221HmrIBG1LOlU52NJAe'
WHERE password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqBtF6zOqTdpA6wYJp7zXxL1O0k2a';
