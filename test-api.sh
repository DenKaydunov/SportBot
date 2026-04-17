#!/bin/bash

# API Testing Script for SportBot
BASE_URL="http://localhost:8080"
TEST_TELEGRAM_ID=676150892
TEST_TELEGRAM_ID2=123456789

# Generate random IDs for registration tests to avoid conflicts
RANDOM_ID1=$((10000000 + RANDOM % 1000000))
RANDOM_ID2=$((10000000 + RANDOM % 1000000))

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Counters
PASSED=0
FAILED=0
TOTAL=0

# Function to test endpoint
test_endpoint() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local expected_status="$5"

    TOTAL=$((TOTAL + 1))

    echo -e "\n${BLUE}[$TOTAL] ${YELLOW}$name${NC}"

    if [ "$method" == "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "$url")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi

    status_code=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | sed '$d')

    if [ "$status_code" == "$expected_status" ]; then
        echo -e "${GREEN}✓ PASSED${NC} (Status: $status_code)"
        if [ -n "$body" ] && [ "$body" != "null" ]; then
            echo "Response: ${body:0:150}..."
        fi
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}✗ FAILED${NC} (Expected: $expected_status, Got: $status_code)"
        echo "Response: ${body:0:150}..."
        FAILED=$((FAILED + 1))
    fi
}

echo "========================================="
echo "   SportBot API Testing Report"
echo "========================================="

# 1. USER ENDPOINTS
echo -e "\n${YELLOW}=== USER ENDPOINTS ===${NC}"

test_endpoint \
    "Register New User" \
    "POST" \
    "$BASE_URL/api/v1/users" \
    '{"telegramId":'$RANDOM_ID1',"sendPulseId":"test_'$RANDOM_ID1'","isSubscribed":true,"fullName":"Test User '$RANDOM_ID1'","sex":"MAN","age":25,"referrerTelegramId":null,"language":"ru"}' \
    "200"

test_endpoint \
    "Register User with Referrer" \
    "POST" \
    "$BASE_URL/api/v1/users" \
    '{"telegramId":'$RANDOM_ID2',"sendPulseId":"test_'$RANDOM_ID2'","isSubscribed":true,"fullName":"Test User '$RANDOM_ID2'","sex":"WOMAN","age":28,"referrerTelegramId":'$TEST_TELEGRAM_ID',"language":"en"}' \
    "200"

test_endpoint \
    "Get User Locale" \
    "GET" \
    "$BASE_URL/api/v1/users/$TEST_TELEGRAM_ID/locale" \
    "" \
    "200"

test_endpoint \
    "Unsubscribe User" \
    "POST" \
    "$BASE_URL/api/v1/users/unsubscribe/$RANDOM_ID1" \
    "" \
    "200"

# 2. EXERCISE ENDPOINTS
echo -e "\n${YELLOW}=== EXERCISE ENDPOINTS ===${NC}"

test_endpoint \
    "Log Exercise (Push-ups)" \
    "POST" \
    "$BASE_URL/api/v1/exercises" \
    '{"telegramId":'$TEST_TELEGRAM_ID',"exerciseType":"push_up","count":50,"date":"2026-04-17"}' \
    "200"

test_endpoint \
    "Log Exercise (Pull-ups)" \
    "POST" \
    "$BASE_URL/api/v1/exercises" \
    '{"telegramId":'$TEST_TELEGRAM_ID',"exerciseType":"pull_up","count":10,"date":"2026-04-17"}' \
    "200"

test_endpoint \
    "Log Max Exercise" \
    "POST" \
    "$BASE_URL/api/v1/exercises/max" \
    '{"telegramId":'$TEST_TELEGRAM_ID',"exerciseType":"pull_up","count":20}' \
    "200"

test_endpoint \
    "Get Exercise Progress" \
    "GET" \
    "$BASE_URL/api/v1/exercises/progress/$TEST_TELEGRAM_ID?exerciseType=push_up&startDate=2026-04-01&endDate=2026-04-30" \
    "" \
    "200"

