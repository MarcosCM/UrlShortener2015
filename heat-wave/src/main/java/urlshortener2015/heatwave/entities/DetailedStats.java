package urlshortener2015.heatwave.entities;

import java.util.Map;

/**
 * Google Charts drawer map
 */
public class DetailedStats {

	private ShortURL shortURL;
	private Map<String, ChartData> charts;
	
	public DetailedStats (ShortURL shortURL, Map<String, ChartData> charts){
		this.shortURL = shortURL;
		this.charts = charts;
	}
	
	/**
	 * Google Charts drawer data map
	 */
	public static class ChartData {
		
		private Map<String, Integer> data;
		private Map<String, String> options;
		private String type;
		
		public ChartData(Map<String, Integer> data, Map<String, String> options, String type){
			this.data = data;
			this.options = options;
			this.type = type;
		}
		
		public Map<String, Integer> getData(){
			return data;
		}
		
		public Map<String, String> getOptions(){
			return options;
		}
		
		public String getType(){
			return type;
		}
	}
	
	public ShortURL getShortURL(){
		return shortURL;
	}
	
	public Map<String, ChartData> getCharts(){
		return charts;
	}
}
