package models;

import java.util.*;
import javax.persistence.*;
import play.db.jpa.*;

@Entity
public class Rating extends Model {
	
	public Long user_id;
	public Long movie_id;
	public Double rating;
	
	public static ArrayList<Rating> allRatings = new ArrayList<>();
	
	public Rating(Long user_id, Long movie_id, Double rating) {
		this.user_id = user_id;
		this.movie_id = movie_id;
		this.rating = rating;
	}
	
	public static List<Map> findUserWith(Long user_id) {
        return Rating.find(
            "select new map(m.movie_id as movie_id, m.title as title, r.rating as rating) from Rating r join Movie as m on r.movie_id = m.movie_id where r.user_id = ?",
            user_id
        ).fetch();
    }
	
}
