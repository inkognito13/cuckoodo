package hackaton.cuckoodobot;

import org.quartz.*;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @author Dmitry Tarasov, Anastasia Yarunina(@crazzysun)
 *         Date: 03/17/2017
 *         Time: 20:03
 */
public class CuckoodoBot extends TelegramLongPollingBot {

    private final static String[] ADD = {"add", "д", "добавить", "задача", "еще"};
    private final static String[] LIST = {"list", "c", "список", "все", "всё"};
    private final static String[] DONE = {"done", "г", "готово", "готов", "сделаль", "сделать", "сделано", "выполнено", "разделался"};
    private final static String[] DELETE = {"del", "y", "удалить", "убрать"};
    private final static String[] ASSIGNEE = {"assignee", "н", "назначить", "навесить", "перевестистрелки"};
    private final static String[] HELP = {"help", "п", "помощь", "хелп", "памагите", "ничегонепонимаю"};
    private final static String[] FULLHELP = {"fullhelp", "в", "всяпомощь", "ещепомощь", "команды", "ещекоманды"};
    private final static String[] ENGLISHHELP = {"eng", "english", "englishplease"};
    private final static String[] DAY = {"day", "день"};
    private final static String[] HOUR = {"hour", "час"};
    private final static String[] MINUTE = {"min", "минут"};
    private final static String[] SECOND = {"sec", "секунд", "секунду"};

    private String botToken;
    private Scheduler scheduler;
    private static DataSource dataSource;
    private static CuckoodoBot bot;
    private String somethingWentWrong = "\uD83D\uDCA9 Что-то пошло не так! \uD83D\uDCA9";
    private long groupId;

    private static String helpMessage = "Привет! Я кукуду-туду-лист с напоминалками, буду куковать тебе о важных вещах, чтобы ты не забыл.\n" +
            "Используй команду /help для помощи В)\n" +
            "Список моих команд:\n" +
            "   [] -- опционально. По умолчанию -- весь чат.\n" +
            "/add - добавить задачу\n" +
            "    /напомнить купить мяса @username\n" +
            "    /добавить заехать за Петей\n" +
            "/list - вывести все задачи\n" +
            "    /список\n" +
            "    /список @username\n" +
            "/done - пометить задачу как готовую\n" +
            "    /готово 1\n" +
            "/del - удалить задачу\n" +
            "    /удалить 1\n" +
            "/assign - назначить задачу\n" +
            "    /назначить 1 [@username]\n" +
            "/help - вывести эту помощь\n" +
            "/fullhelp - полный список команд\n" +
            "/eng - if you speak only english, use this command. But we recommend use russian text(and fullhelp).";

    private static String fullHelpMessage = "/add: добавить, задача, напомнить, д\n" +
            "/list: список, все, всё, в\n" +
            "/done: готово, готов, сделать, сделано, сделаль, выполнено, разделался, г\n" +
            "/del: удалить, убрать, у\n" +
            "/assign: назначить, навесить, перевестистрелки, н\n" +
            "/help помощь, хелп, памагите, ничегонепонимаю, п\n" +
            "/fullhelp: всяпомощь, ещепомощь, команды, ещекоманды, в\n" +
            "/eng: english, englishplease";

    private static String enghelpMessage = "add - add an issue\n" +
            "list - view a list with all issues\n" +
            "done - mark the issue as done by id\n" +
            "del - delete the issue by id\n" +
            "assign - assign issue\n" +
            "help - print help\n" +
            "fullhelp - print full help\n" +
            "eng - print help in english";

