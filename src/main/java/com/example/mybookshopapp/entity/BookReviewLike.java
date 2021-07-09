package com.example.mybookshopapp.entity;

import com.example.mybookshopapp.entity.security.BookstoreUser;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_review_like")
public class BookReviewLike implements Serializable {

    @Id
    @SequenceGenerator(name = "seq_review_like", sequenceName = "seq_review_like", initialValue = 1001, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_review_like")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "review_id", referencedColumnName = "id")
    private BookReview bookReview;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private BookstoreUser user;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime time;

    @Column(columnDefinition = "SMALLINT")
    private Integer value;

    public BookReviewLike(BookstoreUser user, BookReview bookReview, Integer value) {
        this();
        this.user = user;
        this.bookReview = bookReview;
        this.value = value;
    }

    public BookReviewLike() {
        this.time = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BookReview getBookReview() {
        return bookReview;
    }

    public void setBookReview(BookReview bookReview) {
        this.bookReview = bookReview;
    }

    public BookstoreUser getUser() {
        return user;
    }

    public void setUser(BookstoreUser user) {
        this.user = user;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
