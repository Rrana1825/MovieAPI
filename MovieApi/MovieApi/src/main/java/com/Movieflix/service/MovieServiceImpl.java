package com.Movieflix.service;

import com.Movieflix.dto.MovieDto;
import com.Movieflix.dto.MoviePageResponse;
import com.Movieflix.entities.Movie;
import com.Movieflix.expections.FileExistsException;
import com.Movieflix.expections.MovieNotFoundException;
import com.Movieflix.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Pageable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
@Service
public class MovieServiceImpl implements MovieService{

    private final MovieRepository movieRepository;
    private final FileService fileService;

    @Value(("${project.poster"))
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(MovieRepository movieRepository,FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        //1. Upload the file
        if(Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))){
            throw new FileExistsException("File Already Exists !! Create a new one.");
        }
        String uploadedFileName = fileService.uploadFile(path,file);

        //2. To set the value of field 'poster' as a filename
        movieDto.setPoster(uploadedFileName);

        //3. Map Dto to movie object
        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        //4. To save the movie object -> saved movie object
        Movie savedMovie = movieRepository.save(movie);

        /* 5. Generate the posterUrl */
        String posterUrl = baseUrl + "/file/" + uploadedFileName;

        //6. Map movie object to DTO object and return it
        MovieDto response = new MovieDto(
                savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );

        return response;
    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        //1. check the data in DB and if exists, fetch the data of given ID
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie Not Found with id : "+movieId));

        //2. generate posterUrl
        String posterUrl = baseUrl + " " + movie.getPoster();

        //3. Map to MovieObj and send it
        MovieDto response = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );

        return response;
    }

    @Override
    public List<MovieDto> getAllMovies() {
        //1. fetch all data from DB
        List<Movie> movies = movieRepository.findAll();
        List<MovieDto> movieDtos = new ArrayList<>();

        //2. Iterate through the list , generate posterUrl for each movie obj,
        //   and map to MovieObj obj
        for(Movie movie : movies){
            String posterUrl = baseUrl +"/file/" + movie.getPoster();
            MovieDto response = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(response);
        }

        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        //1. Check if the movie Exists or not
        Movie mv = movieRepository.findById(movieId)
                .orElseThrow(()-> new MovieNotFoundException("Movie Not Found with id : "+movieId));

        //2. if the field is null , do nothing
        //   else -> delete the existing file associate with the record and
        //   upload a new file
        String fileName = mv.getPoster();
        if(file != null){
            Files.deleteIfExists(Paths.get(path+File.separator+fileName));
            fileName = fileService.uploadFile(path,file);
        }

        //3. Set movieDto's poster value according to step2
        movieDto.setPoster(fileName);

        //4. map it to MovieDto object
        Movie movie = new Movie(
                mv.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        //5.save the movie object
        Movie updatedMovie = movieRepository.save(movie);

        //6. generate PosterUrl for it
        String posterUrl = baseUrl + "/file/" +fileName;

        //7. movie to MovieDto object
        MovieDto response = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );

        return response;
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {
        //1. Check if the file exists or not
        Movie mv = movieRepository.findById(movieId)
                .orElseThrow(()-> new MovieNotFoundException("Movie Not Found with id : "+movieId));
        Integer id = mv.getMovieId();

        //2. delete the file associated with this object
        Files.deleteIfExists(Paths.get(path+File.separator+mv.getPoster()));

        //3. delete the movie Object
        movieRepository.delete(mv);
        return "Movie deleted with Id : "+id;

    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = (Pageable) PageRequest.of(pageNumber,pageSize);
        Page<Movie> moviePage =  movieRepository.findAll((org.springframework.data.domain.Pageable) pageable);
        List<Movie> movies = moviePage.getContent();

        List<MovieDto> movieDtos =new ArrayList<>();
        for(Movie movie : movies){
            String posterUrl = baseUrl +"/file/" + movie.getPoster();
            MovieDto response = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(response);
        }

        return new MoviePageResponse(movieDtos ,pageNumber,pageSize,
                moviePage.getTotalPages(), (int) moviePage.getTotalElements(),
                moviePage.isLast());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String dir) {
        Sort sort = sortBy.equalsIgnoreCase("asc")?Sort.by(dir).ascending()
                                                              :Sort.by(dir).descending();


        Pageable pageable = (Pageable) PageRequest.of(pageNumber,pageSize,sort);
        Page<Movie> moviePage =  movieRepository.findAll((org.springframework.data.domain.Pageable) pageable);
        List<Movie> movies = moviePage.getContent();

        List<MovieDto> movieDtos =new ArrayList<>();
        for(Movie movie : movies){
            String posterUrl = baseUrl +"/file/" + movie.getPoster();
            MovieDto response = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(response);
        }
        return new MoviePageResponse(movieDtos ,pageNumber,pageSize,
                moviePage.getTotalPages(), (int) moviePage.getTotalElements(),
                moviePage.isLast());
    }
}