test_endpoint \
    "Get Today Progress" \
    "GET" \
    "$BASE_URL/api/v1/exercises/today?telegramId=$TEST_TELEGRAM_ID" \
    "" \
    "200"

test_endpoint \
    "Get Calendar" \
    "GET" \
    "$BASE_URL/api/v1/exercises/calendar/$TEST_TELEGRAM_ID?year=2026&month=4" \
    "" \
    "200"

# 3. LEADERBOARD ENDPOINTS
echo -e "\n${YELLOW}=== LEADERBOARD ENDPOINTS ===${NC}"

test_endpoint \
    "Get Rating" \
    "GET" \
    "$BASE_URL/api/v1/leaderboards/rating?telegramId=$TEST_TELEGRAM_ID" \
    "" \
    "200"

test_endpoint \
    "Get Leaderboard by Period (Push-ups)" \
    "GET" \
    "$BASE_URL/api/v1/leaderboards/push_up/by-period?period=ALL_TIME&telegramId=$TEST_TELEGRAM_ID&size=5" \
    "" \
    "200"

test_endpoint \
    "Get Leaderboard by Period Paged" \
    "GET" \
    "$BASE_URL/api/v1/leaderboards/push_up/by-period/paged?period=MONTH&telegramId=$TEST_TELEGRAM_ID&page=0&size=10" \
    "" \
    "200"

test_endpoint \
    "Get Leaderboard by Dates" \
    "GET" \
    "$BASE_URL/api/v1/leaderboards/push_up/by-dates?startDate=2026-04-01&endDate=2026-04-30&telegramId=$TEST_TELEGRAM_ID&size=5" \
    "" \
    "200"

# 4. PROFILE ENDPOINTS
echo -e "\n${YELLOW}=== PROFILE ENDPOINTS ===${NC}"

test_endpoint \
    "Get Profile" \
    "GET" \
    "$BASE_URL/api/v1/profile?telegramId=$TEST_TELEGRAM_ID" \
    "" \
    "200"

test_endpoint \
    "Update Profile" \
    "PUT" \
    "$BASE_URL/api/v1/profile" \
    '{"telegramId":'$TEST_TELEGRAM_ID',"age":27,"sex":"MAN","reminderTime":"10:00","language":"ru"}' \
    "200"

# 5. STREAK ENDPOINTS
echo -e "\n${YELLOW}=== STREAK ENDPOINTS ===${NC}"

test_endpoint \
    "Get Current Streak" \
    "GET" \
    "$BASE_URL/api/v1/streaks/current?telegramId=$TEST_TELEGRAM_ID" \
    "" \
    "200"

test_endpoint \
    "Get Best Streak" \
    "GET" \
    "$BASE_URL/api/v1/streaks/best?telegramId=$TEST_TELEGRAM_ID" \
    "" \
    "200"

test_endpoint \
    "Get All Streaks" \
    "GET" \
    "$BASE_URL/api/v1/streaks?telegramId=$TEST_TELEGRAM_ID" \
    "" \
    "200"

test_endpoint \
    "Save Streak" \
    "POST" \
    "$BASE_URL/api/v1/streaks/save?telegramId=$TEST_TELEGRAM_ID" \
    "" \
    "200"

# 6. SUBSCRIPTION ENDPOINTS
echo -e "\n${YELLOW}=== SUBSCRIPTION ENDPOINTS ===${NC}"

test_endpoint \
    "Get All Users Paged" \
    "GET" \
    "$BASE_URL/api/v1/subscriptions/users/paged?page=0&size=10" \
    "" \
    "200"

# Use existing test users for subscription tests
TEST_USER_1=1000001
TEST_USER_2=1000002

test_endpoint \
    "Subscribe to User" \
    "POST" \
    "$BASE_URL/api/v1/subscriptions/$TEST_USER_1/subscribe/$TEST_USER_2" \
    "" \
    "200"

