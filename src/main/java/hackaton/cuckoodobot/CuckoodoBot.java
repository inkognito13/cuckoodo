package hackaton.cuckoodobot;

import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.quartz.TriggerBuilder.*;

/**
 * @author Dmitry Tarasov
 *         Date: 03/17/2017
 *         Time: 20:03
 */
public class CuckoodoBot extends TelegramLongPollingBot {

    private String botToken;
    private Scheduler scheduler;
    private static DataSource dataSource;
    private static CuckoodoBot bot;

    private final static String[] ADD = {"add", "добавить","напомни"};
    private final static String[] LIST = {"list", "список", "все"};
    private final static String[] DONE = {"done", "готово", "готов", "сделаль", "сделать", "сделано"};
    private final static String[] DELETE = {"del", "удалить"};
    private final static String[] DAY = {"day", "день"};
    private final static String[] HOUR = {"hour", "час"};
    private final static String[] MINUTE = {"min", "минут"};
    private final static String[] SECOND = {"sec", "секунд","секунду"};


    private final Pattern intervalPattern = Pattern.compile("(\\d{1,2} d)?(\\d{1,2} h)?(\\d{1,2} min)?");

    public CuckoodoBot(String botToken, Scheduler scheduler) {
        this.botToken = botToken;
        this.scheduler = scheduler;
        this.dataSource = new DataSource();
        this.bot = this;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return "cuckodootest_bot";
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = message.getText();

            if (startWith(ADD, messageText)) {
                addIssue(message);
            } else if (startWith(LIST, messageText)) {
                listIssue(message);
            } else if (startWith(DONE, messageText)) {
                doneIssue(message);
            } else if (startWith(DELETE, messageText)) {
                deleteIssue(message);
            }
        }
    }

    private void deleteIssue(Message message) {
        long groupId = message.getChatId();
        int idx = Integer.parseInt(deleteCommand(message.getText()));
        Issue res = dataSource.deleteIssue(idx, groupId);
        if (res != null) {
            sendMessage("Запись была удалена:\n\r" + res.toString(), groupId);
        } else {
            sendMessage("Не могу это удалить.", groupId);
        }
    }

    private void addIssue(Message message) {
        String messageWithAssignee = deleteCommand(message.getText());
        String[] messageArr = messageWithAssignee.split(" ");
        String lastWord = messageArr[messageArr.length - 1];
        Issue issue;

        if (lastWord.startsWith("@")) {
            String assignee = messageArr[messageArr.length - 1].substring(1);
            String messageText = messageWithAssignee.substring(0, messageWithAssignee.length() - assignee.length() - 1);
            issue = new Issue(message.getChatId(), messageText, assignee);
            messageArr = messageText.split(" ");
        } else {
            issue = new Issue(message.getChatId(), messageWithAssignee);
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
            for (int i=1;i<messagePartsWithoutInterval.size();i++){
                resultMessage+=" "+messagePartsWithoutInterval.get(i-1);
            }
            issue.setText(resultMessage);
        }
        dataSource.addIssue(issue);
        if (issue.getRepeat()!=null){
            scheduleIssue(issue);
        }
        sendMessage("Добавлена заметка для " + issue.getAssignee(), message.getChatId());
    }

    private void listIssue(Message message) {
        long groupId = message.getChatId();

        List<Issue> issues = dataSource.getIssueForGroup(groupId);
        StringBuilder res = new StringBuilder();
        int idx = 0;
        for (Issue issue : issues) {
            idx++;
            res.append(formatIssue(issue, idx)).append("\n\r");
        }

        sendMessage(res.toString(), message.getChatId());
    }

    private void doneIssue(Message message) {
        long groupId = message.getChatId();
        int idx = Integer.parseInt(deleteCommand(message.getText()));
        if (dataSource.doneIssue(idx, groupId)) {
            listIssue(message);
        } else {
            sendMessage("Что-то пошло не так \uD83D\uDCA9", message.getChatId());
        }
    }

    private boolean startWith(String[] commands, String message) {
        for (String command : commands) {
            if (message.startsWith("/" + command)) {
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

    private void sendMessage(String message, Long groupId) {
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

//        return "Ответственный: " + issue.getAssignee() + "\n\r" + done + idx + ". " + issue.getText();
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

            Issue issue = dataSource.getIssue(Long.parseLong(key.getName()),Long.parseLong(key.getGroup()));

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
