package hackaton.cuckoodobot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author Dmitry Tarasov
 *         Date: 03/17/2017
 *         Time: 19:58
 */
public class Main {

    private final static String BOT_TOKEN = "BOT_TOKEN";
    private final static String DB_URL = "jdbc:h2:~/cuckoodo;CIPHER=AES"; //db file will be created in home dir with AES encryption
    private final static String DB_USER = "DB_USER";
    private final static String DB_PASSWORD = "DB_PASSWORD";
    private final static String DB_FILE_PASSWORD = "DB_FILE_PASSWORD";

    public static void main(String[] args) throws Exception {

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();

        ApiContextInitializer.init();

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        
        String dbUser = System.getenv(DB_USER);
        String dbPassword = System.getenv(DB_FILE_PASSWORD)+" "+System.getenv(DB_PASSWORD); //format is <file password><space><user password>        
        
        Flyway flyway = new Flyway();
        flyway.setDataSource(DB_URL, dbUser, dbPassword);
        flyway.migrate();
        
        try {
            telegramBotsApi.registerBot(new CuckoodoBot(System.getenv(BOT_TOKEN), scheduler,new DataSource(initDataSource(dbUser,dbPassword))));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    private static javax.sql.DataSource initDataSource(String user,String pass){
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(DB_URL);
        config.setUsername(user);
        config.setPassword(pass);
        config.setMaximumPoolSize(5);
        config.setAutoCommit(true);
        return new HikariDataSource(config);
    }
}
