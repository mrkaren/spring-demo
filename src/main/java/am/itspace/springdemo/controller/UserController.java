package am.itspace.springdemo.controller;

import am.itspace.springdemo.model.User;
import am.itspace.springdemo.repository.UserRepository;
import am.itspace.springdemo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
    public String addUser(@ModelAttribute User user, @RequestParam("image") MultipartFile file) throws IOException {
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            return "redirect:/?msg=Password and Confirm Password does not match!";
        }

        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if (byUsername.isPresent()) {
            return "redirect:/?msg=User already exists";
        }
        String name = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File image = new File(uploadDir, name);
        file.transferTo(image);
        user.setProfilePic(name);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(false);
        user.setToken(UUID.randomUUID().toString());
        userRepository.save(user);
        String link = "http://localhost:8081/user/activate?email=" + user.getUsername() + "&token=" + user.getToken();
        emailService.send(user.getUsername(),
                "Welcome", "Dear " + user.getName() + " You have successfully registered. Please activate your account by clicking on: " + link);
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
