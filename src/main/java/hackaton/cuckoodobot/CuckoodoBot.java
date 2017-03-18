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
import java.util.regex.Pattern;

import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @author Dmitry Tarasov, Anastasia Yarunina(@crazzysun)
 *         Date: 03/17/2017
 *         Time: 20:03
 */
public class CuckoodoBot extends TelegramLongPollingBot {

    private final static String[] ADD = {"add", "a", "д", "добавить", "задача", "еще"};
    private final static String[] LIST = {"list", "l", "c", "список", "все", "всё"};
    private final static String[] DONE = {"done", "d", "г", "готово", "готов", "сделаль", "сделать", "сделано", "выполнено", "разделался"};
    private final static String[] DELETE = {"del", "r", "y", "удалить", "убрать"};
    private final static String[] ASSIGNEE = {"assignee", "t", "н", "назначить", "навесить", "перевестистрелки"};
    private final static String[] HELP = {"help", "h", "п", "помощь", "хелп", "памагите", "ничего не понимаю"};
    private final static String[] FULLHELP = {"fullhelp", "f", "в", "всяпомощь", "ещепомощь", "команды", "ещекоманды"};
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

    private final Pattern intervalPattern = Pattern.compile("(\\d{1,2} d)?(\\d{1,2} h)?(\\d{1,2} min)?");

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
                    String help = "Привет! Я кукуду-туду-лист с напоминалками, буду куковать тебе о важных вещах, чтобы ты не забыл.\n\r" +
                            "Используй команды /помощь для помощи В)\n" +
                            "Список моих команд:\n\n" +
                            "[] -- опционально. По умолчанию -- весь чат.\n" +
                            "/добавить\tдобавить задачу\n" +
                            "\t/добавить задача [@username]\n" +
                            "/список\tвывести все задачи\n" +
                            "\t/список\n" +
                            "/готово\tпометить задачу как готовую\n" +
                            "\t/готово 1\n" +
                            "/удалить\tудалить задачу\n" +
                            "\tудалить 1\n" +
                            "/назначить\tназначить задачу\n" +
                            "\t/назначить 1 [@username]\n" +
                            "/помощь\tвывести эту помощь\n" +
                            "/всяпомощь\tвывести полный список команд\n" +
                            "/eng\t/if you speak only english, use this command. But we are recommend use russian text(and fullhelp)!";
                    sendMessage(help);
                } else if (startWith(FULLHELP, messageText)) {
                    String full = "добавить: задача, еще, д, add, a\n" +
                            "список: все, всё, с, list, l\n" +
                            "готово: готов, сделать, сделано, сделаль, выполнено, разделался, г, done, d\n" +
                            "удалить: убрать, у, del, r\n" +
                            "назначить: навесить, перевестистрелки, н, assignee, t\n" +
                            "помощь: хелп, памагите, ничегонепонимаю, п, help, h\n" +
                            "всяпомощь: ещепомощь, \"ещепомощь\", команды, ещекоманды, в, fullhelp, f\n" +
                            "eng: english, englishplease";
                    sendMessage(full);
                } else if (startWith(ENGLISHHELP, messageText)) {
                    String enghelp = "add\tadd a issue\n" +
                            "list\tview a list with all issues\n" +
                            "done\tmark issue as done\n" +
                            "del\tdelete issue\n" +
                            "assignee\tassignee issue\n" +
                            "help\tprint help\n" +
                            "fullhelp\tprint full help\n" +
                            "eng\tprint help in english";
                    sendMessage(enghelp);
                } else {
                    sendMessage("Нет такой команды! /помощь");
                }
            } catch (Exception e) {
                sendMessage(somethingWentWrong + "\n\rПопробуй еще раз.");
            }
        }
    }

    private void reassigneeIssue(Message message) {
        String text = message.getText().trim();
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
            sendMessage("Не могу это удалить. /помощь");
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
        List<Issue> issues = dataSource.getIssueForGroup(groupId);
        if (issues.size() == 0) {
            sendMessage("Задач нет! Используй /помощь.");
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
