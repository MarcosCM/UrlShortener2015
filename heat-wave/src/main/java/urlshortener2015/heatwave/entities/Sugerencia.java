package urlshortener2015.heatwave.entities;

public class Sugerencia {
	
	String recomendacion="";
	public Sugerencia(String nueva){
		recomendacion=nueva;
	}
	public String getRecomendacion() {
		return recomendacion;
	}
	public void setRecomendacion(String recomendacion) {
		this.recomendacion = recomendacion;
	}
	@Override
	public String toString(){
		return recomendacion;
	}
}