    CuckoodoBot(String botToken, Scheduler scheduler) {
        this.botToken = botToken;
        this.scheduler = scheduler;
        dataSource = new DataSource();
        bot = this;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return "cuckodoobot";
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = message.getText();
            groupId = message.getChatId();

            try {
                if (startWith(ADD, messageText)) {
                    addIssue(message);
                } else if (startWith(LIST, messageText)) {
                    listIssue(message);
                } else if (startWith(DONE, messageText)) {
                    doneIssue(message);
                } else if (startWith(DELETE, messageText)) {
                    deleteIssue(message);
                } else if (startWith(ASSIGNEE, messageText)) {
                    reassigneeIssue(message);
                } else if (startWith(HELP, messageText)) {
                    sendMessage(helpMessage);
                } else if (startWith(FULLHELP, messageText)) {
                    sendMessage(fullHelpMessage);
                } else if (startWith(ENGLISHHELP, messageText)) {
                    sendMessage(enghelpMessage);
                } else {
                    sendMessage("Нет такой команды! /help");
                }
            } catch (Exception e) {
                System.err.println(e.toString());
                sendMessage(somethingWentWrong + "\n\rПопробуй еще раз.");
            }
        }
    }

    private void reassigneeIssue(Message message) {
        String text = deleteCommand(message.getText()).trim();
        String assignee = getAssignee(text);

        if (assignee != null) {
            text = text.split(" ")[0];
        } else {
            assignee = "all";
        }

        int idx = Integer.parseInt(text);

        if (dataSource.assigneeIssue(idx, assignee, groupId)) {
            sendMessage("Задача " + idx + " назначена на " + assignee);
        } else {
            sendMessage(somethingWentWrong);
        }
    }

    private void deleteIssue(Message message) {
        int idx = Integer.parseInt(deleteCommand(message.getText()));
        Issue res = dataSource.deleteIssue(idx, groupId);
        if (res != null) {
            sendMessage("Запись была удалена:\n\r" + res.toString());
        } else {
            sendMessage("Не могу это удалить. /help");
        }
    }

    private void addIssue(Message message) {
        String messageWithAssignee = deleteCommand(message.getText());
        String assignee = getAssignee(messageWithAssignee);
        String[] messageArr = messageWithAssignee.split(" ");
        Issue issue;

        if (assignee != null) {
            String messageText = messageWithAssignee.substring(0, messageWithAssignee.length() - assignee.length() - 1);
            messageArr = messageText.split(" ");
            issue = new Issue(groupId, messageText, assignee);
        } else {
            issue = new Issue(groupId, messageWithAssignee);
        }

        int interval = 0;

        List<String> messagePartsWithoutInterval = new ArrayList<String>(Arrays.asList(messageArr));

        for (int i = 0; i < messageArr.length; i++) {
            for (String day : DAY) {
                if (messageArr[i].equals(day)) {
                    interval += Integer.parseInt(messageArr[i - 1]) * 24 * 60 * 60;
                    messagePartsWithoutInterval.remove(messageArr[i]);
                    messagePartsWithoutInterval.remove(messageArr[i - 1]);
                }
            }
            for (String hour : HOUR) {
                if (messageArr[i].equals(hour)) {
                    interval += Integer.parseInt(messageArr[i - 1]) * 60 * 60;
                    messagePartsWithoutInterval.remove(messageArr[i]);
                    messagePartsWithoutInterval.remove(messageArr[i - 1]);
                }
            }
            for (String minute : MINUTE) {
                if (messageArr[i].equals(minute)) {
                    interval += Integer.parseInt(messageArr[i - 1]) * 60;
                    messagePartsWithoutInterval.remove(messageArr[i]);
                    messagePartsWithoutInterval.remove(messageArr[i - 1]);
                }
            }
            for (String second : SECOND) {
                if (messageArr[i].equals(second)) {
                    interval += Integer.parseInt(messageArr[i - 1]);
                    messagePartsWithoutInterval.remove(messageArr[i]);
                    messagePartsWithoutInterval.remove(messageArr[i - 1]);
                }
            }
        }

        if (interval > 0) {
            issue.setRepeat(new Repeat(interval));
            String resultMessage = messagePartsWithoutInterval.get(0);
            for (int i = 1; i < messagePartsWithoutInterval.size(); i++) {
                resultMessage += " " + messagePartsWithoutInterval.get(i - 1);
            }
            issue.setText(resultMessage);
        }
        dataSource.addIssue(issue);
        if (issue.getRepeat() != null) {
            scheduleIssue(issue);
        }
        sendMessage("Добавлена заметка для " + issue.getAssignee());
    }

    private void listIssue(Message message) {
        String text = deleteCommand(message.getText()).trim();
        String assignee = getAssignee(text);
        List<Issue> issues;
        if (assignee != null) {
            issues = dataSource.getAllIssueForUser(assignee, groupId);
        } else {
            issues = dataSource.getIssueForGroup(groupId);
        }

        if (issues.size() == 0) {
            sendMessage("Задач нет!");
        } else {
            StringBuilder res = new StringBuilder();
            int idx = 0;
            for (Issue issue : issues) {
                idx++;
                res.append(formatIssue(issue, idx)).append("\n\r");
            }
            sendMessage(res.toString());
        }
    }

    private void doneIssue(Message message) {
        int idx = Integer.parseInt(deleteCommand(message.getText()));
        if (dataSource.doneIssue(idx, groupId)) {
            listIssue(message);
        } else {
            sendMessage(somethingWentWrong);
        }
    }

    private String getAssignee(String message) {
        String[] messageArr = message.split(" ");
        String lastWord = messageArr[messageArr.length - 1];

        if (lastWord.startsWith("@")) {
            return lastWord.substring(1);
        }
        return null;
    }

    private boolean startWith(String[] commands, String message) {
        for (String command : commands) {
            if (message.trim().split(" ")[0].equals("/" + command)) {
                return true;
            }
        }
        return false;
    }

    private String deleteCommand(String rawValue) {
        String[] arr = rawValue.split(" ");

        if (arr.length > 1) {
            String ans = arr[1];

            for (int i = 2; i < arr.length; i++) {
                ans += " " + arr[i];
            }
            return ans;
        }

        return "";
    }

    private void sendMessage(String message) {
        try {
            sendMessage(
                    new SendMessage()
                            .setChatId(groupId)
                            .setText(message)
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String formatIssue(Issue issue, int idx) {
        String done = (issue.getDone()) ? "\u2705" : "\uD83D\uDCCC";
        return done + idx + ". " + issue.getText() + " @" + issue.getAssignee();
    }

    private static String formatIssue(Issue issue) {
        return issue.getText() + " @" + issue.getAssignee();
    }

    private void scheduleIssue(Issue issue) {

        JobDetail jobDetail = JobBuilder.newJob(IssueJob.class)
                .withIdentity(new JobKey(issue.getId().toString(), issue.getGroupId().toString()))
                .build();

        Trigger trigger = newTrigger()
                .withIdentity(new TriggerKey(issue.getId().toString(), issue.getGroupId().toString()))
                .forJob(jobDetail)
                .startAt(DateBuilder.futureDate(issue.getRepeat().getTime(), DateBuilder.IntervalUnit.SECOND))
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    public static class IssueJob implements Job {
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            JobKey key = jobExecutionContext.getJobDetail().getKey();

            Issue issue = dataSource.getIssue(Long.parseLong(key.getName()), Long.parseLong(key.getGroup()));

            SendMessage message = new SendMessage()
                    .setChatId(issue.getGroupId())
                    .setText(formatIssue(issue));
            try {
                bot.sendMessage(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

}
