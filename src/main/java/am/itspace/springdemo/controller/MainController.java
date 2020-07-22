package am.itspace.springdemo.controller;

import am.itspace.springdemo.model.Book;
import am.itspace.springdemo.model.Role;
import am.itspace.springdemo.model.User;
import am.itspace.springdemo.repository.BookRepository;
import am.itspace.springdemo.repository.UserRepository;
import am.itspace.springdemo.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {

    @Value("${file.upload.dir}")
    private String uploadDir;

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String homePage(@AuthenticationPrincipal Principal principal, Model modelMap, @RequestParam(name = "msg", required = false) String msg) {
        String username = null;
        if (principal != null) {
            username = principal.getName();
        }
        List<User> users = userRepository.findAll();
        List<Book> books = bookRepository.findAll();
        modelMap.addAttribute("users", users);
        modelMap.addAttribute("books", books);
        modelMap.addAttribute("msg", msg);
        modelMap.addAttribute("username", username);
        return "home";
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute User user, @RequestParam("image") MultipartFile file) throws IOException {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if (byUsername.isPresent()) {
            return "redirect:/?msg=User already exists";
        }
        String name = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File image = new File(uploadDir, name);
        file.transferTo(image);
        user.setProfilePic(name);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/?msg=User was added";
    }

    @GetMapping("/deleteUser")
    public String deleteUser(@RequestParam("id") int id) {
        userRepository.deleteById(id);
        String msg = "User was removed";
        return "redirect:/?msg=" + msg;
    }

    @GetMapping("/about")
    public String aboutUsPage() {
        return "about";
    }

    @GetMapping("/loginPage")
    public String loginPage() {
        return "loginPage";
    }

    @GetMapping("/successLogin")
    public String successLogin(@AuthenticationPrincipal CurrentUser currentUser) {
        if (currentUser == null) {
            return "redirect:/";
        }
        User user = currentUser.getUser();
        if (user.getRole() == Role.ADMIN) {
            return "redirect:/admin";
        } else {
            return "redirect:/user";
        }
    }


    @GetMapping(
            value = "/image",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public @ResponseBody
    byte[] getImage(@RequestParam("name") String imageName) throws IOException {
        InputStream in = new FileInputStream(uploadDir + File.separator + imageName);
        return IOUtils.toByteArray(in);
    }
}
