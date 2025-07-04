# TeamQuests
TeamQuests is a plugin for spigot 1.20+ that allows players to form teams and complete customizable quests with custom prizes.

## Commands
### Player Commands
- `/team`: View your team info. Requires permission `teamquests.info`
- `/team create <name>`: Create a team. Requires permission `teamquests.create`
- `/team invite <player>`: Invite a player to your team. Requires permission `teamquests.invite`
- `/team join <team>`: Join a team once invited. Requires permission `teamquests.join`
- `/team leave`: Leave/disband your team. Requires permission `teamquests.leave`
- `/team quests`: View quest progress menu. Requires permission `teamquests.quests`
### Admin Commands
- `/teamquests reload`: Reload config files. Requires permission `teamquests.admin.reload`
- `/team addprogress <player> <questId> [progress]`: Advances the progress of a specific quest for the player's team. Useful for creating custom quest types. Requires permission `teamquests.admin.addprogress`
