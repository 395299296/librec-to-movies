package manage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import models.Movie;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.rating.RMSEEvaluator;
import net.librec.filter.GenericRecommendedFilter;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.cf.ItemKNNRecommender;
import net.librec.recommender.item.RecommendedItem;
import net.librec.similarity.PCCSimilarity;
import net.librec.similarity.RecommenderSimilarity;

public class RecommendMgr {

	private static final Log LOG = LogFactory.getLog(RecommendMgr.class);
	
    private static RecommendMgr instance;
    
    private Recommender recommender;
    private TextDataModel dataModel;
    
    private RecommendMgr () {
    	 
    }
    
	public static RecommendMgr getInstance() {
		if (instance == null) {
			synchronized(RecommendMgr.class) {
				if (instance == null)
					instance = new RecommendMgr(); 
			}
		}
		return instance;
	}
	
	public TextDataModel getDataModel() {
		return dataModel;
	}
	     
	public void init() {
		Configuration conf = new Configuration();
        conf.set("dfs.data.dir", "data");
        conf.set("data.input.path", "u.data");
		dataModel = new TextDataModel(conf);
        try {
			dataModel.buildDataModel();
		} catch (LibrecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // build recommender context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build similarity
        conf.set("rec.recommender.similarity.key" ,"item");
        RecommenderSimilarity similarity = new PCCSimilarity();
        similarity.buildSimilarityMatrix(dataModel);
        context.setSimilarity(similarity);

        // build recommender
        recommender = new ItemKNNRecommender();
        recommender.setContext(context);

        // run recommender algorithm
        try {
			recommender.recommend(context);
		} catch (LibrecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // evaluate the recommended result
        RecommenderEvaluator evaluator = new RMSEEvaluator();
        try {
			LOG.info("RMSE:" + recommender.evaluate(evaluator));
		} catch (LibrecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Movie> getDefaultItemList() {
		Collections.sort(Movie.allMovies, new Comparator<Movie>() {
            public int compare(Movie m1, Movie m2) {
                return m2.avg_rating.compareTo(m1.avg_rating);
            }
        });
		List<Movie> movies = new ArrayList<>();
    	for (Movie movie:Movie.allMovies) {
    		if (movies.size() >= 10)
    			break;
    		movies.add(movie);
    	}
    	return movies;
	}
	
	public List<Movie> getFilterItemList(String user_id) {
        // filter the recommended result
        List<RecommendedItem> recommendedItemList = recommender.getRecommendedList();
        GenericRecommendedFilter filter = new GenericRecommendedFilter();
        List<String> userIdList = new ArrayList<>();
        userIdList.add(user_id);
        filter.setUserIdList(userIdList);
        recommendedItemList = filter.filter(recommendedItemList);
        
        List<Movie> movies = new ArrayList<>();
        for (RecommendedItem item:recommendedItemList) {
        	if (movies.size() >= 12)
    			break;
        	Long movie_id = Long.parseLong(item.getItemId());
        	Movie movie = Movie.getMovie(movie_id);
        	movies.add(movie);
        }
        
        return movies;
	}
	
	public List<List<Movie>> getRecentReleaseList() {
        // sort by release time
		Collections.sort(Movie.allMovies, new Comparator<Movie>() {
            public int compare(Movie m1, Movie m2) {
                return m2.released_at.compareTo(m1.released_at);
            }
        });
		List<List<Movie>> recent_list = new ArrayList<>();
		List<Movie> movies = new ArrayList<>();
    	for (Movie movie:Movie.allMovies) {
    		movies.add(movie);
    		if (movies.size() == 5) {
    			recent_list.add(movies);
    			movies = new ArrayList<>();
    		}
    	}
    	
    	return recent_list;
	}
	
	public List<Movie> getScoreTopList() {
		Collections.sort(Movie.allMovies, new Comparator<Movie>() {
            public int compare(Movie m1, Movie m2) {
                return m2.avg_rating.compareTo(m1.avg_rating);
            }
        });
		List<Movie> movies = new ArrayList<>();
    	for (Movie movie:Movie.allMovies) {
    		if (movies.size() >= 10)
    			break;
    		movies.add(movie);
    	}
    	return movies;
	}
}
