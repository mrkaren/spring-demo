package am.itspace.springdemo.controller;

import am.itspace.springdemo.model.Book;
import am.itspace.springdemo.model.User;
import am.itspace.springdemo.repository.BookRepository;
import am.itspace.springdemo.repository.UserRepository;
import am.itspace.springdemo.service.BookService;
import am.itspace.springdemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/book")
    public String bookPage(Model modelMap,
                           @RequestParam(name = "msg", required = false) String msg,
                           @RequestParam(value = "page", defaultValue = "1") int page,
                           @RequestParam(value = "size", defaultValue = "20") int size,
                           @RequestParam(value = "orderBy", defaultValue = "title") String orderBy,
                           @RequestParam(value = "order", defaultValue = "ASC") String order) {

        Sort sort = order.equals("ASC") ? Sort.by(Sort.Order.asc(orderBy)) : Sort.by(Sort.Order.desc(orderBy));

        PageRequest pageRequest = PageRequest.of(page - 1, size, sort);
        List<User> users = userRepository.findAll();
        Page<Book> books = bookRepository.findAll(pageRequest);
        int totalPages = books.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            modelMap.addAttribute("pageNumbers", pageNumbers);
        }
        modelMap.addAttribute("books", books);
        modelMap.addAttribute("users", users);

        modelMap.addAttribute("msg", msg);
        return "book";
    }

    @PostMapping("/book/save")
    public String add(@ModelAttribute Book book) {
        String msg = book.getId() > 0 ? "Book was updated" : "Book was added";
        bookService.save(book);
        return "redirect:/?msg=" + msg;
    }

    @GetMapping("/book/editPage")
    public String edit(@RequestParam("id") int id, Model model) {
        Optional<Book> one = bookService.findOne(id);
        if (!one.isPresent()) {
            return "redirect:/";
        }
        model.addAttribute("users", userService.findAll());
        model.addAttribute("book", one.get());
        return "editBook";
    }

    @GetMapping("/book/delete")
    public String edit(@RequestParam("id") int id) {
        bookService.deleteById(id);
        return "redirect:/";
    }

}
