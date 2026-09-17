# MBTI AI Assessment Platform

A standalone modern reconstruction of the legacy Servlet/JSP MBTI assessment system. The legacy project is not modified.

## Phase 1 stack

- Frontend: Vue 3 + TypeScript + Vite + Pinia + Vue Router + Element Plus + ECharts
- Backend: Java 17 + Spring Boot + Spring MVC + Validation
- Data: MySQL 8 + Redis 7 + Flyway
- API: REST + unified response envelope + ProblemDetail + OpenAPI
- Delivery: Docker Compose + Nginx

## One-command startup

Prerequisite: Docker Desktop is running.

    docker compose up --build

Open http://localhost:5173, API health at http://localhost:8080/api/v1/health, Swagger at http://localhost:8080/swagger-ui.html.

## Local development

    cd backend && mvnw.cmd spring-boot:run
    cd frontend && npm install && npm run dev

The frontend proxies API requests to http://localhost:8080.

## Migration

Flyway migrations are under backend/src/main/resources/db/migration. V1 contains the legacy schema and V2 contains legacy seed data migrated from the original sql files.

The legacy admin seed is retained only for data compatibility. Authentication will replace plaintext passwords with BCrypt and RBAC in the next phase.

## Next phase

Spring Security/JWT, secure test attempts, assessment APIs, reports, administration, Spring AI streaming, and RAG.
