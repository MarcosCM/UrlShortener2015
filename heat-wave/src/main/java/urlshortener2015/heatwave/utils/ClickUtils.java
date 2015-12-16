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
		Clicks : { '0-5' : x, '5-10' : y},
		Country : { 'Spain' : x, 'France' : y},
		Browser : { firefox : x, opera : y},
		Platform : { windows : x, unix : y}
		}
		
		TO:
		
		{
		Clicks : { data : { '0-5' : x, '5-10' : y},
			options : { 'title' : 'By Clicks' },
			type : 'PieChart' },
		Country : { 'Spain' : x, 'France' : y},
		Browser : { firefox : x, opera : y},
		Platform : { windows : x, unix : y}
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
			if (byName.equals("Clicks")) type = "PieChart";
			else if (byName.equals("Country")) type = "BarChart";
			else if (byName.equals("Browser")) type = "PieChart";
			else if (byName.equals("Platform")) type = "PieChart";
			DetailedStats.ChartData chartData = new DetailedStats.ChartData(data, options, type);
			// Anadimos a la estructura
			charts.put(byName, chartData);
		}
		DetailedStats detailedStats = new DetailedStats(shortURL, charts);
		return detailedStats;
	}
}
