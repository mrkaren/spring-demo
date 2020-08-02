package am.itspace.springdemo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class HeaderController {

    @ModelAttribute("username")
    public String username(@AuthenticationPrincipal Principal principal) {
        String username = null;
        if (principal != null) {
            username = principal.getName();
        }
        return username;
    }

}
