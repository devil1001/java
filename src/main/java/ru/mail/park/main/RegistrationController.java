package ru.mail.park.main;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.model.UserProfile;
import ru.mail.park.services.AccountService;
import ru.mail.park.services.SessionService;

import javax.servlet.http.HttpSession;

@RestController
public class RegistrationController {


    private final AccountService accountService;
    private final SessionService sessionService;


    @Autowired
    public RegistrationController(AccountService accountService, SessionService sessionService) {
        this.accountService = accountService;
        this.sessionService = sessionService;
    }

    @RequestMapping(path = "/api/user", method = RequestMethod.POST)
    public ResponseEntity login(@RequestBody RegistRequest body) {


        final String login = body.getLogin();
        final String password = body.getPassword();
        final  String email = body.getEmail();


        if (StringUtils.isEmpty(login)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{Одно из полей пусто}");
        }
        final UserProfile existingUser = accountService.getUser(login);
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{Такой пользователь существует}");
        }

        accountService.addUser(login, password, email);
        return ResponseEntity.ok(new SuccessResponse(login));
    }
    @RequestMapping(path = "/api/session", method = RequestMethod.GET)
    public ResponseEntity checkAuth(HttpSession sessionId) {
        if (sessionService.checkExists(sessionId.getId())) {
            return ResponseEntity.ok(new SuccessResponse(sessionService.returnLogin(sessionId.getId())));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{Вы не авторизованы!}");
        }
    }

    @RequestMapping(path = "/api/session", method = RequestMethod.DELETE)
    public ResponseEntity deleteSession(HttpSession sessionId) {
        if(sessionService.checkExists(sessionId.getId())) {
            sessionService.deleteSession(sessionId.getId());
            return ResponseEntity.ok().body("{Вы больше не авторизованы!}");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{Вы не были авторизованы!}");
    }

    @RequestMapping(path = "/api/session", method = RequestMethod.POST)
    public ResponseEntity auth(@RequestBody AuthRequest body, HttpSession sessionId) {
        final String login = body.getLogin();
        final String password = body.getPassword();
        if(StringUtils.isEmpty(login)
                || StringUtils.isEmpty(password) ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{Неправильный запрос}");
        }
        final UserProfile user = accountService.getUser(login);
        if(user.getPassword().equals(password)) {
            sessionService.addSession(sessionId.getId(),user.getLogin());
            return ResponseEntity.ok(new SuccessResponse(user.getLogin()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{Возникла ошибка}");
    }

    @RequestMapping(path = "/api/user/{id}", method = RequestMethod.GET)
    public ResponseEntity getInfo(@PathVariable("id") int id, HttpSession sessionId) {
        if (sessionService.checkExists(sessionId.getId())) {
            return ResponseEntity.ok().body(accountService.getUser(sessionService.returnLogin(sessionId.getId())));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{Вы не авторизованы!}");
        }
    }


    @RequestMapping(path = "/api/user/{id}", method = RequestMethod.PUT)
    public ResponseEntity changeInfo(@PathVariable("id") int id, HttpSession sessionId, @RequestBody RegistRequest body) {
        final String login = body.getLogin();
        final String email = body.getEmail();
        final String password = body.getPassword();
        if (sessionService.checkExists(sessionId.getId())) {
            UserProfile temp = accountService.getUser(sessionService.returnLogin(sessionId.getId()));
                    if (temp.getId() == id) {
                        accountService.changeUser(sessionService.returnLogin(sessionId.getId()), login, password, email);
                        return ResponseEntity.ok().body(body);
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{}"); }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{Вы не авторизованы!}");
        }
    }

    @RequestMapping(path = "/api/user/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteUser(@PathVariable("id") int id, HttpSession sessionId) {
        if (sessionService.checkExists(sessionId.getId())) {
            UserProfile temp = accountService.getUser(sessionService.returnLogin(sessionId.getId()));
            if (temp.getId() == id) {
                accountService.removeUser(temp.getLogin());
                return ResponseEntity.ok().body("{}");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{}"); }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{Вы не авторизованы!}");
        }
    }


    private  static final class RegistRequest {
        private String login;
        private String email;
        private String password;

        @JsonCreator
        private RegistRequest(@JsonProperty("login") String login,
                              @JsonProperty("email") String email,
                              @JsonProperty("password") String password) {
            this.login = login;
            this.email = email;
            this.password = password;
        }

        public String getLogin() {
            return login;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }

    private static final class AuthRequest {
        private String login;
        private String password;
        @JsonCreator
        private  AuthRequest(@JsonProperty("login") String login,
                             @JsonProperty("password") String password){
            this.login = login;
            this.password = password;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    }


    private static final class SuccessResponse {
        private String login;

        private SuccessResponse(String login) {
            this.login = login;
        }

        public String getLogin() {
            return login;
        }
    }

}
