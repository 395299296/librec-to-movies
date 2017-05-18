package controllers;

import play.*;
import play.mvc.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hamcrest.core.IsInstanceOf;

import com.google.common.collect.BiMap;

import manage.RecommendMgr;
import models.*;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.math.structure.SparseMatrix;
import net.librec.recommender.AbstractRecommender;
import net.librec.recommender.Recommender;
import net.librec.recommender.TensorRecommender;
import net.librec.recommender.cf.ItemKNNRecommender;

public class Application extends Controller {

	private static final Log LOG = LogFactory.getLog(Application.class);
	
    public static void index() {
    	// show recommended movie list
    	List<Movie> recommend_list = new ArrayList<>();
    	String user_id = session.get("user_id");
    	if (user_id == null)
    		recommend_list = RecommendMgr.getInstance().getDefaultItemList(10);
    	else
    		recommend_list = RecommendMgr.getInstance().getFilterItemList(user_id, 10);
        // show recent release movie list
        List<Movie> recent_list = RecommendMgr.getInstance().getRecentReleaseList(0);
        // show score list top 10
        List<Movie> score_list = RecommendMgr.getInstance().getScoreTopList(10);
        
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
    	User current_user = null;
    	MovieScore user_rating = null;
    	Double prediction = null;
    	String user_id = session.get("user_id");
    	if (user_id != null) {
    		current_user = User.getUser(Long.parseLong(user_id));
    		user_rating = current_user.getUserRating(movie_id);
    		prediction = RecommendMgr.getInstance().predictUserItemRating(user_id, movie_id.toString());
    		BigDecimal bg = new BigDecimal(prediction);
			prediction = bg.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    	}
    	// show recommended movie list
    	List<Movie> recommend_list = RecommendMgr.getInstance().getFilterUserList(user_id, movie_id.toString(), 12);
        // show recent score movie list top 8
    	List<Movie> hot_list = RecommendMgr.getInstance().getHotTopList(8);
    	
        render("@movie_detail", movie, current_user, user_rating, prediction, recommend_list, hot_list);
    }
    
    /*
     * Show more movies by click view button times
     */
    public static void show_more_movies(int index) {
    	List<Movie> recent_list = RecommendMgr.getInstance().getRecentReleaseList(index);
    	renderJSON(recent_list);
	}
    
    /*
     * To a particular movie score
     */
    public static void add_new_rating(Long movie_id, Double new_rating) {
    	if (new_rating == null) {
            flash("message", "Please input 1-5 number!!!");
    	} else {
	    	String user_id = session.get("user_id");
	        if (user_id == null) {
	            flash("message", "Hey! You're not even signed in!!!");
	        } else {
		    	User current_user = User.getUser(Long.parseLong(user_id));
		    	Long datetime = System.currentTimeMillis();
		    	int result = current_user.setUserRating(movie_id, new_rating, (double)datetime);
		    	MovieEx movie = (MovieEx) MovieEx.getMovie(movie_id);
		    	movie.calcAvgRating();
		    	switch (result) {
				case 0:
					flash("message", "Your rating was submitted.");
					break;
				case 1:
					flash("message", "Rating added.");
				default:
					break;
				}
	        }
    	}
    	redirect("/movies/" + movie_id);
    }
    
    /*
     * test recommendation algorithms.
     */
    public static void test(String className, Long movie_id, Double new_rating, Integer itemNum) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, LibrecException {
    	String user_id = session.get("user_id");
    	if (user_id == null) {
    		user_id = "1";
    		session.put("user_id", user_id);
    	}
    	if (className == null || className.isEmpty()) {
    		className = "net.librec.recommender.cf.rating.PMFRecommender";
    	}
    	// add new rating
    	if (movie_id != null && new_rating != null) {
    		TextDataModel dataModel = RecommendMgr.getInstance().getDataModel();
    		SparseMatrix testMatrix = (SparseMatrix) dataModel.getTestDataSet();
    		BiMap<String, Integer> userIds = dataModel.getUserMappingData();
    		BiMap<String, Integer> itemIds = dataModel.getItemMappingData();
    		int userIdx = userIds.get(user_id);
    		int itemIdx = itemIds.get(movie_id.toString());
    		testMatrix.set(userIdx, itemIdx, new_rating);
    		User current_user = User.getUser(Long.parseLong(user_id));
	    	Long datetime = System.currentTimeMillis();
	    	current_user.setUserRating(movie_id, new_rating, (double)datetime);
	    	MovieEx movie = (MovieEx) MovieEx.getMovie(movie_id);
	    	movie.calcAvgRating();
    	}
    	if (itemNum == null) {
    		itemNum = 5;
    	}
    	
    	Class<?> clazz = Class.forName(className);
    	Constructor constructor = clazz.getConstructor();
    	Recommender recommender = (Recommender) constructor.newInstance();
    	Configuration conf = RecommendMgr.getInstance().getConfig();
    	// build rating recommender
        LOG.info("build rating recommender");
    	Long runtime = RecommendMgr.getInstance().buildRecommender(recommender, conf);
    	
    	List<Entry<String, String>> algorithms = RecommendMgr.getInstance().getRecommenderAlgorithms();
    	List<Double> ratings = new ArrayList<>();
    	List<Double> predict_ratings = new ArrayList<>();
    	// show recommended movie list
    	List<Movie> recommend_list = RecommendMgr.getInstance().getFilterItemList(recommender, user_id, itemNum, predict_ratings);
    	// show real rating
    	for (Movie movie:recommend_list) {
    		ratings.add(((MovieEx)movie).getUserRating(Long.parseLong(user_id)));
    	}
    	
        render("@test", algorithms, className, runtime, recommend_list, ratings, predict_ratings, itemNum);
    }

}