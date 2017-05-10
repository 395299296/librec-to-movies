package models;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.*;
import play.db.jpa.*;

@Entity
public class Movie extends Model {
	
	public Long movie_id;
	public String title;
	public String released_at;
	public String imdb_url;
	public Double avg_rating;
	
	@OneToMany(mappedBy="user", cascade=CascadeType.ALL)
	public List<UserScore> ratings;
	
	public static ArrayList<Movie> allMovies = new ArrayList<>();
	
	public Movie(Long movie_id) {
		// TODO Auto-generated constructor stub
		this.movie_id = movie_id;
		this.avg_rating = 0.0;
	}
	
	@SuppressWarnings("deprecation")
	public Movie(Long movie_id, String title, String released_at, String imdb_url) {
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
        this.imdb_url = imdb_url;
        this.avg_rating = 0.0;
        this.ratings = new ArrayList<>();
	}

	public static Movie getMovie(Long movie_id) {
        // Movie movie = Movie.find("movie_id", movie_id).first();
		for (Movie movie:allMovies) {
			if (movie.movie_id.equals(movie_id))
				return movie;
		}
        return new Movie(movie_id);
	}
	
	public Movie addUser(User user, Double score) {
		UserScore us = new UserScore(user, score);
		this.ratings.add(us);
		return this;
	}
	
	public int setUserRating(Long user_id, Double score) {
		// Check to see if a rating is in the database.
		for (UserScore us:ratings) {
			if (us.user.user_id.equals(user_id)) {
				us.score = score;
				return 0;
			}
		}
		User user = User.getUser(user_id);
		addUser(user, score);
		return 1;
	}
	
	public void calcAvgRating() {
		Double totalScore = 0.0;
		for (UserScore us:ratings) {
			totalScore += us.score;
		}
		this.avg_rating = totalScore / ratings.size();
	}
}
