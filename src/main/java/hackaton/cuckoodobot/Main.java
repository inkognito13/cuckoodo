package hackaton.cuckoodobot;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * @author Dmitry Tarasov
 *         Date: 03/17/2017
 *         Time: 19:58
 */
public class Main {

    private final static String BOT_TOKEN = "BOT_TOKEN";

        public static void main(String[] args) throws Exception{

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();

        ApiContextInitializer.init();

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new CuckoodoBot(System.getenv(BOT_TOKEN), scheduler));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
