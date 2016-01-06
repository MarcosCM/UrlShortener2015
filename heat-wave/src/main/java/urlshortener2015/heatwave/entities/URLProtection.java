package urlshortener2015.heatwave.entities;

import java.util.ArrayList;

public class URLProtection {

    private String url;

    private ArrayList<String> methods;

    public URLProtection(String url){
        this.methods = new ArrayList<>();
        this.url = url;
    }

    public void setAllMethods() {
    	this.addMethod("GET");
    	this.addMethod("PUT");
    	this.addMethod("POST");
    	this.addMethod("DELETE");
    }

    public ArrayList<String> getMethods() {
        return methods;
    }

    public void setMethods(ArrayList<String> methods) {
        this.methods = methods;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void addMethod(String method){
        this.methods.add(method);
    }

    public boolean hasMethod(String method){
        return methods.contains(method);
    }
}