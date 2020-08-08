package am.itspace.springdemo.controller;

import am.itspace.springdemo.dto.UserRequestDto;
import am.itspace.springdemo.model.User;
import am.itspace.springdemo.repository.UserRepository;
import am.itspace.springdemo.service.EmailService;
import am.itspace.springdemo.util.TextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
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
        String link = "http://localhost:8081/user/activate?email=" + userRequest.getUsername() + "&token=" + user.getToken();
        emailService.sendHtmlEmail(userRequest.getUsername(),
                "Welcome", user, link,"email/UserWelcomeMail.html", locale);
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
}
