package models;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.*;
import play.db.jpa.*;

@Entity
public class Movie extends Model {

	public Long movie_id;
	public String title;
	public String released_at;
	public Double avg_rating;
	public Double avg_datetime;
	
	public static ArrayList<Movie> allMovies = new ArrayList<>();
	
	public Movie(Long movie_id) {
		this.movie_id = movie_id;
		this.avg_rating = 0.0;
		this.avg_datetime = 0.0;
	}
	
	@SuppressWarnings("deprecation")
	public Movie(Long movie_id, String title, String released_at) {
		this.movie_id = movie_id;
        this.title = title;
        try {
        	Date date = new Date(released_at);
	        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	        this.released_at = sdf.format(date);
		} catch (Exception e) {
			// TODO: handle exception
			this.released_at = "";
		}
        this.avg_rating = 0.0;
        this.avg_datetime = 0.0;
	}
	
	public Movie clone() {
		Movie newMovie = new Movie(this.movie_id);
		newMovie.title = this.title;
		newMovie.released_at = this.released_at;
		newMovie.avg_rating = this.avg_rating;
		newMovie.avg_datetime = this.avg_datetime;
		return newMovie;
	}
	
	public static Movie getMovie(Long movie_id) {
        // Movie movie = Movie.find("movie_id", movie_id).first();
		Movie movie = findMovie(movie_id);
		if (movie != null)
			return movie;
		
        return new Movie(movie_id);
	}
	
	public static Movie findMovie(Long movie_id) {
		for (Movie movie:allMovies) {
			if (movie.movie_id.equals(movie_id))
				return movie;
		}
		
		return null;
	}
}
