![Main view white.png](img/Main%20view%20white.png)
# Sport Bot 💪 (Java Edition)  
Java-based Telegram bot for tracking fitness challenges (push-ups, squats, pull-ups).  
Originally built using SendPulse & Google Sheets, now migrated to a full Java backend.

### Telegram Bot
Запусти бота 👉 [@PushupsWardengzBot](https://t.me/PushupsWardengzBot)  
Web-site: https://pushupswardengzbot.tg.pulse.is/

### Features
✅ Input results  
📊 Statistics by days, weeks, months  
🏆 Leaderboard by members  
🔄 Automatic progress counting  
💬 Motivational quotes  
📅 Support challenge and maximum checks  

## Новые изменения 
- 
### Tech Stack
- Java 23
- Telegram Bot API
- PostgresSQL
- Docker
- Spring Boot

<details>
<summary>
How to run the app locally with SendPulse chains?
</summary>

1. Create ngrok account: https://dashboard.ngrok.com/
2. Copy your-authtoken https://dashboard.ngrok.com/get-started/your-authtoken
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
