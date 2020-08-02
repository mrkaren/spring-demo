package am.itspace.springdemo.controller;

import am.itspace.springdemo.model.Book;
import am.itspace.springdemo.model.Role;
import am.itspace.springdemo.model.User;
import am.itspace.springdemo.repository.BookRepository;
import am.itspace.springdemo.repository.UserRepository;
import am.itspace.springdemo.security.CurrentUser;
import am.itspace.springdemo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class MainController {

    @Value("${file.upload.dir}")
    private String uploadDir;

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @GetMapping("/")
    public String homePage(Model modelMap,
                           @RequestParam(name = "msg", required = false) String msg,
                           @RequestParam(value = "page", defaultValue = "1") int page,
                           @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        List<User> users = userRepository.findAll();
        Page<Book> books = bookRepository.findAll(pageRequest);
        int totalPages = books.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            modelMap.addAttribute("pageNumbers", pageNumbers);
        }

        modelMap.addAttribute("users", users);
        modelMap.addAttribute("books", books);
        modelMap.addAttribute("msg", msg);
        return "home";
    }

    @PostMapping("/addUser")
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
        String link = "http://localhost:8081/activate?email=" + user.getUsername() + "&token=" + user.getToken();
        emailService.send(user.getUsername(),
                "Welcome", "Dear " + user.getName() + " You have successfully registered. Please activate your account by clicking on: " + link);
        return "redirect:/?msg=User was added";
    }

    @GetMapping("/activate")
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
