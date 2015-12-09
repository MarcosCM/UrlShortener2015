package urlshortener2015.heatwave;

import java.util.Random;
import urlshortener2015.common.domain.ShortURL;
import urlshortener2015.common.repository.ShortURLRepository;

public class Sugerencias {
	
	public static String sugerenciaSufijos(ShortURLRepository repositorio, String nombre){
		Random random= new Random();

		/*
		 * Diferentes formas de generar los sufijos de forma aleatoria
		 */
		int intento=1;
		while(repositorio.findByKey(nombre)!=null){
			int tamanoSufijo=random.nextInt(intento);
			if(tamanoSufijo==0)tamanoSufijo++;
			//60-122
			for(int j=0;j<tamanoSufijo;j++){
				//generas la siguiente letra del sufijo
				nombre+=Character.toString ((char) (random.nextInt(122-60)+60));
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
