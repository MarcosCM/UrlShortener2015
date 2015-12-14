package urlshortener2015.heatwave.entities;

public class Estadisticas {

	Long clicks;
	String url;
	String fechaCreacion;

	public Long getClicks() {
		return clicks;
	}

	public void setClicks(Long clicks) {
		this.clicks = clicks;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(String fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	@Override
	public String toString() {

		String resultado = "Este enlace ha recibido " + clicks + " clicks";
		resultado += "</br>La url es " + url;
		resultado += "</br>La fecha de creación es " + fechaCreacion;

		return resultado;
	}

	public Estadisticas(Long clicks, String url, String fechaCreacion) {
		this.clicks = clicks;
		this.url = url;
		this.fechaCreacion = fechaCreacion;

	}
}
