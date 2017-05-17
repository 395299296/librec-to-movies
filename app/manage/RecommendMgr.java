package manage;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import common.Util;
import models.*;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.DataModel;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.rating.RMSEEvaluator;
import net.librec.filter.GenericRecommendedFilter;
import net.librec.recommender.AbstractRecommender;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.cf.ItemKNNRecommender;
import net.librec.recommender.cf.UserKNNRecommender;
import net.librec.recommender.cf.rating.PMFRecommender;
import net.librec.recommender.item.RecommendedItem;
import net.librec.similarity.PCCSimilarity;
import net.librec.similarity.RecommenderSimilarity;

public class RecommendMgr {

	private static final Log LOG = LogFactory.getLog(RecommendMgr.class);
	
    private static RecommendMgr instance;
    
    private Recommender itemRecommender;
    private TextDataModel dataModel;
    
    private List<Movie> releaseTopList;
    private List<Movie> scoreTopList;
    private List<Movie> hotTopList;
    
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
		Configuration conf = getConfig();
		dataModel = new TextDataModel(conf);
        try {
			dataModel.buildDataModel();
		} catch (LibrecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // build item recommender
        LOG.info("build item recommender");
        itemRecommender = new ItemKNNRecommender();
        buildRecommender(itemRecommender, conf);
        
        // build user recommender
        /*LOG.info("build user recommender");
        conf.set("rec.recommender.similarity.key" ,"user");
        conf.set("rec.neighbors.knn.number", "50");
        userRecommender = new UserKNNRecommender();
        buildRecommender(userRecommender, conf);
        
        // build rating recommender
        LOG.info("build rating recommender");
        ratingRecommender = new PMFRecommender();
        buildRecommender(ratingRecommender, conf);*/
	}
	
	public Configuration getConfig() {
		Configuration conf = new Configuration();
        conf.set("dfs.data.dir", "data");
        conf.set("data.input.path", "u.data");
        conf.set("data.column.format", "UIRT");
        return conf;
	}
	
	public void sortMovies() {
		// sort by average score desc
		Collections.sort(Movie.allMovies, new Comparator<Movie>() {
            public int compare(Movie m1, Movie m2) {
                return m2.avg_rating.compareTo(m1.avg_rating);
            }
        });
		scoreTopList = new ArrayList<>();
		for (Movie movie:Movie.allMovies) {
			scoreTopList.add(movie);
		}
		
		// sort by release time desc
		Collections.sort(Movie.allMovies, new Comparator<Movie>() {
            public int compare(Movie m1, Movie m2) {
                return m2.released_at.compareTo(m1.released_at);
            }
        });
		releaseTopList = new ArrayList<>();
		for (Movie movie:Movie.allMovies) {
			releaseTopList.add(movie);
		}
		
		// sort by average score date time desc
		Collections.sort(Movie.allMovies, new Comparator<Movie>() {
            public int compare(Movie m1, Movie m2) {
                return m2.avg_datetime.compareTo(m1.avg_datetime);
            }
        });
		hotTopList = new ArrayList<>();
		for (Movie movie:Movie.allMovies) {
			hotTopList.add(movie);
		}
	}
	
