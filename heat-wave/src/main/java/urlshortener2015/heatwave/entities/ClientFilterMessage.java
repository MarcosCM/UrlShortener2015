package urlshortener2015.heatwave.entities;

import java.util.Date;

public class ClientFilterMessage {

    private String id;
    private Date from;
    private Date to;

    public String getId() {
        return id;
    }
    
    public Date getFrom(){
    	return from;
    }
    
    public Date getTo(){
    	return to;
    }
}
