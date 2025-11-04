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
