package ru.mail.park;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/*
 Включает аутоконфигурацию на основе зависимостей
 и поиск компонентов Spring (@SpringBootConfiguration, @EnableAutoConfiguration)
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
