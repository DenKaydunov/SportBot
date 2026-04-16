# SportBot Bruno API Collection

This Bruno collection provides a comprehensive set of HTTP requests for manual testing of the SportBot REST API.

## Setup

1. Install [Bruno](https://www.usebruno.com/)
2. Open the collection: `File > Open Collection` and select the `bruno/SB` folder
3. Select an environment:
   - **local**: For local development (`http://localhost:8080`)
   - **cloud**: For production GCP deployment

## Collection Structure

The collection is organized into the following folders:

### User
- Register User
- Register User with Referrer
- Get User Locale
- Update User Locale
- Unsubscribe User

### Exercise
- Log Exercise
- Log Max Exercise
- Get Today Progress
- Get Exercise Progress
- Get Calendar

### Achievement
- Get User Achievements
- Get Monthly Congratulation
- Send Monthly Congratulation

### Leaderboard
- Get Leaderboard by Period
- Get Leaderboard by Period Paged
- Get Leaderboard by Dates
- Get Rating

### Subscription
- Subscribe
- Unsubscribe
- Get Following
- Get Followers
- Compare Users
- Get All Users Paged
- Send Subscription Menu
- Send Unsubscription Menu

### Profile
- Get Profile
- Update Profile

### Streak
- Get All Streaks
- Get Current Streak
- Get Best Streak
- Save Streak

### Deposit
- Create Deposit
- Get Deposits

### Competitors
- Get Competitors

### Motivation
- Get Motivation

### UserProgram
- Get User Programs
- Update User Program

## Environment Variables

Both environments define:
- `url`: Base URL for the API
- `testTelegramId`: Primary test user ID (676150892)
- `testTelegramId2`: Secondary test user ID (123456789)

## Exercise Types

Valid values for `exerciseType`:
- `push_up`
- `squat`
- `pull_up`
- `abs`

## Period Types

Valid values for `period`:
- `DAY`
- `WEEK`
- `MONTH`
- `ALL_TIME`

## Languages

Valid values for `language`:
- `ru` (Russian)
- `en` (English)
- `uk` (Ukrainian)

## Testing Workflow

1. **Register a user** (User folder)
2. **Log exercises** (Exercise folder)
3. **Check achievements** (Achievement folder)
4. **View leaderboards** (Leaderboard folder)
5. **Test subscriptions** (Subscription folder)

## Notes

- Most requests use the `{{testTelegramId}}` variable for convenience
- Update the environment variables if testing with different user IDs
- For production testing, use the `cloud` environment
- For local testing with ngrok, update the `local` environment URL
