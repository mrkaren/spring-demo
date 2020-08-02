package am.itspace.springdemo.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequiredArgsConstructor
public class MainController {

    @Value("${file.upload.dir}")
    private String uploadDir;


    @GetMapping("/")
    public String homePage() {
        return "home";
    }

    @GetMapping("/about")
    public String aboutUsPage() {
        return "about";
    }

    @GetMapping("/loginPage")
    public String loginPage() {
        return "loginPage";
    }

//    @GetMapping("/successLogin")
//    public String successLogin(@AuthenticationPrincipal CurrentUser currentUser) {
//        if (currentUser == null) {
//            return "redirect:/";
//        }
//        User user = currentUser.getUser();
//        if (user.getRole() == Role.ADMIN) {
//            return "redirect:/admin";
//        } else {
//            return "redirect:/user";
//        }
//    }


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
