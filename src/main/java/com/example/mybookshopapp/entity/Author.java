package com.example.mybookshopapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "authors")
@ApiModel(description = "data model of author entity")
public class Author implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "author id generated by bd", position = 1)
    private Integer id;

    @ApiModelProperty(value = "name of author", example = "Bob Blaskovits", position = 2)
    private String name;

    @ApiModelProperty(value = "link to photo of author", example = "Bob Blaskovits", position = 3)
    private String photo;

    @ApiModelProperty(value = "mnemonic identifier of author", position = 5)
    private String slug;

    @ApiModelProperty(value = "description (biography, characteristics) of author", position = 4)
    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "author")
    @JsonIgnore
    private List<Book> bookList = new ArrayList<>();

    public Author(List<String> authors) {
        if (authors != null) {
            this.name = authors.toString();
        }
    }

    public Author() {
    }

    public List<Book> getBookList() {
        return bookList;
    }

    public void setBookList(List<Book> bookList) {
        this.bookList = bookList;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String firstName) {
        this.name = firstName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }


}
