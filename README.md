# Student Achievement Portal

A full-stack web application built with Spring Boot 3, Java 17, Thymeleaf, Spring Security, and MySQL. It enables students to upload proof of their achievements, allows faculty to verify them through department-routed queues, awards verified points, generates shareable public portfolios with SEO tags, displays college-wide leaderboards and analytics, and provides an administrative management console.

---

## 🛠️ Technology Stack

- **Backend:** Java 17, Spring Boot 3.3.4
- **Security:** Spring Security 6 (BCrypt, Role-Based Access Control, 5-Attempt Lockout, Security Headers)
- **Database:** MySQL 8.0, Spring Data JPA / Hibernate
- **Frontend / Templating:** Thymeleaf, Vanilla CSS, Semantic HTML5
- **Build & Containerization:** Maven 3.9, Docker, Docker Compose

---

## 🚀 Key Features

1. **Role-Based Authentication (3 Roles):**
   - **Student:** Upload achievements, resubmit rejected entries, manage privacy settings, view public portfolio.
   - **Faculty:** Review assigned pending verification queues, approve, reject, or request changes with notes.
   - **Admin:** Manage dynamic categories, users, verifier department mappings, and view full audit logs.

2. **Proof Document Upload & Verification:**
   - Multi-category support (Certificates, Hackathons, Paper Publications, Sports, Internships, Competitions) with dynamic JSON extra fields.
   - Strict file validation: maximum 10MB, extension check, and header magic-byte verification (%PDF, PNG `89 50 4E 47`, JPEG `FF D8 FF`) to prevent spoofed files.
   - Duplicate prevention using SHA-256 file checksums.
   - Uploaded files are stored outside static web roots and served only via authenticated endpoints.

3. **Gamification & Points Engine:**
   - Automatic point calculation based on category base points and participation level (College, State, National, International).
   - In-app notification badge and notification center alerting students when decisions are recorded.

4. **Public Portfolio & SEO (/u/{slug}):**
   - Unique vanity URL slugs for every student.
   - 3 Visibility modes: `PRIVATE` (owner only), `COLLEGE_ONLY` (logged-in users only), and `PUBLIC` (open to the web).
   - Full SEO optimization: Semantic HTML (`<h1>`, `<h2>`, `<article>`, `<time>`), unique titles, meta descriptions, OpenGraph tags, canonical URLs, and Schema.org `Person` JSON-LD structured data.
   - Dynamic `/sitemap.xml` listing only public students with approved records and `/robots.txt` disallowing private sections.

5. **Analytics & Leaderboard:**
   - **Analytics (/analytics):** For Faculty and Admin with summary cards, date range filtering, and inline CSS percentage bar charts (no external JavaScript libraries needed).
   - **Leaderboard (/leaderboard):** Top 10 students ranked by verified points with branch and year filters. Respects privacy by excluding private profiles and hiding sensitive contact info.

6. **College Search & CSV Export:**
   - Filter by keyword, category, branch, year, level, and event date range with pagination.
   - Safe CSV export with UTF-8 BOM for Microsoft Excel compatibility and sanitization against spreadsheet formula injection (`=`, `+`, `-`, `@`).

7. **Security Polish & Error Pages:**
   - Account lockout: 5 consecutive failed login attempts locks the account for 15 minutes, logged under `ACCOUNT_LOCKED` in `AuditLog`.
   - Security headers: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy`.
   - Session cookies configured with `HttpOnly` and `SameSite=Lax`.
   - Custom friendly error pages (`404.html`, `403.html`, `500.html`) and `@ControllerAdvice` suppressing technical stack traces.

---

## 🔑 Pre-seeded Test Accounts

All accounts are pre-seeded on startup. You can customize the default admin and faculty passwords via environment variables.

| Role | Email | Default Password | Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@anurag.edu.in` | `admin123` | Full access to Admin Console, Analytics, and Audit Logs |
| **Faculty 1** | `faculty@anurag.edu.in` | `faculty123` | Computer Science & Engineering (verifies ALL & CSE) |
| **Faculty 2** | `sports_faculty@anurag.edu.in` | `faculty123` | Physical Education (verifies Sports category) |
| **Student** | `student@anurag.edu.in` | `student123` | Roll No: `21EG105A01`, CSE, Year 3 |

> **Security Note:** Change default seeded passwords upon first login.

---

## 💻 How to Run Locally

### Prerequisites
- Java 17 (JDK)
- Maven 3.8+
- MySQL 8.0 running on `localhost:3306`

### Steps
1. Create the MySQL database and user:
   ```sql
   CREATE DATABASE IF NOT EXISTS achievement_portal;
   CREATE USER IF NOT EXISTS 'portal_user'@'localhost' IDENTIFIED BY 'portal_pass123';
   GRANT ALL PRIVILEGES ON achievement_portal.* TO 'portal_user'@'localhost';
   FLUSH PRIVILEGES;
   ```
2. Navigate to the backend directory:
   ```bash
   cd backend
   ```
3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```
4. Access the application in your browser at:
   ```
   http://localhost:8080
   ```

---

## 🐳 How to Run with Docker Compose

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```
2. Build and start both MySQL and Spring Boot containers:
   ```bash
   docker compose up --build
   ```
3. Watch application logs:
   ```bash
   docker compose logs -f app
   ```
4. Stop containers while preserving database and file upload volumes:
   ```bash
   docker compose down
   ```
5. Wipe all volumes and start fresh:
   ```bash
   docker compose down -v
   ```
6. Backup the database using `mysqldump` through Docker:
   ```bash
   docker exec -t achievement_portal_db mysqldump -u portal_user -pportal_pass123 achievement_portal > backup.sql
   ```

---

## 📁 Project Folder Structure

```
student_achievement_portal/
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/portal/
│       │   │   ├── config/          # SecurityConfig, DataSeeder
│       │   │   ├── controller/      # Auth, Student, Faculty, Admin, Analytics, Leaderboard, SEO, Profile
│       │   │   ├── dto/             # Form DTOs, LeaderboardRowDto, ChartItemDto, StoredFileInfo
│       │   │   ├── entity/          # User, Student, Faculty, Role, Category, Achievement, Document, etc.
│       │   │   ├── repository/      # Spring Data JPA repositories with custom queries
│       │   │   ├── security/        # CustomUserDetailsService, LoginLockoutListener
│       │   │   ├── service/         # Business logic, file storage, verification, analytics
│       │   │   └── util/            # Custom exceptions and CSV sanitizers
│       │   └── resources/
│       │       ├── application.properties
│       │       ├── static/          # CSS, JavaScript, and images
│       │       └── templates/       # Thymeleaf views (auth, student, faculty, admin, analytics, error)
│       └── test/                    # Integration and unit tests
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```
