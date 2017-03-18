package hakaton.cuckoobot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * @author Dmitry Tarasov
 *         Date: 03/17/2017
 *         Time: 19:58
 */
public class Main {
    
    private final static String API_KEY_ENV_VAR_NAME = "CUCKODOO_API_KEY";
    
        public static void main(String[] args) {

            ApiContextInitializer.init();

            TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
            try {
                telegramBotsApi.registerBot(new CuckoodoBot(System.getenv(API_KEY_ENV_VAR_NAME)));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
}
