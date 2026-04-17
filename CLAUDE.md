# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SportBot is a Java-based Telegram bot for tracking fitness challenges (push-ups, squats, pull-ups, abs). It migrated from SendPulse & Google Sheets to a full Spring Boot backend with PostgreSQL.

**Live Bot:** [@PushupsWardengzBot](https://t.me/PushupsWardengzBot)

## Build & Run Commands

### Local Development

```bash
# Build project
mvn clean install

# Run with local profile (uses Docker PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Start PostgreSQL via Docker
docker compose -f docker/docker-compose.yaml up -d

# Stop PostgreSQL
docker compose -f docker/docker-compose.yaml down
```

### Testing

```bash
# Run all unit tests
mvn test

# Run specific unit test
mvn test -Dtest=ExerciseServiceTest

# Run tests for a specific class pattern
mvn test -Dtest=*ServiceTest

# Integration tests are located in src/test-integration/
# They are NOT configured in pom.xml and must be run via IDE
# Integration test naming: *IntegrationTest.java
```

### Deployment (Google Cloud Platform)

```bash
# Deploy to GCP App Engine
gcloud app deploy

# View logs
gcloud app logs tail -s default

# Open app in browser
gcloud app browse
```

### Local Development with ngrok (for SendPulse chains)

```bash
# Expose local app to internet
ngrok http http://localhost:8080
```

### Other Useful Commands

```bash
# Access Swagger UI
# http://localhost:8080/swagger-ui/index.html

# Create/update Maven wrapper
mvn wrapper:wrapper
```

## Architecture

### Core Components

**Telegram Bot (`bot/` package)**
- `SportBot`: Main bot class extending `TelegramLongPollingBot`, handles message routing
- `CallbackRouter`: Routes callback queries from inline keyboards to appropriate handlers
- `InlineKeyboardFactory`: Creates pagination and subscription menus
- `SubscriptionHandler`: Handles user subscription/unsubscription logic
- `PaginationHandler`: Manages paginated list navigation

**Service Layer (`service/` package)**
- `ExerciseService`: Core business logic for exercise recording and retrieval
- `AchievementService`: Manages two types of achievements:
  - **Streak achievements** (via `milestone` field in `Achievement`)
  - **Referral achievements** (via `referralMilestone` field in `Achievement`)
  - CRITICAL: When querying achievements, always filter by the relevant milestone type (e.g., `.filter(a -> a.getMilestone() != null)` for streak achievements)
- `AchievementAggregationService`: Aggregates monthly achievements and sends congratulations to subscribed users
- `StreakService`: Tracks user workout streaks (current and best)
- `LeaderboardService`: Calculates and ranks users by exercise totals
- `UserService`: User management and profile operations
- `RankService`: Manages user ranking system based on exercise performance
- `SubscriptionService`: Handles user subscriptions to follow other users
- `NotificationService`: Sends scheduled reminders to users
- `EntityLocalizationService`: Provides localized titles for milestones

**Controllers (`controller/` package)**
- REST API endpoints for all operations
- `ExerciseController`: Exercise logging endpoints
- `UserController`: User registration and management
- `LeaderboardController`: Ranking and statistics
- `AchievementController`: Achievement retrieval
- `AchievementAggregationController`: Monthly achievement generation and sending
- `SubscriptionController`: Subscription management

**Models (`model/` package)**
- `User`: Central entity with `telegramId`, streaks, referral info, balance, and localization fields
- `ExerciseRecord`: Individual exercise entries
- `Achievement`: Can represent either streak OR referral achievements (never both)
  - Has nullable `milestone` (for streak achievements)
  - Has nullable `referralMilestone` (for referral achievements)
- `StreakMilestone`: Defines streak-based achievement thresholds
- `ReferralMilestone`: Defines referral-based achievement thresholds
- `ExerciseType`: Types of exercises (push-ups, squats, pull-ups, abs)
- `UserProgram`: Links users to exercise programs
- `Rank`: User ranking system
- `Subscription`: User-to-user following relationships

### Database

**Technology:** PostgreSQL (local Docker, GCP Cloud SQL in production)

**Schema Management:** Liquibase (`src/main/resources/db/changelog/`)
- Master changelog: `db.changelog-master.yaml`
- Migration files: `changes/0001-*.sql`, `changes/0002-*.sql`, etc.
- NEVER manually modify the database; always create new Liquibase changesets

**Key Tables:**
- `users`: User profiles with streak tracking and localization
- `exercise_records`: Exercise logs
- `achievements`: Polymorphic table for streak and referral achievements
- `streak_milestones`: Streak achievement definitions
- `referral_milestones`: Referral achievement definitions
- `subscriptions`: User following relationships
- `ranks`: Ranking system
- `user_programs`: User-program associations

### Internationalization (i18n)

**Supported Languages:** Russian (ru), English (en), Ukrainian (uk)

**Message Sources:** Located in `src/main/resources/` as `messages_*.properties`

**Key Services:**
- `MessageLocalizer`: Resolves localized messages based on user's `language` field
- `EntityLocalizationService`: Provides localized titles for milestones and achievements

**User Language:** Stored in `User.language` field, defaults to "ru" if invalid/missing

### Scheduled Tasks

**Scheduler Configuration:** `SchedulerConfig` enables `@Scheduled` annotations

**Active Schedulers:**
- `AchievementAggregationScheduler`: Monthly achievement congratulations
- Add other scheduled tasks here as they're implemented

## Important Patterns & Conventions

### Achievement System

The `Achievement` entity supports two mutually exclusive types:
1. **Streak achievements**: `milestone` is set, `referralMilestone` is null
2. **Referral achievements**: `referralMilestone` is set, `milestone` is null

**CRITICAL:** When processing achievements, always filter by the relevant type to avoid NullPointerExceptions:
```java
// For streak achievements
achievementStream.filter(a -> a.getMilestone() != null)
                 .map(a -> a.getMilestone().getId())

// For referral achievements
achievementStream.filter(a -> a.getReferralMilestone() != null)
                 .map(a -> a.getReferralMilestone().getId())
```

### Localization

Always use `MessageSource` or helper services (`MessageLocalizer`, `EntityLocalizationService`) for user-facing text. Never hardcode messages in Russian/English/Ukrainian outside of property files.

### Repository Queries

Custom queries use Spring Data JPA projections (e.g., `CompetitorProjection`, `ExercisePeriodProjection`) for efficient data retrieval.

### TON Balance

Users have a `balanceTon` field that gets incremented when they achieve milestones. Ensure balance updates are wrapped in transactions.

## Configuration

**Profiles:**
- `local`: Local development (uses Docker PostgreSQL)
- Default: Production (uses GCP Cloud SQL)

**Key Properties (`application.yaml`):**
- `bot.token`: Telegram bot token
- `spring.cloud.gcp.sql.*`: GCP Cloud SQL connection
- `spring.datasource.*`: PostgreSQL credentials
- `spring.liquibase.*`: Database migration settings

**Environment Variables:** Verify environment variables in `application.yaml` before deploying to GCP.

## Tech Stack

- Java 21 (pom.xml specifies 21, README mentions 23)
- Spring Boot 3.3.4
- PostgreSQL
- Liquibase (schema migrations)
- Telegram Bots Spring Boot Starter 6.9.7.1
- MapStruct (DTO mapping)
- Lombok (boilerplate reduction)
- Swagger/OpenAPI (API documentation)
- Docker (local PostgreSQL)
- Google Cloud Platform (production deployment)

## Testing

### Unit Tests

- **Location:** `src/test/java/com/github/sportbot/`
- **Database:** H2 in-memory database
- **Naming:** `*Test.java` (e.g., `ExerciseServiceTest.java`)
- **Execution:** Run via `mvn test` command

**Test Coverage:**
- Service layer tests (business logic)
- Controller tests (REST endpoints with `@WebMvcTest`)
- Repository tests (database queries)
- Bot component tests (Telegram bot handlers)
- Achievement checker tests (streak and referral achievements)

### Integration Tests

- **Location:** `src/test-integration/java/com/github/sportbot/`
- **Naming:** `*IntegrationTest.java` (e.g., `LeaderboardControllerIntegrationTest.java`)
- **Configuration:** Configured via `build-helper-maven-plugin` in `pom.xml`
- **Execution:** Must be run via IDE (IntelliJ IDEA), not Maven command line
- **Purpose:** End-to-end testing of REST controllers with mocked services

**IMPORTANT:** Integration tests use `@WebMvcTest` and mock service layers. They test controller behavior, request mapping, and response formatting without hitting the real database.

**Test Files:**
- `LeaderboardControllerIntegrationTest.java`: Leaderboard API endpoints
- `ExerciseControllerIntegrationTest.java`: Exercise logging endpoints
- `UserControllerIntegrationTest.java`: User management endpoints
- `AchievementControllerIntegrationTest.java`: Achievement retrieval
- `SubscriptionControllerIntegrationTest.java`: User subscriptions
- Other controller integration tests in same directory

**Troubleshooting:**

If IntelliJ IDEA shows "Class not found" errors for integration tests:
1. Verify `build-helper-maven-plugin` is configured in `pom.xml` (should already be present)
2. Reload Maven project: **Maven → Reload Project** (Cmd+Shift+I on Mac / Ctrl+Shift+O on Windows)
3. IntelliJ will automatically mark `src/test-integration/java` as Test Sources Root
4. If still not working, manually mark directory: Right-click `src/test-integration/java` → Mark Directory as → Test Sources Root

### Running Tests

```bash
# Run all unit tests
mvn test

# Run specific unit test
mvn test -Dtest=ExerciseServiceTest

# Run unit tests by pattern
mvn test -Dtest=*ServiceTest

# Integration tests: Run via IDE (right-click on test class/directory)
# They are NOT executed by Maven surefire plugin during `mvn test`
```
