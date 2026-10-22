# Student Achievement Portal

A full-stack web application designed for educational institutions (e.g., National Institute of Technology) built with a decoupled architecture:
- **Backend:** Spring Boot 3.3.4, Java 17, Spring Security + JWT, Spring Data JPA, and MySQL 8. Pure REST API returning JSON under `/api`.
- **Frontend:** React 18, Vite, React Router v6, Axios, and plain CSS tokens with a subtle, calm theme (Navy, Warm Gold, Soft Slate, `#f7f8fb` background).

Students upload verifiable proof of achievements, faculty verify them through department-routed queues, points are automatically computed, public shareable portfolios are dynamically generated, and college administrators monitor activities through analytics, search, CSV export, and category management.

---

## 🛠️ Technology Stack

- **Backend:** Java 17, Spring Boot 3.3.4
- **Security:** Spring Security 6, JWT Authentication (`Bearer <token>`), BCrypt, Role-Based Access Control, Account Lockout, Security Headers
- **Database:** MySQL 8.0, Spring Data JPA / Hibernate
- **Frontend:** React 18, Vite, React Router v6, Axios, Vanilla CSS Design System (`tokens.css`)
- **Containerization:** Docker, Docker Compose

---

## 🚀 Key Features & Completed Phases

1. **Authentication & Role-Based Access (Phases 1-3):**
   - JWT stateless authentication with auto-refresh / localStorage session handling.
   - 3 distinct roles: **Student**, **Faculty**, and **Admin**.
   - Account lockout after 5 consecutive failed login attempts (15-minute freeze).

2. **Achievement Upload & Secure Document Management (Phase 4):**
   - Dynamic schema-driven forms based on category configuration (Hackathons, Research Papers, Sports, Internships, etc.).
   - Multi-check file validation: 10MB maximum, extension filtering, and header magic-byte verification (PDF, PNG, JPEG).
   - In-app `DocumentViewer` streaming documents securely through authorized endpoints (`/api/documents/{id}`) with blob URL previews.

3. **Faculty Verification Queue & Student Dashboard (Phase 5):**
   - Department-routed verification queues for faculty members.
   - Two-column verification review interface: student details and claims on the left, full document preview on the right, and decision actions (Approve, Reject, Send Back with note).
   - Real-time student dashboard tracking verified points, pending submissions, approved count, and action-required items.

4. **Public Portfolio & Notification System (Phase 6):**
   - Shareable student portfolios with customizable privacy controls (`PUBLIC`, `COLLEGE_ONLY`, `PRIVATE`).
   - Clean vanity URLs (`/u/{slug}`) with certificate-style presentation, initials avatar, and achievement breakdown.
   - In-app notification center with real-time unread badges in the navigation bar.

5. **Advanced Search, CSV Export & Admin Console (Phase 7):**
   - Multi-criteria paged search across achievements, branches, categories, and date ranges.
   - Secure CSV export with UTF-8 BOM and formula injection protection (`=`, `+`, `-`, `@`).
   - Comprehensive admin console for managing categories (with dynamic JSON schemas), user accounts, and immutable audit logs.

6. **Analytics, Leaderboard & SEO (Phase 8):**
   - Interactive analytics dashboard with distribution charts across categories, branches, and levels.
   - College-wide leaderboard highlighting top student achievers with medal badges (#1 Gold, #2 Silver, #3 Bronze).
   - Automated SEO endpoints (`/robots.txt` and `/sitemap.xml`) indexing verified public student profiles.

---

## 🔑 Pre-seeded Test Accounts

All accounts are automatically seeded on startup:

| Role | Email | Password | Details |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@college.edu` | `admin123` | Full access to Admin Console, Analytics & Audit Logs |
| **Faculty (CSE)** | `faculty@college.edu` | `faculty123` | Department: Computer Science & Engineering |
| **Student** | `student@college.edu` | `student123` | Roll No: `21EG105A99`, Branch: CSE, Year: 3 |

*(Legacy accounts with `@anurag.edu.in` domain are also supported for backward compatibility)*.

---

## 💻 How to Run Locally

### Prerequisites
- Java 17 (JDK)
- Node.js 18+ and npm
- Maven 3.8+
- MySQL 8.0 running on `localhost:3306`

### 1. Database Setup
```sql
CREATE DATABASE IF NOT EXISTS achievement_portal;
CREATE USER IF NOT EXISTS 'portal_user'@'localhost' IDENTIFIED BY 'portal_pass123';
GRANT ALL PRIVILEGES ON achievement_portal.* TO 'portal_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Start the Backend (Spring Boot REST API)
```bash
cd backend
mvn spring-boot:run
```
*The REST API will start on `http://localhost:8080`.*

### 3. Start the Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```
*The web application will open on `http://localhost:5173`.*

---

## 🐳 Running with Docker Compose

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Build and run all services:
   ```bash
   docker compose up --build
   ```
3. Stop services:
   ```bash
   docker compose down
   ```
