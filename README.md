# TeamQuests
TeamQuests is a plugin for spigot 1.20+ that allows players to form teams and complete customizable quests with custom prizes. To install it, simply put it in your `plugins` folder and start the server. Then, you can edit the `quests.yml` file to create your custom quests.

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

## quests.yml
This file is for configuring all the quests that will be shown in the `/team quests` menu. The configuration options are explained in the default quest.yml file, but further explanation for some options is provided below.
### type
The quest `type` can be one of the following:
- `BREAK_BLOCKS`: Break a certain number of blocks
- `PLACE_BLOCKS`: Place a certain number of blocks
- `KILL_ENTITIES`: Kill a certain number of entities
- `EARN_EXP`: Earn a certain number of EXP points
- `TRAVEL_BLOCKS`: Travel a certain number of blocks
- `CUSTOM`: These quests can only be advanced through the `/team addprogress` command (see above.) Useful for hooking into plugins that allow for automatic command execution when certain events are triggered.
### valid-objects
These are the valid blocks/entities that are required to advance in the quest (only applicable to certain quest types.) For the `BREAK_BLOCKS` and `PLACE_BLOCKS` quest types, this must be a list of block types. For `KILL_ENTITIES`, it must be a list of entity types. If you omit this option, all blocks/entities will count toward the goal.

Examples are shown in the default quests.yml file. For example, to make a quest where you must kill a certain number of players, you would write:
```
  type: KILL_ENTITIES
  valid-objects:
    - PLAYER
```
