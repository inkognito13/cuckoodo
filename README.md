# cuckoodo
Elastic Telegram task-manager/reminder for chats


# Interface spec

`/add <issue> [@assignee]`

  Examples:

  `/add buy a bottle [@assignee]`

  `/add buy a bottle`

`/list [@assignee]`

  Examples:

  `/list`

  `/list @vasya`


### User story #0:
  User wants to add an todo issue by telegram bot.
  By default the issue without assignee will be assigned to the chat.

### User story #1:
  User wants to change an assignee for an issue.

### User story #2:
  User could delete an issue.

  Issue <id> SHOULD BE short.

### User story #3:
  User could get a list of all issues or filtered by assignee.

  `/list`
  `/list @vasya`

### User story #4 ( Risk: We should add an additional property - status of the issue )
  User could set 'Done' for the issue.

  `/done <id>`

  10. user get a list of issues (all ids SHOUL BE present)
  11. user set the issue's type as 'Done

### User story #5:
  User wants to fix a typo in the issue's body.
  `/change <id> "new (fixed) body"`

  User ui flow:
   10. user creates an issue
   11. the bot SHOULD return id of the created issue (as link to issue)
   20. user get a list of issues (all ids SHOUL BE present)
   30. user change the body of the specific issue by id

#### User story #6:
  User wants to fix a typo in the issue's body.
  User should remove the issue by <id> and create it again.

  `/create`
  `/delete`
  `/create`

  `/list`
  `/delete`
  `/create`
  User ui flow:
   10. user creates an issue
   11. the bot SHOULD return id of the created issue (as link to issue)
   20. user get a list of issues (all ids SHOUL BE present)
   30. user change the body of the specific issue by id

