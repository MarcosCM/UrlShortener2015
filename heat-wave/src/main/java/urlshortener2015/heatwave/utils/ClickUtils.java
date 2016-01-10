package urlshortener2015.heatwave.utils;

import java.util.HashMap;
import java.util.Map;

import urlshortener2015.heatwave.entities.DetailedStats;
import urlshortener2015.heatwave.entities.ShortURL;

public class ClickUtils {
	
	/**
	 * 
		FROM:
		
		{
		clicks : { "0-5" : x, "5-10" : y},
		country : { "Spain" : x, "France" : y},
		browser : { "Firefox" : x, "Opera" : y},
		platform : { "Windows" : x, "Unix" : y}
		}
		
		TO:
		
		{
		clicks : { data : { '0-5' : x, '5-10' : y},
			options : { 'title' : 'By Clicks' },
			type : 'LineChart' },
		...
		...
		...
		}
	 */
	public static DetailedStats fromMapToChartParams(ShortURL shortURL, Map<String, Map<String, Integer>> mapReducedMap){
		Map<String, DetailedStats.ChartData> charts = new HashMap<String, DetailedStats.ChartData>();
		for(Map.Entry<String, Map<String, Integer>> by : mapReducedMap.entrySet()){
			// Titulo de la grafica
			String byName = by.getKey();
			// Datos de la grafica
			Map<String, Integer> data = by.getValue();
			// Opciones de la grafica
			Map<String, String> options = new HashMap<String, String>();
			options.put("title", "By " + byName);
			// Tipo de grafica
			String type = "";
			if (byName.equalsIgnoreCase("date")) type = "LineChart";
			else if (byName.equalsIgnoreCase("country")) type = "BarChart";
			else if (byName.equalsIgnoreCase("browser")) type = "PieChart";
			else if (byName.equalsIgnoreCase("platform")) type = "PieChart";
			DetailedStats.ChartData chartData = new DetailedStats.ChartData(data, options, type);
			// Anadimos a la estructura
			charts.put(byName, chartData);
		}
		DetailedStats detailedStats = new DetailedStats(shortURL, charts);
		return detailedStats;
	}
}