test_endpoint \
    "Get Following" \
    "GET" \
    "$BASE_URL/api/v1/subscriptions/$TEST_USER_1/following" \
    "" \
    "200"

test_endpoint \
    "Get Followers" \
    "GET" \
    "$BASE_URL/api/v1/subscriptions/$TEST_USER_2/followers" \
    "" \
    "200"

test_endpoint \
    "Compare Users" \
    "GET" \
    "$BASE_URL/api/v1/subscriptions/$TEST_USER_1/compare/$TEST_USER_2?exerciseCode=push_up" \
    "" \
    "200"

test_endpoint \
    "Unsubscribe from User" \
    "POST" \
    "$BASE_URL/api/v1/subscriptions/$TEST_USER_1/unsubscribe/$TEST_USER_2" \
    "" \
    "200"

# 7. ACHIEVEMENT ENDPOINTS
echo -e "\n${YELLOW}=== ACHIEVEMENT ENDPOINTS ===${NC}"

test_endpoint \
    "Get User Achievements" \
    "GET" \
    "$BASE_URL/api/v1/achievements?telegramId=$TEST_TELEGRAM_ID" \
    "" \
    "200"

test_endpoint \
    "Get Monthly Congratulation" \
    "GET" \
    "$BASE_URL/api/v1/achievements/congratulation?telegramId=$TEST_TELEGRAM_ID&yearMonth=2026-04" \
    "" \
    "200"

test_endpoint \
    "Send Monthly Congratulation" \
    "POST" \
    "$BASE_URL/api/v1/achievements/congratulation" \
    '{"yearMonth":"2026-04"}' \
    "200"

# 8. COMPETITORS ENDPOINTS
echo -e "\n${YELLOW}=== COMPETITORS ENDPOINTS ===${NC}"

test_endpoint \
    "Get Competitors (Push-ups)" \
    "GET" \
    "$BASE_URL/api/v1/competitors/push_up?telegramId=$TEST_TELEGRAM_ID" \
    "" \
    "200"

test_endpoint \
    "Get Competitors (Pull-ups)" \
    "GET" \
    "$BASE_URL/api/v1/competitors/pull_up?telegramId=$TEST_TELEGRAM_ID" \
    "" \
    "200"

# 9. MOTIVATION ENDPOINTS
echo -e "\n${YELLOW}=== MOTIVATION ENDPOINTS ===${NC}"

test_endpoint \
    "Get Motivation (RU)" \
    "GET" \
    "$BASE_URL/api/v1/motivation?exerciseType=push_up&locale=ru" \
    "" \
    "200"

test_endpoint \
    "Get Motivation (EN)" \
    "GET" \
    "$BASE_URL/api/v1/motivation?exerciseType=pull_up&locale=en" \
    "" \
    "200"

test_endpoint \
    "Get Motivation (UK)" \
    "GET" \
    "$BASE_URL/api/v1/motivation?exerciseType=squat&locale=uk" \
    "" \
    "200"

# 10. USER PROGRAM ENDPOINTS
echo -e "\n${YELLOW}=== USER PROGRAM ENDPOINTS ===${NC}"

test_endpoint \
    "Get User Programs" \
    "GET" \
    "$BASE_URL/api/v1/programs?telegramId=$TEST_TELEGRAM_ID&exerciseType=push_up" \
    "" \
    "200"

# SUMMARY
echo -e "\n========================================="
echo -e "           TEST SUMMARY"
echo -e "========================================="
echo -e "Total Tests: ${BLUE}$TOTAL${NC}"
echo -e "${GREEN}✓ Passed: $PASSED${NC}"
echo -e "${RED}✗ Failed: $FAILED${NC}"

if [ $TOTAL -gt 0 ]; then
    success_rate=$(awk "BEGIN {printf \"%.1f\", ($PASSED/$TOTAL)*100}")
    echo -e "Success Rate: ${success_rate}%"
fi

echo -e "=========================================\n"

# Exit with error code if any test failed
if [ $FAILED -gt 0 ]; then
    exit 1
fi

exit 0
