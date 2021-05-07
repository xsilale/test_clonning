package com.example.MyBookShopApp.data.repositories;

import com.example.MyBookShopApp.data.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer> {

    List<Book> findBooksByAuthor_FirstName(String name);

//    @Query("FROM Book b JOIN Author a ON b.author.id = a.id")
    @Query("FROM Book")
    List<Book> customFindAllBooks();

    List<Book> findBookByUsers_Name(String name);
}
