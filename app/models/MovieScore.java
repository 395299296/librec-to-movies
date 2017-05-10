package models;

import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class MovieScore extends Model {
	
	public Movie movie;
	public Double score;
	
	public MovieScore(Movie movie, Double score) {
		this.movie = movie;
		this.score = score;
	}
}
