package am.itspace.springdemo.controller;

import am.itspace.springdemo.dto.UserRequestDto;
import am.itspace.springdemo.model.User;
import am.itspace.springdemo.repository.UserRepository;
import am.itspace.springdemo.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.generic.LOOKUPSWITCH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @Value("${file.upload.dir}")
    private String uploadDir;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @GetMapping("/user")
    public String userPage(ModelMap modelMap) {
        List<User> users = userRepository.findAll();
        modelMap.addAttribute("users", users);
        return "user";
    }

    @PostMapping("/user/add")
    public String addUser(@ModelAttribute @Valid UserRequestDto userRequest, BindingResult br, ModelMap modelMap, @RequestParam("image") MultipartFile file, Locale locale) throws IOException, MessagingException {
        if (br.hasErrors()) {
            List<User> users = userRepository.findAll();
            modelMap.addAttribute("users", users);
            return "user";
        }
//        if (!TextUtil.VALID_EMAIL_ADDRESS_REGEX.matcher(userRequest.getUsername()).find()) {
//            return "redirect:/userRequestDto?msg=Email does not valid";
//        }

        if (!userRequest.getPassword().equals(userRequest.getConfirmPassword())) {
            return "redirect:/?msg=Password and Confirm Password does not match!";
        }

        Optional<User> byUsername = userRepository.findByUsername(userRequest.getUsername());
        if (byUsername.isPresent()) {
            return "redirect:/?msg=User already exists";
        }
        String profilePic = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File image = new File(uploadDir, profilePic);
        file.transferTo(image);
        User user = User.builder()
                .name(userRequest.getName())
                .surname(userRequest.getSurname())
                .username(userRequest.getUsername())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .active(false)
                .token(UUID.randomUUID().toString())
                .profilePic(profilePic)
                .build();
        userRepository.save(user);

        log.debug("user with {} email was registered", user.getUsername());

        String link = "http://localhost:8081/user/activate?email=" + userRequest.getUsername() + "&token=" + user.getToken();
        emailService.sendHtmlEmail(userRequest.getUsername(),
                "Welcome", user, link, "email/UserWelcomeMail.html", locale);
        return "redirect:/?msg=User was added";
    }

    @GetMapping("/user/delete")
    public String deleteUser(@RequestParam("id") int id) {
        userRepository.deleteById(id);
        String msg = "User was removed";
        return "redirect:/?msg=" + msg;
    }

    @GetMapping("/user/activate")
    public String activate(@RequestParam("email") String email, @RequestParam("token") String token) {
        Optional<User> byUsername = userRepository.findByUsername(email);
        if (byUsername.isPresent()) {
            User user = byUsername.get();
            if (user.getToken().equals(token)) {
                user.setActive(true);
                user.setToken("");
                userRepository.save(user);
                return "redirect:/?msg=User was activate, please login";
            }
        }
        return "redirect:/?msg=Something went wrong. Please try again";
    }

    @GetMapping("/user/forgotPassword")
    public String forgotPass(@RequestParam("email") String email) {
        Optional<User> byUsername = userRepository.findByUsername(email);
        if (byUsername.isPresent() && byUsername.get().isActive()) {
            User user = byUsername.get();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            userRepository.save(user);
            String link = "http://localhost:8081/user/forgotPassword/reset?email=" + user.getUsername() + "&token=" + token;
            emailService.send(user.getUsername(), "RESET password", "Dear user, please open this link in order to reset your password: " + link);
        }
        return "redirect:/";
    }

    @GetMapping("/user/forgotPassword/reset")
    public String forgotPassReset(ModelMap modelMap, @RequestParam("email") String email, @RequestParam("token") String token) {
        Optional<User> byUsername = userRepository.findByUsername(email);
        if (byUsername.isPresent() && byUsername.get().getToken().equals(token)) {
            modelMap.addAttribute("email", byUsername.get().getUsername());
            modelMap.addAttribute("token", byUsername.get().getToken());
            return "changePassword";
        }
        return "redirect:/";
    }

    @PostMapping("/user/forgotPassword/change")
    public String changePassword(@RequestParam("email") String email, @RequestParam("token") String token,
                                 @RequestParam("password") String password,
                                 @RequestParam("repeatPassword") String repeatPassword) {
        Optional<User> byUsername = userRepository.findByUsername(email);
        if (byUsername.isPresent()) {
            User user = byUsername.get();
            if (user.getToken().equals(token) && password.equals(repeatPassword)) {
                user.setToken("");
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                //
                return "redirect:/?msg=Your password changed!";
            }
        }
        return "redirect:/";
    }
}
