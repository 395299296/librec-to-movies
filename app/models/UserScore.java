package models;

import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class UserScore extends Model {
	
	public User user;
	public Double score;
	public Double datetime;
	
	public UserScore(User user, Double score, Double datetime) {
		this.user = user;
		this.score = score;
		this.datetime = datetime;
	}
}
