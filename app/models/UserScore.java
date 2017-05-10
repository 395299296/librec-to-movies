package models;

import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class UserScore extends Model {
	
	public User user;
	public Double score;
	
	public UserScore(User user, Double score) {
		this.user = user;
		this.score = score;
	}
}
