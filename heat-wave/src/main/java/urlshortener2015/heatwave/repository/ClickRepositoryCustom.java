package urlshortener2015.heatwave.repository;

import java.util.Map;

public interface ClickRepositoryCustom {

	Map<String, Map<String, Integer>> aggregateInfoByHash(String hash);
}
