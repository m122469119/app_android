package com.dym.film.entity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "FILM_REVIEW".
 */
public class FilmReview implements java.io.Serializable {

    private Long id;
    /** Not-null value. */
    private String FilmReviewId;
    private String FilmReviewDes;

    public FilmReview() {
    }

    public FilmReview(Long id) {
        this.id = id;
    }

    public FilmReview(Long id, String FilmReviewId, String FilmReviewDes) {
        this.id = id;
        this.FilmReviewId = FilmReviewId;
        this.FilmReviewDes = FilmReviewDes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getFilmReviewId() {
        return FilmReviewId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setFilmReviewId(String FilmReviewId) {
        this.FilmReviewId = FilmReviewId;
    }

    public String getFilmReviewDes() {
        return FilmReviewDes;
    }

    public void setFilmReviewDes(String FilmReviewDes) {
        this.FilmReviewDes = FilmReviewDes;
    }

}