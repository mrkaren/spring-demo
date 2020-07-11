package am.itspace.springdemo.controller;

import am.itspace.springdemo.model.Book;
import am.itspace.springdemo.model.User;
import am.itspace.springdemo.repository.BookRepository;
import am.itspace.springdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @PostMapping("/saveBook")
    public String add(@ModelAttribute Book book) {
        String msg = book.getId() > 0 ? "Book was updated" : "Book was added";
        bookRepository.save(book);
        return "redirect:/?msg=" + msg;
    }

    @GetMapping("/editBook")
    public String edit(@RequestParam("id") int id, Model model) {
        Book book = bookRepository.getOne(id);
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("book", book);
        return "editBook";
    }

}
