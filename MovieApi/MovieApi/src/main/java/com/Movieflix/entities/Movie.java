package com.Movieflix.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer movieId;

    @Column(nullable = false , length = 200)
    @NotBlank(message = "Please Enter a valid title!!")
    private String title;

    @Column(nullable = false)
    @NotBlank(message = "Please Enter movie's director!!")
    private String director;

    @Column(nullable = false)
    @NotBlank(message = "Please Enter a movie's studio!!")
    private String studio;

    @ElementCollection
    @CollectionTable(name = "movie_cast")
    private Set<String> movieCast;

    @Column(nullable = false )
    private Integer releaseYear;

    @Column(nullable = false)
    @NotBlank(message = "Please Enter a movie's poster!!")
    private String poster;
}
