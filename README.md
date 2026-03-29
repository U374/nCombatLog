# 🛡️ nCombatLog
A lightweight and configurable combat logging prevention plugin for Minecraft servers.  
Stop players from escaping fights unfairly and keep PvP fair and competitive.

## ⚔️ Features

### 🕒 Combat Timer
- Players are tagged in combat after PvP
- Customizable duration via config

### 📊 BossBar Timer
- Visual countdown showing combat status
- Displays opponent name and remaining time

### 🚫 Combat Logging Prevention
- Detects if a player logs out during combat
- Multiple punishment modes available

### ⚙️ Action Modes
- 1 → Log to Discord only
- 2 → Kill player, drop loot, and log to Discord
- 3 → Instantly kill player (no Discord log)

### 📡 Discord Webhook Logging
- Clean embed logs for combat logging events
- Includes attacker, victim, timestamps, and ping info

### 🧠 Smart Detection System
- Prevents false punishments on:
  • Server shutdown
  • Kicks / admin actions
- Ignores configured damage types

### 🔧 Highly Configurable
- Editable combat timer
- Custom ignored damage sources
- Easy-to-understand config files

### 🔄 Reload Command
- Reload config without restarting server
```
/ncombatlogreload
```
