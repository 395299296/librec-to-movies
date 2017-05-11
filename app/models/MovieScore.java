package models;

import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class MovieScore extends Model {
	
	public Movie movie;
	public Double score;
	public Double datetime;
	
	public MovieScore(Movie movie, Double score, Double datetime) {
		this.movie = movie;
		this.score = score;
		this.datetime = datetime;
	}
}