	public long buildRecommender(Recommender recommender, Configuration conf) {
		// set configuration
		setConfig(recommender.getClass().getName(), conf);
		// build item similarity
		RecommenderSimilarity similarity = new PCCSimilarity();
        similarity.buildSimilarityMatrix(dataModel);
        
		 // build recommender context
        RecommenderContext context = new RecommenderContext(conf, dataModel, similarity);

        // run recommender algorithm
		long start = System.currentTimeMillis();
        try {
			recommender.recommend(context);
		} catch (LibrecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
        LOG.info( "Build recommender costs " + (end - start) + " milliseconds" );
        
        // evaluate the recommended result
        RecommenderEvaluator evaluator = new RMSEEvaluator();
        try {
			LOG.info("RMSE:" + recommender.evaluate(evaluator));
		} catch (LibrecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        return end - start;
	}
	
	public List<Movie> getDefaultItemList(int limit) {
		List<Movie> movies = new ArrayList<>();
    	for (Movie movie:scoreTopList) {
    		if (movies.size() >= limit)
    			break;
    		movies.add(movie);
    	}
    	return movies;
	}
	
	public List<Movie> getFilterItemList(String user_id, int limit) {
        // filter the recommended result
        return getFilterItemList(itemRecommender, user_id, limit);
	}
	
	public List<Movie> getFilterItemList(Recommender recommender, String user_id, int limit) {
        // filter the recommended result
        List<RecommendedItem> recommendedItemList = recommender.getRecommendedList();
        GenericRecommendedFilter filter = new GenericRecommendedFilter();
        List<String> userIdList = new ArrayList<>();
        userIdList.add(user_id);
        filter.setUserIdList(userIdList);
        recommendedItemList = filter.filter(recommendedItemList);
        
        List<Movie> movies = new ArrayList<>();
        for (RecommendedItem item:recommendedItemList) {
        	if (movies.size() >= limit)
    			break;
        	Long movie_id = Long.parseLong(item.getItemId());
        	Movie movie = Movie.getMovie(movie_id);
        	movies.add(movie);
        }
        
        return movies;
	}
	
	public Double predictUserItemRating(String user_id, String item_id) {
		try {
			int userIdx = ((ItemKNNRecommender) itemRecommender).userMappingData.get(user_id);
			int itemIdx = ((ItemKNNRecommender) itemRecommender).itemMappingData.get(item_id);
			return ((ItemKNNRecommender) itemRecommender).predict(userIdx, itemIdx);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LibrecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0.0;
	}
	
	public List<Movie> getFilterUserList(String user_id, String item_id, int limit) {
        // filter the recommended result
        List<RecommendedItem> recommendedItemList = itemRecommender.getRecommendedList();
        GenericRecommendedFilter filter = new GenericRecommendedFilter();
        List<String> userIdList = new ArrayList<>();
        if (user_id != null) {
        	userIdList.add(user_id);
        	filter.setUserIdList(userIdList);
        	recommendedItemList = filter.filter(recommendedItemList);
        }
        
        List<Movie> movies = new ArrayList<>();
        for (RecommendedItem item:recommendedItemList) {
        	if (movies.size() >= limit)
    			break;
        	if (item.getItemId().equals(item_id)) continue;
        	Long movie_id = Long.parseLong(item.getItemId());
        	Movie movie = Movie.getMovie(movie_id);
        	movies.add(movie);
        }
        
        return movies;
	}
	
	public List<Movie> getRecentReleaseList(int page) {
		List<Movie> movies = new ArrayList<>();
		int start = page * 5;
		if (start < 0)
			return movies;
		
		int end = (page + 1) * 5;
		if (end >= releaseTopList.size())
			end = releaseTopList.size();
		
    	for (int i = start; i < end; i++) {
    		movies.add(releaseTopList.get(i).clone());
    	}
    	
    	return movies;
	}
	
	public List<Movie> getScoreTopList(int limit) {
		List<Movie> movies = new ArrayList<>();
    	for (Movie movie:scoreTopList) {
    		if (movies.size() >= limit)
    			break;
    		movies.add(movie);
    	}
    	return movies;
	}
	
	public List<Movie> getHotTopList(int limit) {
		List<Movie> movies = new ArrayList<>();
    	for (Movie movie:hotTopList) {
    		if (movies.size() >= limit)
    			break;
    		movies.add(movie);
    	}
    	return movies;
	}
	
	public List<Entry<String, String>> getRecommenderAlgorithms() throws ClassNotFoundException {
		String packageName = "net.librec.recommender";
		List<Entry<String, String>> algorithms = new ArrayList<>();
        List<String> classNames = Util.getClassName(packageName, true);
        if (classNames != null) {
        	for (String className:classNames) {
        		if (className.indexOf("$") != -1) continue;
        		Class<?> clazz = Class.forName(className);
        		if (Modifier.isAbstract(clazz.getModifiers())) continue;
        		int index = className.lastIndexOf(".");
        		String algoName = className.substring(index + 1);
        		if (algoName.equals("package-info")) continue;
        		algorithms.add(new AbstractMap.SimpleEntry(algoName, className));
        	}
        }
        Collections.sort(algorithms, new Comparator<Entry<String, String>>() {
            public int compare(Entry<String, String> p1, Entry<String, String> p2) {
                return p1.getKey().compareTo(p2.getKey());
            }
        });
        return algorithms;
	}
	
	public void setConfig(String className, Configuration conf) {
		int index = className.lastIndexOf(".");
		String algoName = className.substring(index + 1);
		switch (algoName) {
		case "AoBPRRecommender":
			conf.set("rec.item.distribution.parameter", "500");
			break;
		case "ItemKNNRecommender":
			conf.set("rec.recommender.similarity.key" ,"item");
			break;
		case "EFMRecommender":
			conf.set("rec.iterator.maximum", "20");
			break;
		case "FISMaucRecommender":
			conf.set("rec.fismauc.rho", "0.5");
			break;
		case "FISMrmseRecommender":
			conf.set("rec.fismrmse.rho", "1");
			break;
		case "FactorizationMachineRecommender":
			
			break;
		default:
			break;
		}
	}
}
