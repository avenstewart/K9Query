package com.avenstewart;
import java.util.List;

public class ImageList {

    private static String status;
    private static List<String> message;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    private List<String> getMessage(){
        return message;
    }
    public void setMessage(List<String> message){
        this.message = message;
    }

    public List<String> createList(){
        return getMessage();
    }

}