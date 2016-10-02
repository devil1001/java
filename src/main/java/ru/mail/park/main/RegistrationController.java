package ru.mail.park.main;

//импорты появятся автоматически, если вы выбираете класс из выпадающего списка или же после alt+enter
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.model.UserProfile;
import ru.mail.park.services.AccountService;
import ru.mail.park.services.SessionService;

import javax.servlet.http.HttpSession;

/**
 * Created by Solovyev on 06/09/16.
 */

//Метка по которой спринг находит контроллер
@RestController
public class RegistrationController {


    private final AccountService accountService;
    private final SessionService sessionService;


    /**
     * Важное место. Мы не управляем жизненным циклом нашего класса. За нас это делает Spring. Аннотация говорит, что
     * зависимости должны быть разрешены с помощью спрингового контекста{@see ApplicationContext}(реестра классов). В нем могут присутствовать,
     * как наши сервисы(написанные нами), так и сервисы, предоставляемые спрингом.
     * @param accountService - подставляет наш синглтон
     */
    @Autowired
    public RegistrationController(AccountService accountService, SessionService sessionService) {
        this.accountService = accountService;
        this.sessionService = sessionService;
    }

    /**
     * Я ориентировался на {@see http://docs.technopark.apiary.io/} . В методе что-то сделано сильно не так, как в документации.
     * Что именно? Варианты ответа принимаются в slack {@see https://technopark-mail.slack.com/messages}
     * @param login - реквест параметр
     * @param password - =
     * @param email- =
     * @return - Возвращаем вместо id логин. Но это пока нормально.
     */
    @RequestMapping(path = "/api/user", method = RequestMethod.POST)
    public ResponseEntity login(@RequestParam(name = "login") String login,
                                @RequestParam(name = "password") String password,
                                @RequestParam(name = "email") String email) {
        //Инкапсулированная проверка на null и на пустоту. Выглядит гораздо более читаемо
        if (StringUtils.isEmpty(login)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{}");
        }
        final UserProfile existingUser = accountService.getUser(login);
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{}");
        }

        accountService.addUser(login, password, email);
        return ResponseEntity.ok(new SuccessResponse(login));
    }
    @RequestMapping(path = "/api/session", method = RequestMethod.GET)
    public ResponseEntity checkAuth(HttpSession sessionId) {
        if (sessionService.checkExists(sessionId.getId())) {
            return ResponseEntity.ok(new SuccessResponse(sessionService.returnLogin(sessionId.getId())));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}");
        }
    }

    @RequestMapping(path = "/api/session", method = RequestMethod.DELETE)
    public ResponseEntity deleteSession(HttpSession sessionId) {
        sessionService.deleteSession(sessionId.getId());
        return ResponseEntity.ok().body("{}");
    }

    @RequestMapping(path = "/api/session", method = RequestMethod.POST)
    public ResponseEntity auth(@RequestParam(name = "login") String login,
                               @RequestParam(name = "password") String password, HttpSession sessionId) {
        if(StringUtils.isEmpty(login)
                || StringUtils.isEmpty(password) ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{}");
        }
        final UserProfile user = accountService.getUser(login);
        if(user.getPassword().equals(password)) {
            sessionService.addSession(sessionId.getId(),user.getLogin());
            return ResponseEntity.ok(new SuccessResponse(user.getLogin()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{}");
    }

    // TODO: доделать вывод

    @RequestMapping(path = "/api/user/{id}", method = RequestMethod.GET)
    public ResponseEntity getInfo(@PathVariable("id") int id, HttpSession sessionId) {
        if (sessionService.checkExists(sessionId.getId())) {
            return ResponseEntity.ok().body(accountService.getUser(sessionService.returnLogin(sessionId.getId())));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}");
        }
    }
    // TODO: доделать вывод

    @RequestMapping(path = "/api/user/{id}", method = RequestMethod.POST)
    public ResponseEntity changeInfo(@PathVariable("id") int id, HttpSession sessionId, @RequestParam(name = "login") String login,
                                     @RequestParam(name = "password") String password,
                                     @RequestParam(name = "email") String email) {
        if (sessionService.checkExists(sessionId.getId())) {
            UserProfile temp = accountService.getUser(sessionService.returnLogin(sessionId.getId()));
                    if (temp.getId() == id) {
                        accountService.changeUser(sessionService.returnLogin(sessionId.getId()), login, password, email);
                        return ResponseEntity.ok().body("vse celikom");
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{}"); }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}");
        }
    }

    @RequestMapping(path = "/api/user/{id}", method = RequestMethod.POST)
    public ResponseEntity deleteUser(@PathVariable("id") int id, HttpSession sessionId) {
        if (sessionService.checkExists(sessionId.getId())) {
            UserProfile temp = accountService.getUser(sessionService.returnLogin(sessionId.getId()));
            if (temp.getId() == id) {
                accountService.removeUser(temp.getLogin());
                return ResponseEntity.ok().body("{}");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{}"); }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}");
        }
    }


    // объект класса будет автоматически преобразован в JSON при записи тела ответа
    private static final class SuccessResponse {
        private String login;

        private SuccessResponse(String login) {
            this.login = login;
        }

        //Функция необходима для преобразования см  https://en.wikipedia.org/wiki/Plain_Old_Java_Object
        @SuppressWarnings("unused")
        public String getLogin() {
            return login;
        }
    }

}
