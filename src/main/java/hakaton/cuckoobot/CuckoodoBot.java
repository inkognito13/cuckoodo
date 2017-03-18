package hakaton.cuckoobot;

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

    public CuckoodoBot(String botToken, Scheduler scheduler) {
        this.botToken = botToken;
        this.scheduler = scheduler;
        this.dataSource = new DataSource();
    }

    public String getBotToken() {
        return botToken;
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            
            Message message = update.getMessage();

            String messageText = message.getText();

            

            if (messageText.contains("добавить")){
                String reminderText = messageText.replaceFirst("добавить","").trim();
                System.out.println("Adding with text="+reminderText);
                Issue issue = new Issue();
                issue.setOwner(message.getChatId().toString());
                issue.setText(reminderText);
                dataSource.addIssue(issue);
                try {
                    sendMessage(
                            new SendMessage()
                                    .setChatId(message.getChatId())
                                    .setText("Успешно добавлено напоминание "+issue.getText())
                    );
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }else if (messageText.contains("список")){
                
                String ownerId = message.getChatId().toString();
                
                List<Issue> issues = dataSource.getIssueForGroup(ownerId);

                try {
                    sendMessage(
                            new SendMessage()
                                    .setChatId(message.getChatId())
                                    .setText(formatIssuesList(issues)
                                    )
                    );
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private String formatIssuesList(List<Issue> issues){
        StringBuilder builder = new StringBuilder();
        for (int i=1;i<=issues.size();i++){
            builder.append(i+") ");
            builder.append(issues.get(i-1).getText());
            builder.append("\n\r");
        }
        return builder.toString();
    }
    
    
    private void addScheduledIssue(Issue issue){
        
        if (issue.schedulable()){
            
            issue = dataSource.addIssue(issue);
            
            JobDetail jobDetail = JobBuilder.newJob(IssueJob.class)
                    .withIdentity(new JobKey(issue.getId(),issue.getOwner()))
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(new TriggerKey(issue.getId(),issue.getOwner()))
                    .startNow()
                    .withSchedule(CronScheduleBuilder.cronSchedule(issue.getRepeat().getCron()))
                    .build();
            
            try {
                scheduler.scheduleJob(jobDetail,trigger);
            }catch (SchedulerException e){
                e.printStackTrace();
            }    
        }else{
            dataSource.addIssue(issue);
        }
    }

    public String getBotUsername() {
        return "cuckodoobot";
    }

    public class IssueJob implements Job {
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            JobKey key = jobExecutionContext.getJobDetail().getKey();
            
            Issue issue = dataSource.getIssue(key.getName(),key.getGroup());

            SendMessage message = new SendMessage()
                    .setChatId(issue.getOwner())
                    .setText(issue.getText());
            try {
                sendMessage(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
