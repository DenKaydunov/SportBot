# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Working with This Project

**IMPORTANT:** The project owner has granted permission to make code changes without asking for approval on each edit. Proceed with file modifications confidently once the task is clear. Only ask for clarification if requirements are ambiguous or if making risky/destructive changes (e.g., deleting files, force-pushing, dropping database tables).

## Project Overview

SportBot is a Java-based Telegram bot for tracking fitness challenges (push-ups, squats, pull-ups, abs). It migrated from SendPulse & Google Sheets to a full Spring Boot backend with PostgreSQL.

**Live Bot:** [@SquatMasterBot](https://t.me/SquatMasterBot)

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
- `UnifiedAchievementService`: New unified achievement system with 80+ achievements
  - Uses `AchievementDefinition` and `UserAchievement` tables
  - Supports multiple categories: STREAK, TOTAL_REPS, MAX_REPS, REFERRAL, WORKOUT_COUNT, SOCIAL, LEADERBOARD
  - Achievement checkers for different types (TotalRepsAchievementChecker, MaxRepsAchievementChecker, etc.)
- `AchievementAdminService`: Admin service for managing achievement localizations and metadata via REST API
- `EntityLocalizationService`: Provides localized titles for achievements (loads from `achievements` table in database, not properties files)
- `AchievementAggregationService`: Aggregates monthly achievements and sends congratulations to subscribed users (DEPRECATED - uses old system)
- `StreakService`: Tracks user workout streaks (current and best)
- `LeaderboardService`: Calculates and ranks users by exercise totals
- `UserService`: User management and profile operations
- `RankService`: Manages user ranking system based on exercise performance
- `SubscriptionService`: Handles user subscriptions to follow other users
- `NotificationService`: Sends scheduled reminders to users

**Controllers (`controller/` package)**
- REST API endpoints for all operations
- `ExerciseController`: Exercise logging endpoints
- `UserController`: User registration and management
- `LeaderboardController`: Ranking and statistics
- `AchievementController`: Achievement retrieval
- `AchievementAdminController`: Admin API for managing achievements (in `controller/admin/` package)
- `AchievementAggregationController`: Monthly achievement generation and sending
- `SubscriptionController`: Subscription management

**Models (`model/` package)**
- `User`: Central entity with `telegramId`, streaks, referral info, balance, and localization fields
- `ExerciseRecord`: Individual exercise entries
- `AchievementDefinition`: Defines available achievements (code, category, emoji, targetValue, rewardTon, sortOrder, isLegendary)
  - Categories: STREAK, TOTAL_REPS, MAX_REPS, REFERRAL, WORKOUT_COUNT, SOCIAL, LEADERBOARD
  - Links to ExerciseType for exercise-specific achievements
- `Achievement`: Stores localized achievement texts (title, description) for each language in database
  - Links to AchievementDefinition via achievement_definition_id
  - Supports multiple languages (ru, en, uk, etc.)
- `UserAchievement`: Tracks user progress and unlocked achievements
  - Links User to AchievementDefinition
  - Stores currentProgress and achievedDate
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
- `achievement_definitions`: Master list of all available achievements (metadata: code, category, emoji, target, reward, sort_order)
- `achievements`: Localized achievement texts (title, description) for each language
- `user_achievements`: User progress and unlocked achievements
- `exercise_type`: Exercise types with localization support
- `subscriptions`: User following relationships
- `ranks`: Ranking system
- `user_programs`: User-program associations

### Internationalization (i18n)

**Supported Languages:** Russian (ru), English (en), Ukrainian (uk)

**Message Sources:** Located in `src/main/resources/` as `messages_*.properties`

**Key Services:**
- `MessageLocalizer`: Resolves localized messages based on user's `language` field
- `EntityLocalizationService`: Provides localized titles and descriptions for achievements
  - **IMPORTANT:** Loads achievement texts from `achievements` table in database, NOT from properties files
  - Fallback strategy: requested language → Russian → achievement code
  - Uses AchievementRepository to fetch localized texts

**User Language:** Stored in `User.language` field, defaults to "ru" if invalid/missing

### Scheduled Tasks

**Scheduler Configuration:** `SchedulerConfig` enables `@Scheduled` annotations

**Active Schedulers:**
- `AchievementAggregationScheduler`: Monthly achievement congratulations
- Add other scheduled tasks here as they're implemented

## Important Patterns & Conventions

### Achievement System (New Unified System)

The new unified achievement system uses three main tables:
1. **achievement_definitions** - metadata (code, category, emoji, targetValue, rewardTon, sortOrder, isLegendary, exerciseTypeId)
2. **achievements** - localized texts (title, description) stored in database for each language
3. **user_achievements** - user progress and unlocked achievements

**Achievement Categories:**
- `STREAK`: Consecutive days achievements (5, 10, 20, 50, 100 days)
- `TOTAL_REPS`: Total repetitions across all time (100, 500, 1000, 5000, 10000, 50000, 100000)
- `MAX_REPS`: Maximum repetitions in single workout (20, 50, 100, 200, 500)
- `REFERRAL`: Referral count achievements (1, 3, 5, 10, 30, 100, 250)
- `WORKOUT_COUNT`: Total workout count (1, 10, 50, 100, 250, 500, 1000)
- `SOCIAL`: Social features (following, followers)
- `LEADERBOARD`: Leaderboard position achievements

**Exercise-Specific Achievements:**
Some achievements are linked to specific exercises via `exercise_type_id`:
- 1 = Push-ups
- 2 = Pull-ups
- 3 = Squats
- 4 = Abs
- NULL = General achievements (not exercise-specific)

**Achievement Localization:**
- Texts stored in `achievements` table (NOT in properties files)
- Use `EntityLocalizationService.getAchievementTitle()` and `getAchievementDescription()` to get localized texts
- Admin API available at `/admin/achievements` for managing localizations without deployment

**Achievement Checking:**
- `UnifiedAchievementService.checkAchievements()` processes achievement triggers
- Uses strategy pattern with AchievementChecker implementations (TotalRepsAchievementChecker, MaxRepsAchievementChecker, etc.)
- Each checker calculates progress for specific achievement category

### Localization

Achievement localization is stored in database (`achievements` table), not in properties files. Use `EntityLocalizationService` to get localized achievement texts:

```java
String title = entityLocalizationService.getAchievementTitle(definition, locale);
String description = entityLocalizationService.getAchievementDescription(definition, locale);
```

For other UI messages, use `MessageSource` or `MessageLocalizer`. Never hardcode messages in Russian/English/Ukrainian outside of property files or database.

### Admin API for Achievement Management

Use `/admin/achievements` endpoints to manage achievement localizations without deployment:
- `GET /admin/achievements` - list all achievements with all localizations
- `PUT /admin/achievements/{id}/localization` - add/update localization
- `PATCH /admin/achievements/{id}` - update metadata (reward, emoji, sortOrder)
- `DELETE /admin/achievements/{id}/localization/{language}` - remove localization

Available via Swagger UI at `http://localhost:8080/swagger-ui/index.html`

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
