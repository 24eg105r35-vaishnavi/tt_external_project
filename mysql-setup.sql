-- 1. Create the database
CREATE DATABASE achievement_portal;

-- 2. Create the user
CREATE USER 'portal_user'@'localhost' IDENTIFIED BY 'portal_pass123';

-- 3. Give full permissions to the user for this database
GRANT ALL PRIVILEGES ON achievement_portal.* TO 'portal_user'@'localhost';

-- 4. Apply the changes
FLUSH PRIVILEGES;
