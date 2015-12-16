package urlshortener2015.heatwave.utils;

import java.util.Random;
import urlshortener2015.heatwave.repository.ShortURLRepository;

public class SuggestionUtils {
	
	public static String sugerenciaSufijos(ShortURLRepository repositorio, String nombre){
		Random random= new Random();

		/*
		 * Diferentes formas de generar los sufijos de forma aleatoria
		 */
		int intento=1;
		while(repositorio.findByHash(nombre) != null){
			int tamanoSufijo = random.nextInt(intento);
			if(tamanoSufijo == 0) tamanoSufijo++;
			//numeros ASCII de 'a-Z' 60-122
			for(int j=0; j<tamanoSufijo; j++){
				//generas la siguiente letra del sufijo
				nombre += Character.toString ((char) (random.nextInt(122-60)+60));
			}
			intento++;
		}
		
		return nombre;
	}
	
	public static String SugerenciaSinonimos(ShortURLRepository repositorio, String nombre){
		String sugerencias= "";
		
		
		
		return sugerencias;
	}
}
