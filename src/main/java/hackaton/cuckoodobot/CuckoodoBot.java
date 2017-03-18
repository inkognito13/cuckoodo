package hackaton.cuckoodobot;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.List;

import static org.quartz.TriggerBuilder.*;

/**
 * @author Dmitry Tarasov
 *         Date: 03/17/2017
 *         Time: 20:03
 */
public class CuckoodoBot extends TelegramLongPollingBot {

    private String botToken;
    private Scheduler scheduler;
    private DataSource dataSource;

    private final static String[] ADD = {"add", "добавить"};
    private final static String[] LIST = {"list", "список", "все"};
    private final static String[] DONE = {"done", "готово", "готов", "сделаль"};
    private final static String[] DELETE = {"del", "удалить"};

    public CuckoodoBot(String botToken, Scheduler scheduler) {
        this.botToken = botToken;
        this.scheduler = scheduler;
        this.dataSource = new DataSource();
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

            if (startWith(ADD, messageText)) {
                addIssue(message);
            } else if (startWith(LIST, messageText)) {
                listIssue(message);
            } else if (startWith(DONE, messageText)) {
                doneIssue(message);
            } else if (startWith(DELETE, messageText)) {

            }
        }
    }

    private void addIssue(Message message) {
        String messageWithAssignee = deleteCommand(message.getText());
        String[] messageArr = messageWithAssignee.split(" ");
        String lastWord = messageArr[messageArr.length - 1];

        String assignee;
        String messageText;

        if (lastWord.startsWith("@")) {
            assignee = messageArr[messageArr.length - 1].substring(1);
            messageText = messageWithAssignee.substring(0, messageWithAssignee.length() - assignee.length() + 1);
        } else {
            assignee = "(all)";
            messageText = messageWithAssignee;
        }

        Issue issue = new Issue(message.getChatId(), messageText);
        issue.setAssignee(assignee);
        dataSource.addIssue(issue);
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
            sendMessage("Сделано!", message.getChatId());
        } else {
            sendMessage("Что-то пошло не так :(", message.getChatId());
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

        return done + idx + ". " + issue.getText() + " [" + issue.getAssignee() + "]";
    }

//      TODO
    //
//    private void addScheduledIssue(Issue issue) {
//
//        if (issue.schedulable()) {
//
//            issue = dataSource.addIssue(issue);
//
//            JobDetail jobDetail = JobBuilder.newJob(IssueJob.class)
//                    .withIdentity(new JobKey(issue.getId(), issue.getAssignee()))
//                    .build();
//
//            Trigger trigger = newTrigger()
//                    .withIdentity(new TriggerKey(issue.getId(), issue.getAssignee()))
//                    .startNow()
//                    .withSchedule(CronScheduleBuilder.cronSchedule(issue.getRepeat().getCron()))
//                    .build();
//
//            try {
//                scheduler.scheduleJob(jobDetail, trigger);
//            } catch (SchedulerException e) {
//                e.printStackTrace();
//            }
//        } else {
//            dataSource.addIssue(issue);
//        }
//    }


//    public class IssueJob implements Job {
//        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//            JobKey key = jobExecutionContext.getJobDetail().getKey();
//
//            Issue issue = dataSource.getIssue(key.getName(), key.getGroup());
//
//            SendMessage message = new SendMessage()
//                    .setChatId(issue.getAssignee())
//                    .setText(issue.getText());
//            try {
//                sendMessage(message);
//            } catch (TelegramApiException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
