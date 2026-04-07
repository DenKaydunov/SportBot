![Main view white.png](img/Main%20view%20white.png)
# Sport Bot 💪

Java-based Telegram bot for tracking fitness challenges (push-ups, squats, pull-ups).  
Originally built using SendPulse & Google Sheets, now migrated to a full Java backend.

## Telegram Bot

Запусти бота 👉 [@PushupsWardengzBot](https://t.me/PushupsWardengzBot)  
Web-site: https://pushupswardengzbot.tg.pulse.is/

## Features

✅ Input results  
📊 Statistics by days, weeks, months  
🏆 Leaderboard by members  
🔄 Automatic progress counting  
💬 Motivational quotes  
📅 Support challenge and maximum checks  

## Tech Stack

- Java 23
- Telegram Bot API
- PostgreSQL
- Docker
- Spring Boot

<details>
<summary>
Installation
</summary>

1. Install brew:

```shell
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)" 
```

2. Install necessary tools:

```shell
brew install gh
brew install maven
```

3. Install Java 23:

```shell
brew install openjdk@23
java -version
```

4. Login to gh:

```shell
gh auth login
```

5. Download project:

```shell
gh repo clone DenKaydunov/SportBot
```

6. Build project:

```shell
mvn clean install
```

7. [Install and start Docker](https://docs.docker.com/get-started/get-docker/)

8. Run Postgres database using Docker:

```shell
docker compose -f docker/docker-compose.yaml up -d
```

9. Run application:

```shell
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

10. [Swagger](http://localhost:8080/swagger-ui/index.html)

</details>

<details>

<summary>
How to run the app locally with SendPulse chains?
</summary>

1. Create ngrok account: https://dashboard.ngrok.com/
2. Copy your auth token https://dashboard.ngrok.com/get-started/your-authtoken
3. Install ngrok via Homebrew with the following command:

```shell
brew install ngrok
```
4. Connect your agent to your ngrok account by providing your auth token as shown below—replace $YOUR_TOKEN with the string given to you in the dashboard.

```
ngrok config add-authtoken $YOUR_TOKEN
```
5. Put your app online:

```shell
ngrok http http://localhost:8080
```

</details>

<details>

<summary>
Deploy
</summary>

This section outlines how to deploy the Sport Bot application to Google Cloud Platform (GCP).

1.  **Google Cloud Account:** Ensure you have an active Google Cloud account and a project set up.
2.  **Google Cloud SDK:** Install the `gcloud` command-line tool.

```shell
brew install --cask google-cloud-sdk
```
3. Init project for GCP CLI:
```shell
gcloud init
```  

4. Create maven-wrapper:
```shell
mvn wrapper:wrapper 
```
5. Verify env variables in application yaml.
6. Verify spring.cloud variables in application yaml.
7. Deploy app to GCP:

```shell
gcloud app deploy
```
8. **Go to bot in Telegram and verify that bot is still working!**

⚠️ **IMPORTANT: Telegram Mini App Cache Issue**

After deploying updates to Telegram Mini Apps (e.g., calendar.html), you MUST completely restart the mini app session in Telegram to see the changes. Simply refreshing the page is not enough.

**Why this happens:**
- Telegram aggressively caches Mini App content
- The Telegram app maintains a persistent WebView session
- Old JavaScript/HTML/CSS files remain in memory even after redeployment

**How to fix:**
1. **Close the Mini App completely** (swipe down/press back until you exit the Mini App)
2. **Close the entire chat** with the bot (go back to chat list)
3. **Reopen the chat** and launch the Mini App again
4. Alternatively, use **hard refresh** in browser: `Ctrl + F5` (Windows/Linux) or `Cmd + Shift + R` (Mac)
5. If still not working, **clear Telegram cache**: Settings → Data and Storage → Storage Usage → Clear Cache

**For developers:**
- Always test Mini App changes in a fresh session
- Use browser DevTools Console to verify which version is loaded
- Check for CORS errors if using external API calls
- Verify that `BACKEND_URL` is correctly configured (use empty string `""` for same-origin requests)

You can stream logs from the command line by running:

```shell
gcloud app logs tail -s default
```

To view your application in the web browser run:
```shell
gcloud app browse
```


</details>