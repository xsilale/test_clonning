package com.example.mybookshopapp.controller;

import com.example.mybookshopapp.service.BookService;
import com.example.mybookshopapp.entity.Book;
import com.example.mybookshopapp.entity.BookUser;
import com.example.mybookshopapp.entity.BookUserType;
import com.example.mybookshopapp.repository.BookRepository;
import com.example.mybookshopapp.repository.BookUserRepository;
import com.example.mybookshopapp.entity.security.BookstoreUser;
import com.example.mybookshopapp.entity.security.BookstoreUserDetails;
import com.example.mybookshopapp.repository.security.BookstoreUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping("/books")
public class BookShopCartController {

    @ModelAttribute(name = "bookCart")
    public List<Book> bookCart() {
        return new ArrayList<>();
    }

    private static final String IS_CART_EMPTY = "isCartEmpty";

    private final BookRepository bookRepository;
    private final BookService bookService;
    private final BookstoreUserRepository bookstoreUserRepository;
    private final BookUserRepository bookUserRepository;

    @Autowired
    public BookShopCartController(BookRepository bookRepository, BookService bookService, BookstoreUserRepository bookstoreUserRepository, BookUserRepository bookUserRepository) {
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.bookstoreUserRepository = bookstoreUserRepository;
        this.bookUserRepository = bookUserRepository;
    }

    @GetMapping("/cart")
    public String handleCartRequest(@AuthenticationPrincipal BookstoreUserDetails user,
                                    @CookieValue(value = "userHash", required = false) String userHash,
                                    HttpServletResponse response,
                                    Model model) {

        if (user != null) {
            BookstoreUser bookstoreUserByHash = bookstoreUserRepository.findBookstoreUserByHash(userHash);
            if (userHash != null && !userHash.equals("") && bookstoreUserByHash != null) {
                List<Book> booksFromCookieUser = bookRepository.findBooksByUser(bookstoreUserByHash);
                for (Book book : booksFromCookieUser) {
                    BookUser bookUserFromCookieUser = bookUserRepository.findByBookAndUser(book, bookstoreUserByHash);
                    BookUser bookUserFromCurrentUser = bookUserRepository.findByBookAndUser(book, user.getBookstoreUser());
                    if (bookUserFromCurrentUser != null) {
                        bookUserRepository.delete(bookUserFromCookieUser);
                    } else {
                        bookUserFromCookieUser.setUser(user.getBookstoreUser());
                        bookUserRepository.save(bookUserFromCookieUser);
                    }
                }
                bookstoreUserRepository.delete(bookstoreUserByHash);
                response.addCookie(new Cookie("cartContents", ""));
            }
            List<Book> booksByUser = bookRepository.findBooksByUserAndType(user.getBookstoreUser(), BookUserType.CART);
            model.addAttribute(IS_CART_EMPTY, booksByUser.isEmpty());
            model.addAttribute("bookCart", booksByUser);
            return "cart";
        }
        if (userHash == null || userHash.equals("")) {
            model.addAttribute(IS_CART_EMPTY, true);
        } else {
            BookstoreUser bookstoreUserByHash = bookstoreUserRepository.findBookstoreUserByHash(userHash);
            List<Book> booksFromCookieUser = bookRepository.findBooksByUserAndType(bookstoreUserByHash, BookUserType.CART);
            model.addAttribute(IS_CART_EMPTY, booksFromCookieUser.isEmpty());
            model.addAttribute("bookCart", booksFromCookieUser);
        }
        return "cart";
    }

    @PostMapping("/changeBookStatus/cart/remove/{slug}")
    public String handleRemoveBookFromCartRequest(@AuthenticationPrincipal BookstoreUserDetails user,
                                                  @PathVariable("slug") String slug,
                                                  @CookieValue(name = "userHash", required = false) String userHash,
                                                  Model model) {
        if (user != null) {
            bookService.removeBookFromCartBySlag(user.getBookstoreUser(), slug);
            return "redirect:/books/cart";
        }
        if (userHash != null && !userHash.equals("")) {
            BookstoreUser bookstoreUserByHash = bookstoreUserRepository.findBookstoreUserByHash(userHash);
            bookService.removeBookFromCartBySlag(bookstoreUserByHash, slug);
            model.addAttribute(IS_CART_EMPTY, bookRepository.findBooksByUserAndType(bookstoreUserByHash, BookUserType.CART));
        } else {
            model.addAttribute(IS_CART_EMPTY, true);
        }
        return "redirect:/books/cart";
    }

    @PostMapping("/changeBookStatus/{slug}")
    public String handleChangeBookStatus(@AuthenticationPrincipal BookstoreUserDetails user,
                                         @PathVariable("slug") String slug,
                                         @CookieValue(name = "userHash", required = false) String userHash,
                                         HttpServletResponse response) {
        if (user != null) {
            bookService.changeBookStatusToCartForUser(BookUserType.CART, slug, user.getBookstoreUser());
            return "redirect:/books/" + slug;
        }
        if (userHash != null && !userHash.equals("")) {
            BookstoreUser bookstoreUserByHash = bookstoreUserRepository.findBookstoreUserByHash(userHash);
            bookService.changeBookStatusToCartForUser(BookUserType.CART, slug, bookstoreUserByHash);
        } else {
            BookstoreUser defaultUser = new BookstoreUser();
            defaultUser.setHash(UUID.randomUUID().toString());
            defaultUser = bookstoreUserRepository.save(defaultUser);
            bookService.changeBookStatusForUser(bookRepository.findBookBySlug(slug), defaultUser, BookUserType.CART);

            Cookie cookie = new Cookie("userHash", defaultUser.getHash());
            response.addCookie(cookie);
        }
        return "redirect:/books/" + slug;
    }

}
