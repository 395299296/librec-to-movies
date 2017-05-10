import play.jobs.*;

import java.io.IOException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.*;

import common.Util;
import manage.RecommendMgr;
import models.Movie;
import models.Rating;
import models.User;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.convertor.TextDataConvertor;
import net.librec.data.model.ArffInstance;
import net.librec.data.model.TextDataModel;
import net.librec.math.structure.DataSet;
import net.librec.math.structure.SparseMatrix;
import net.librec.similarity.*;

@OnApplicationStart
public class Bootstrap extends Job {
    
	private static final Log LOG = LogFactory.getLog(Bootstrap.class);
	
	@Override
    public void doJob() throws LibrecException {  

		long start = 0;
		long end = 0;
		
		// initialize recommend system
		start = System.currentTimeMillis();
		// RecommendMgr.getInstance().init();
		end = System.currentTimeMillis();
        LOG.info( "Init recommender costs " + (end - start) + " milliseconds" );
		
		// load movie set
		start = System.currentTimeMillis();
		ArrayList<ArffInstance> arffList = null;
		try {
			arffList = Util.readData("data/u.item");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int numRows = arffList.size();
		for (int row = 0; row < numRows; row++) {
            ArffInstance instance = arffList.get(row);
            Movie movie = new Movie(((Double)instance.getValueByAttrName("movie_id")).longValue(), 
            						(String)instance.getValueByAttrName("title"), 
            						(String)instance.getValueByAttrName("released_str"), 
            						(String)instance.getValueByAttrName("imdb_url"));
            // movie.save();
            Movie.allMovies.add(movie);
		}
		end = System.currentTimeMillis();
        LOG.info( "Load movie set costs " + (end - start) + " milliseconds" );
		
        if (true) return;
        
		// load user set
        start = System.currentTimeMillis();
		try {
			arffList = Util.readData("data/u.user");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		numRows = arffList.size();
		for (int row = 0; row < numRows; row++) {
            ArffInstance instance = arffList.get(row);
            User user = new User(((Double)instance.getValueByAttrName("user_id")).longValue(), 
            					 ((Double)instance.getValueByAttrName("age")).intValue(), 
            					 (String)instance.getValueByAttrName("gender"), 
            					 (String)instance.getValueByAttrName("profession"), 
            					 (String)instance.getValueByAttrName("zipcode"));
            // user.save();
            User.allUsers.add(user);
		}
		end = System.currentTimeMillis();
        LOG.info( "Load user set costs " + (end - start) + " milliseconds" );
		
		// load rating set
        start = System.currentTimeMillis();
		TextDataModel dataModel = RecommendMgr.getInstance().getDataModel();
		SparseMatrix preference = (SparseMatrix) dataModel.getTrainDataSet();
		Table<Integer, Integer, Double> dataTable = preference.getDataTable();
		BiMap<String, Integer> userIds = dataModel.getUserMappingData();
		BiMap<String, Integer> itemIds = dataModel.getItemMappingData();
		for (Map.Entry<String, Integer> userId : userIds.entrySet()) {
			for (Map.Entry<String, Integer> itemId : itemIds.entrySet()) {
				Object value = dataTable.get(userId.getValue(), itemId.getValue());
				if (value != null) {
					Long user_id = Long.parseLong(userId.getKey());
					Long movie_id = Long.parseLong(itemId.getKey());
					Double score = (Double)value;
					Rating rating = new Rating(user_id, movie_id, score);
					User user = User.getUser(user_id);
					Movie movie = Movie.getMovie(movie_id);
					try {
						user.addMovie(movie, score);
						movie.addUser(user, score);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					//rating.save();
					Rating.allRatings.add(rating);
				}
			}
		}
		for (Movie movie:Movie.allMovies) {
			movie.calcAvgRating();
		}
		end = System.currentTimeMillis();
        LOG.info( "Load rating set costs " + (end - start) + " milliseconds" );
    }
      
}