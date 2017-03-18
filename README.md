# cuckoodo
Elastic Telegram task-manager/reminder for chats

[] -- опционально. По умолчанию -- весь чат.
/добавить/tдобавить задачу
/t/добавить задача [@username]
/список/tвывести все задачи
/t/список
/готово/tпометить задачу как готовую
/t/готово 1
/удалить/tудалить задачу
/tудалить 1
/назначить/tназначить задачу
/t/назначить 1 [@username]
/помощь/tвывести эту помощь
/всяпомощь/tвывести полный список команд
/eng/t/if you speak only english, use this command. But we are recommend use russian text(and fullhelp)!

ADD = {"add", "a", "д" "добавить", "задача", "еще"};
LIST = {"list", "l", "c", "список", "все", "всё"};
DONE = {"done", "d", "г", "готово", "готов", "сделаль", "сделать", "сделано", "выполнено", "разделался"};
DELETE = {"del", "r", "y","удалить", "убрать"};
ASSIGNEE = {"assignee", "t", "н", "назначить", "навесить", "перевестистрелки"};
ELP = {"help", "h","п" "помощь", "хелп", "памагите", "ничегонепонимаю"};
FULLHELP = {"fullhelp","f","в" "всяпомощь", "ещепомощь", "};
ENGLISHHELP = {"eng", "english", "englishplease"};

добавить: задача, еще, д, add, a
список: все, всё, с, list, l
готово: готов, сделать, сделано, сделаль, выполнено, разделался, г, done, d
удалить: убрать, у, del, r
назначить: навесить, перевестистрелки, н, assignee, t
помощь: хелп, памагите, ничегонепонимаю, п, help, h
всяпомощь: ещепомощь, "ещепомощь", команды, ещекоманды, в, fullhelp, f
eng: english, englishplease

add\tadd a issue
list\tview a list with all issues
done\tmark issue as done
del\tdelete issue
assignee\assignee issue
help\tprint help
fullhelp\tprint full help
eng\tprint help in english
