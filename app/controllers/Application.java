package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import manage.RecommendMgr;
import models.*;

public class Application extends Controller {

    public static void index() {
    	// show recommended movie list
    	List<Movie> recommend_list = new ArrayList<>();
    	String user_id = session.get("user_id");
    	if (user_id == null)
    		recommend_list = RecommendMgr.getInstance().getDefaultItemList();
    	else
    		recommend_list = RecommendMgr.getInstance().getFilterItemList(user_id);
        // show recent release movie list
        List<Movie> recent_list = RecommendMgr.getInstance().getRecentReleaseList();
        // show score list top 10
        List<Movie> score_list = RecommendMgr.getInstance().getScoreTopList();
        
        render("@index", recommend_list, recent_list, score_list);
    }

    /*
     * Login page.
     */
    public static void login() {
        render();
    }
    
    /*
     * Login in by user_id
     */
    public static void is_logged_in(Long user_id) {
    	// Check to see if a user is in the database.
    	User user = User.findUser(user_id);
    	if (user != null) {
	    	session.put("user_id", user_id);
	        flash("message", "Successfully logged in!");
    	} else {
    		flash("message", "Hey! You're user_id is incorrect.");
    	}
    	redirect("/");
	}

    /*
     * Logout to an existing account.
     */
    public static void logout() {
        String user_id = session.get("user_id");
        if (user_id == null) {
            flash("message", "Hey! You're not even signed in!!!");
        } else {
            flash("message", "Successfully logged out!");
            session.remove("user_id");
        }
        redirect("/");
    }
    
    /*
     * Show list of movies.
     */
    public static void movie_list() {
    	List<Movie> movies = Movie.allMovies; //Movie.findAll();
    	render("@movie_list", movies);
	}

    /*
     * Return page showing the details of a given movie.
     */
    public static void show_movie(Long movie_id) {
    	Movie movie = Movie.getMovie(movie_id);
    	Collections.sort(movie.ratings, new Comparator<UserScore>() {
            public int compare(UserScore m1, UserScore m2) {
                return m1.user.user_id.compareTo(m2.user.user_id);
            }
        });
    	Double totalScore = 0.0;
    	for (UserScore us:movie.ratings) {
    		totalScore += us.score;
    	}
    	// Get average rating of movie
    	Double average = totalScore / movie.ratings.size();
    	User current_user = null;
    	MovieScore user_rating = null;
    	String user_id = session.get("user_id");
    	if (user_id != null) {
    		current_user = User.getUser(Long.parseLong(user_id));
    		user_rating = current_user.getUserRating(movie_id);
    	}
        render("@movie_detail", movie, average, current_user, user_rating);
    }

}