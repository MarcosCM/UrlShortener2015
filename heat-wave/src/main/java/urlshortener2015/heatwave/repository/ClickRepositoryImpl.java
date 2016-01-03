package urlshortener2015.heatwave.repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;

import urlshortener2015.heatwave.utils.FileUtils;

@Repository
public class ClickRepositoryImpl implements ClickRepositoryCustom {

	private static final Logger logger = LoggerFactory.getLogger(ClickRepositoryImpl.class);
	
	private static final String collection = "clicks";
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public Map<String, Map<String, Integer>> aggregateInfoByHash(String hash) {
		Map<String, Map<String, Integer>> res = new HashMap<String, Map<String, Integer>>();
		
		logger.info("Requested mapReduce for hash: " + hash);
		// Read functions from files
		String mapFunction = null, reduceFunction = null;
		try {
			mapFunction = FileUtils.readContent("src/main/resources/static/js/mongo/clickMapFunction.js");
			reduceFunction = FileUtils.readContent("src/main/resources/static/js/mongo/clickReduceFunction.js");
		} catch (IOException e) {
			logger.info(e.getMessage());
		};
		// Query the data => input of the map function
		DBObject query = new BasicDBObject();
		query.put("hash", new BasicDBObject("$eq", hash));
		// Execute Map-Reduce
		DBCollection collection = mongoTemplate.getDb().getCollection(ClickRepositoryImpl.collection);
		MapReduceCommand cmd = new MapReduceCommand(collection, mapFunction, reduceFunction, null, MapReduceCommand.OutputType.INLINE, query);
		MapReduceOutput output = collection.mapReduce(cmd);
		// Convert the result
		Iterator<DBObject> iterator = output.results().iterator();
		DBObject currentObj, currentValues;
		Map<String, Integer> currentMap;
		while(iterator.hasNext()){
			currentObj = iterator.next();
			currentMap = new HashMap<String, Integer>();
			// Convert the values
			currentValues = (DBObject) currentObj.get("value");
			for(String key : currentValues.keySet()){
				currentMap.put(key, ((Double) currentValues.get(key)).intValue());
			}
			// Asign the values to the property (country, browser, platform...)
			res.put((String) currentObj.get("_id"), currentMap);
		}
		
		return res;
	}
}
