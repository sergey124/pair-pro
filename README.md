# pair-pro
Telegram bot for pair programming session scheduling

WIP, the idea is to create or join team, and suggest partner and time.

## Usage
* Find the bot in Telegram: https://t.me/pprobot
* /start 
* /team
* /pair

# Collaboration
# How to run
* Specify env variables: tg.bot.token, tg.bot.username (get from BotFather)
* run \*Application.kt
* For local testing, activate spring profile `test_local` - some initial data and user activity simulation
  * application.yml: spring.profiles.active: test_local
  * or env variable SPRING_PROFILES_ACTIVE = test_local

## tech stack
- Kotlin
- Spring Boot, Data
- Telegrambots
