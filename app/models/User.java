package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class User extends Model {
	
	public Long user_id;
	public int age;
	public String gender;
	public String profession;
	public String zipcode;
	
	@OneToMany(mappedBy="movie", cascade=CascadeType.ALL)
	public List<MovieScore> ratings;
	
	public static ArrayList<User> allUsers = new ArrayList<>();
	
	public User(Long user_id) {
		this.user_id = user_id;
	}
	
	public User(Long user_id, int age, String gender, String profession, String zipcode) {
		this.user_id = user_id;
		this.age = age;
		this.gender = gender;
        this.profession = profession;
        this.zipcode = zipcode;
        this.ratings = new ArrayList<>();
	}
	
	public static User getUser(Long user_id) {
		User user = findUser(user_id);
		if (user != null)
			return user;
		
        return new User(user_id);
	}
	
	public static User findUser(Long user_id) {
        // User user = User.find("user_id", user_id).first();
		for (User user:allUsers) {
			if (user.user_id.equals(user_id))
				return user;
		}
		return null;
	}
	
	public User	addMovie(Movie movie, Double score) {
		MovieScore ms = new MovieScore(movie, score);
		this.ratings.add(ms);
		return this;
	}
	
	public MovieScore getUserRating(Long movie_id) {
		for (MovieScore ms:ratings) {
			if (ms.movie.movie_id.equals(movie_id)) {
				return ms;
			}
		}
		return null;
	}
	
	public int setUserRating(Long movie_id, Double score) {
		// Check to see if a rating is in the database.
		for (MovieScore ms:ratings) {
			if (ms.movie.movie_id.equals(movie_id)) {
				ms.score = score;
				return 0;
			}
		}
		Movie movie = Movie.getMovie(movie_id);
		movie.setUserRating(this.user_id, score);
		movie.calcAvgRating();
		addMovie(movie, score);
		return 1;
	}
}
