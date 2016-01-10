package urlshortener2015.heatwave.repository;

import java.util.Date;
import java.util.Map;

public interface ClickRepositoryCustom {

	/**
	 * Gets the aggregated info of a shortened URL
	 * @param hash Hash
	 * @return Aggregated info with the following structure:
	 * 			[
	 * 				{ "_id" : country,
	 * 				"value" : {
	 * 					"Spain" : 15, "France" : 12
	 * 				}},
	 * 				{ "_id" : "browser",
	 * 				"value" : {
	 * 					"Firefox" : 10, "Chrome" : 10, "Microsoft Edge" : 7
	 * 				}},
	 * 				{ "_id" : "platform",
	 * 				"value" : {
	 * 					"Windows" : 22, "Unix" : 5
	 * 				}}
	 * 			]
	 */
	Map<String, Map<String, Integer>> aggregateInfoByHash(String hash);
	
	/**
	 * Gets the aggregated info of a shortened URL
	 * @param hash Hash
	 * @param from Clicks from a date
	 * @param to Clicks to a date
	 * @return Aggregated info with the following structure:
	 * 			[
	 * 				{ "_id" : country,
	 * 				"value" : {
	 * 					"Spain" : 15, "France" : 12
	 * 				}},
	 * 				{ "_id" : "browser",
	 * 				"value" : {
	 * 					"Firefox" : 10, "Chrome" : 10, "Microsoft Edge" : 7
	 * 				}},
	 * 				{ "_id" : "platform",
	 * 				"value" : {
	 * 					"Windows" : 22, "Unix" : 5
	 * 				}}
	 * 			]
	 */
	Map<String, Map<String, Integer>> aggregateInfoByHash(String hash, Date from, Date to);
}
