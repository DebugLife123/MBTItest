# Phase 1 Architecture

We use a modular monolith rather than premature microservices. Immediate value comes from security, test-attempt integrity, data migration, observability, and AI boundaries.

Modules planned: common, auth, user, assessment, attempt, result, admin, ai, infrastructure.

API conventions: /api/v1; success responses use ApiResponse<T>; failures use RFC ProblemDetail; timestamps are ISO-8601; pagination uses page/size/sort.

Docker Compose orchestrates MySQL, Redis, Spring Boot, and the Vue/Nginx frontend. Flyway owns schema lifecycle.
