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
  User wants to add a todo issue by telegram bot.  
  By default the issue without assignee will be assigned to the chat.

### User story #1:
  User wants to change an assignee for an issue.

### User story #2:
  User could delete an issue by id in the issue list.

### User story #3:
  User could get a list of all issues or filtered by assignee.
  If user want to list his own issues, add self instead of assignee

  `/list`  
  `/list @vasya`

### User story #4
  User could 'Done' the issue.

  `/done <id>`

### User story #5:
  User could delete issue.  
  `/delete 1` 
  
### User story #6:
  User wants to fix a typo in the issue's body.  
  User should remove the issue by <id> and create it again.

  `/add`  
  `/delete`  
  `/add`

  `/list`
  `/delete`
  `/add`
  User ui flow:
   10. user creates an issue
   11. the bot SHOULD return id of the created issue (as link to issue)
   20. user get a list of issues (all ids SHOUL BE present)
   30. user change the body of the specific issue by id

### User story #7:
  User able to get help
     
  `/help`

### User story #8:
  User able to add a reminder 
     
  `/add do sometning 2 min @vasya`
     
### User story #9 
  User able to use autocomplete for list and help commands
     
