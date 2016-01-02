package urlshortener2015.heatwave.repository;

import java.util.Map;

public interface ClickRepositoryCustom {

	/**
	 * Gets the aggregated info of a shortened URL
	 * @param hash Hash
	 * @param mapFunction Map function
	 * @param reduceFunction Reduce function
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
}
