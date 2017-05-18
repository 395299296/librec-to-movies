import play.jobs.*;

import java.io.IOException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.*;

import common.Util;
import manage.RecommendMgr;
import models.*;
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
		RecommendMgr.getInstance().init();
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
            MovieEx movie = new MovieEx(((Double)instance.getValueByAttrName("movie_id")).longValue(), 
            						(String)instance.getValueByAttrName("title"), 
            						(String)instance.getValueByAttrName("released_str"), 
            						(String)instance.getValueByAttrName("imdb_url"),
            						(String)instance.getValueByAttrName("summary"),
            						(String)instance.getValueByAttrName("subtext"));
            // movie.save();
            MovieEx.allMovies.add(movie);
		}
		end = System.currentTimeMillis();
        LOG.info( "Load movie set costs " + (end - start) + " milliseconds" );
		
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
		SparseMatrix trainMatrix = (SparseMatrix) dataModel.getTrainDataSet();
		SparseMatrix testMatrix = (SparseMatrix) dataModel.getTestDataSet();
		RecommendMgr.getInstance().initData(trainMatrix);
		RecommendMgr.getInstance().initData(testMatrix);
		
		for (Movie movie:Movie.allMovies) {
			((MovieEx) movie).calcAvgRating();
		}
		end = System.currentTimeMillis();
        LOG.info( "Load rating set costs " + (end - start) + " milliseconds" );
        
        // sort movies
        RecommendMgr.getInstance().sortMovies();
    }
      
}