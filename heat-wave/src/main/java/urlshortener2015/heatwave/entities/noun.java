package urlshortener2015.heatwave.entities;

	import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

	@JsonIgnoreProperties(ignoreUnknown = true)
	public class noun {

	    private ArrayList<String> syn;

	    public noun() {
	    }

	  
	    public ArrayList<String> getSyn() {
			return syn;
		}


		public void setSyn(ArrayList<String> syn) {
			this.syn = syn;
		}


		public String toString() {
			String resultado="noun:{syn:[";
			for (int i=0;i<syn.size();i++){
				if(i<syn.size()-1)resultado+="'"+syn.get(i)+"',";
				else resultado+="'"+syn.get(i)+"'";
			}
			
	        return resultado+"]}";
	    }
	
}
