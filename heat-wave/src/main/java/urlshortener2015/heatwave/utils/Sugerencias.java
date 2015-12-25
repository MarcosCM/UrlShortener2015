package urlshortener2015.heatwave.utils;

import java.util.ArrayList;
import java.util.Random;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import urlshortener2015.heatwave.entities.Sugerencia;
import urlshortener2015.heatwave.repository.ShortURLRepository;

public class Sugerencias {
	
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
	public static ArrayList<Sugerencia>  sugerenciasFromAPIs (ArrayList<Sugerencia> lista , String customTag,ShortURLRepository shortURLRepository ){
		RestTemplate restTemplate = new RestTemplate();
		try {
			ResponseEntity<String> response = restTemplate.getForEntity("http://words.bighugelabs.com/api/2/c302f07e3593264f58a7366800330462/"
							+ customTag + "/json", String.class);
			String body = response.getBody();
			// a partir de la posicion 5 estan los resultados
			// se anaden dos sugerencias si la api ha devuelto resultados
			// son en posiciones impares (en las pares hay comas)
			int sugerenciasIngles=0;
			int i=5;
			String sugerencias []= body.split("\"");
			while(sugerenciasIngles<3 && sugerencias.length>(i+2)){
				if(shortURLRepository.findByHash(sugerencias[i])==null){
					lista.add(new Sugerencia(sugerencias[i]));
					sugerenciasIngles++;
				}
				// son en posiciones impares (en las pares hay comas)
				i+=2;
			}//
			response = restTemplate.getForEntity("http://thesaurus.altervista.org/thesaurus/v1?word="+customTag+"&language=en_US&key=n2HXWUfppJ1S60lje6Km"
					, String.class);
			body = response.getBody();
			sugerencias =body.split("<synonyms>");
			body =sugerencias[1].split("</synonyms>")[0];
			sugerencias =body.split("\\|");


			i=0;
			while(sugerenciasIngles<5 && sugerencias.length>i){

				if(shortURLRepository.findByHash(sugerencias[i])==null && !esta(lista, sugerencias[i])){
					lista.add(new Sugerencia(sugerencias[i]));
					sugerenciasIngles++;

				}
				// son en posiciones impares (en las pares hay comas)
				i++;
			}
			

			response = restTemplate.getForEntity("http://watson.kmi.open.ac.uk/API/term/synonyms?term="+customTag
					, String.class);

			body = response.getBody();
			sugerencias =body.split("\",\"");
			i=1;

			while(sugerenciasIngles<7 && sugerencias.length>i+1){
				if(shortURLRepository.findByHash(sugerencias[i])==null && !esta(lista, sugerencias[i])){
					lista.add(new Sugerencia(sugerencias[i]));
					sugerenciasIngles++;

				}
				// son en posiciones impares (en las pares hay comas)
				i++;
			}
			

			response = restTemplate.getForEntity("http://dictionaryapi.net/api/definition/"+customTag
					, String.class);

			body = response.getBody();
			body=body.replace("[", "").replace("\"", " ").replace("{", " ").replace("}", " ").replace("]", " ").replace(":", " ").replace(",", " ");
			sugerencias =body.split(" ");
			

			//miras palabras al azar
			i=15;
			while(sugerenciasIngles<9 && sugerencias.length>i && i<100){

				if(shortURLRepository.findByHash(sugerencias[i])==null && !esta(lista, sugerencias[i])
						&& sugerencias[i].length()>3){
					lista.add(new Sugerencia(sugerencias[i]));
					sugerenciasIngles++;

				}
				// son en posiciones impares (en las pares hay comas)
				i+=8;
			}
			
			
			
			
			
		} catch (Exception a) {}
		return lista;
	}
	public static boolean  esta (ArrayList<Sugerencia> lista, String recomendacion){
		boolean esta=false;
		for (int i=0; i<lista.size() && !esta;i++){
			esta=lista.get(i).getRecomendacion().equals(recomendacion);
		}
		
		return esta;
	}
}
