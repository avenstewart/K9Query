package com.avenstewart;

import java.util.List;
import java.util.Map;

public class AllBreeds {
    private static String status;
    private static Map<String, List<String>> message;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Map<String, List<String>> getMessage(){
        return message;
    }
    public void setMessage(Map<String, List<String>> message){
        this.message = message;
    }


    public Map<String, List<String>> createMap(){
        return getMessage();
    }

}
