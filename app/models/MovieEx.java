package models;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.*;
import play.db.jpa.*;

@Entity
public class MovieEx extends Movie {
		
	public String imdb_url;
	public String summary;
	public String subtext;
	
	@OneToMany(mappedBy="user", cascade=CascadeType.ALL)
	public List<UserScore> ratings;
	
	public MovieEx(Long movie_id) {
		// TODO Auto-generated constructor stub
		super(movie_id);
	}
	
	public MovieEx(Long movie_id, String title, String released_at, String imdb_url, String summary, String subtext) {
		super(movie_id, title, released_at);
        this.imdb_url = imdb_url;
        this.summary = summary;
        this.subtext = subtext;
        this.ratings = new ArrayList<>();
	}
	
	public MovieEx addUser(User user, Double score, Double datetime) {
		UserScore us = new UserScore(user, score, datetime);
		this.ratings.add(us);
		return this;
	}
	
	public int setUserRating(Long user_id, Double score, Double datetime) {
		// Check to see if a rating is in the database.
		for (UserScore us:ratings) {
			if (us.user.user_id.equals(user_id)) {
				us.score = score;
				return 0;
			}
		}
		User user = User.getUser(user_id);
		addUser(user, score, datetime);
		return 1;
	}
	
	public void calcAvgRating() {
		if (ratings.size() != 0) {
			Double totalScore = 0.0;
			for (UserScore us:ratings) {
				totalScore += us.score;
			}
			BigDecimal bg = new BigDecimal(totalScore / ratings.size());
			this.avg_rating = bg.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
			
			Double totalDatetime = 0.0;
			for (UserScore us:ratings) {
				totalDatetime += us.datetime;
			}
			this.avg_datetime = totalDatetime / ratings.size();
		}
	}
}
