package com.avenstewart;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.lang.*;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

    // Initialize/Define static variables
    private static String GET_URL = "https://dog.ceo:443/api/";
    private static String USER_AGENT = "";
    private static String simulate_downtime = "";

    // We'll use this to simulate downtime (as error code 500)
    private static int RequestProxy(int status){
        if (simulate_downtime.equals("TRUE")) {
            return 500;
        } else {
            return status;
        }
    }

    // This method handles Mock requests for 3 different image calls -
    // bulldogs, french bulldogs, and boston bulldogs
    private static List<String> ImageListMockRequest(String urlstr) throws IOException{

        byte[] content;

        if (urlstr.contains("bulldog/boston")){
            content = Files.readAllBytes(Paths.get("resources/mock_boston_bulldog.txt"));
        } else if (urlstr.contains("bulldog/french")){
            content = Files.readAllBytes(Paths.get("resources/mock_french_bulldog.txt"));
        } else if (urlstr.contains("bulldog")) {
            content = Files.readAllBytes(Paths.get("resources/mock_bulldog.txt"));
        } else {
            content = null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ImageList obj = objectMapper.readValue(content, ImageList.class);

        return obj.createList();
    }

    // This method ensures the program only responds with mock data for bulldogs if the API is down
    private static Map<String, List<String>> BreedMapMockRequest() throws IOException {

        byte[] content = Files.readAllBytes(Paths.get("resources/mock_bulldog_subbreeds.txt"));

        ObjectMapper objectMapper = new ObjectMapper();
        AllBreeds obj = objectMapper.readValue(content, AllBreeds.class);

        return obj.createMap();
    }

    // Tis method will check the users breed selections to see if we can utilize the current call
    // to update our bulldog mock data, and decides which bulldog mock call will be updated
    private static int InitiateMockUpdate(String breedselection, String subbreedselection){

        int update;

        if(breedselection.equals("bulldog") && subbreedselection==null){
            update = 1;
        } else if(breedselection.equals("bulldog") && subbreedselection.equals("boston")){
            update = 2;
        } else if(breedselection.equals("bulldog") && subbreedselection.equals("french")){
            update = 3;
        } else{
            update = 0;
        }
        return update;
    }

    // This method will take the raw response content and update the corresponding mock data
    private static void UpdateMockData(int update, String content){

        File newfile;

        if(update==1){
            File oldfile=new File("resources/mock_bulldog.txt");
            oldfile.delete();
            newfile=new File("resources/mock_bulldog.txt");
        } else if (update==2){
            File oldfile=new File("resources/mock_boston_bulldog.txt");
            oldfile.delete();
            newfile=new File("resources/mock_boston_bulldog.txt");
        } else {
            File oldfile = new File("resources/mock_french_bulldog.txt");
            oldfile.delete();
            newfile = new File("resources/mock_french_bulldog.txt");
        }

        try {
            FileWriter f2 = new FileWriter(newfile, false);
            f2.write(content);
            f2.close();
        } catch (IOException e) {
            System.out.println("Exception encountered: "+e);
        }
    }

    // This method constructs the breed list request
    // request is only sent once, on program launch, and response is stored to limit API calls
    private static Map<String, List<String>> BreedMapAPIRequest(String urlstr, int timeout) {

        // Put our request in a loop in case we get a timeout from the API
        boolean timeoutexcept = true;
        int timeoutcount = 0;
        while(timeoutexcept && timeoutcount<4) {
            try {

                URL url = new URL(GET_URL + urlstr);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("User-Agent", USER_AGENT);
                con.setConnectTimeout(timeout);
                con.setReadTimeout(timeout);

                int status = con.getResponseCode();

                // run the status code through our downtime simulator
                status = RequestProxy(status);

                // we'll treat all redirects as errors for simplicity
                if (status != 200) {
                    return BreedMapMockRequest();
                }

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                //process the relevant json data type

                ObjectMapper objectMapper = new ObjectMapper();
                AllBreeds obj = objectMapper.readValue(content.toString(), AllBreeds.class);
                timeoutexcept = false;
                return obj.createMap();

            } catch (Exception e) {
                // We caught a real timeout from the API, lets sleep it off and retry
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (Exception e1){
                    System.out.println("Exception encountered: "+e1);
                }
                timeoutcount++;
            }
        }
        try {
            return BreedMapMockRequest();
        } catch (Exception e) {
            System.out.println("Exception encountered: "+ e);
        }
        return null;
    }

    // This method constructs the request for breed and sub-breed images
    private static List<String> ImageListAPIRequest(String urlstr, int timeout, int update) {

        boolean timeoutexcept = true;
        while(timeoutexcept) {
            try {

                URL url = new URL(GET_URL + urlstr);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("User-Agent", USER_AGENT);
                con.setConnectTimeout(timeout);
                con.setReadTimeout(timeout);

                int status = con.getResponseCode();

                // run the status code through our downtime simulator
                status = RequestProxy(status);

                // we'll treat all non-200 codes as errors for simplicity
                if (status != 200) {
                    return ImageListMockRequest(urlstr);
                }

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                String rawContentString = content.toString();

                ObjectMapper objectMapper = new ObjectMapper();
                ImageList obj = objectMapper.readValue(rawContentString, ImageList.class);

                if (update != 0) {
                    UpdateMockData(update, rawContentString);
                }
                timeoutexcept = false;
                return obj.createList();

            } catch (Exception e) {
                // We caught a real timeout from the API, lets sleep it off and retry
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (Exception e1){
                    System.out.println("Exception encountered: "+e1);
                }
            }
        }
        return null;
    }

    // This is the bulk of the program
    // method processes user inputs, triggers API calls, and delivers user readable info
    private static List<String> getBreedSelection() {

        boolean ChoiceSuccess = false;
        List<String> SelectedBreed;
        String urlstr = "breeds/list/all";

        Map<String, List<String>> breedmap = BreedMapAPIRequest(urlstr, 5000);

        while (!ChoiceSuccess) {
            System.out.println("Please enter a breed: ");
            Scanner scan1 = new Scanner(System.in);
            String breed = scan1.next().toLowerCase();

            for(String key : breedmap.keySet()){

                if (key.equals(breed)){
                    ChoiceSuccess=true;
                    if(breedmap.get(key).isEmpty()){

                        System.out.println("\nFetching random image of a(n) "+breed+"...\n");

                        List<String> imagelist = getImages(breed, null);

                        System.out.println(selectRandomImage(imagelist));

                        ChoiceSuccess=true;

                    } else {
                        int SubbreedCount = 0;

                        for(String subbreed : breedmap.get(key)){
                            SubbreedCount++;
                            System.out.println(subbreed);
                        }

                        System.out.println("\nFound "+SubbreedCount+" Sub-Breeds of "+breed+"s.\n");
                        boolean inputbool = false;
                        while(!inputbool) {

                            System.out.println("Would you like to view an image of a random " + breed + ", or an image from a " +
                                    "specific Sub-Breed ?\n(Enter 'Random' or 'Sub-Breed'):");

                            Scanner scan2 = new Scanner(System.in);
                            String rawdata = scan2.next().toLowerCase();


                            if (rawdata.equals("sub-breed")) {

                                String subbreedselection = "default";

                                boolean foundsubbreed = false;

                                while(!foundsubbreed) {
                                    System.out.println("Enter Sub-Breed name:");

                                    Scanner scan3 = new Scanner(System.in);
                                    subbreedselection = scan3.next().toLowerCase();

                                    // make sure that the sub-breed actually exists
                                    for (String subbreed : breedmap.get(key)) {
                                        if (subbreed.equals(subbreedselection)) {
                                            foundsubbreed = true;
                                            break;
                                        }
                                    }
                                    if(!foundsubbreed) {
                                        System.out.println("Sorry, I'm not familiar with that sub-breed. Try again.");
                                    }
                                }

                                System.out.println("\nFetching image of "+subbreedselection+" "+breed+"...\n");

                                List<String> imagelist = getImages(breed, subbreedselection);

                                System.out.println(selectRandomImage(imagelist)+"\n");

                                inputbool = true;

                            } else if (rawdata.equals("random")) {

                                System.out.println("\nFetching images of "+breed+"s...\n");

                                List<String> imagelist = getImages(breed, null);

                                System.out.println(selectRandomImage(imagelist)+"\n");

                                inputbool = true;

                            } else {
                                System.out.println("Sorry, I didn't understand that. Try again.");
                            }
                        }
                    }
                }
            }
            if(!ChoiceSuccess) {
                System.out.println("Breed not found in list, try again.");
            }
        }
        return null;
    }

    // Used to randomly pick a single image from an entire image collection
    private static String selectRandomImage(List<String> imagelist){
        Random rand = new Random();

        int imagecount = 0;
        for (String image : imagelist) {
            imagecount++;
        }

        int n = rand.nextInt(imagecount+1);
        return imagelist.get(n);
    }

    // Constructs the Image API url based on user choices and passes the url to ImageListAPIRequest
    private static List<String> getImages(String breedselection, String subbreedselection){
        String urlstr;
        int update;

        if(subbreedselection!=null){
            urlstr = "breed/"+breedselection+"/"+subbreedselection+"/images";
        } else {
            urlstr = "breed/"+breedselection+"/images";
        }

        //check to see if we can use this opportunity to update our mock data
        update = InitiateMockUpdate(breedselection, subbreedselection);

        return ImageListAPIRequest(urlstr, 5000, update);
    }

    // loads the configuration variables and launches getBreedSelection.
    public static void main(String[] args) {

        System.out.println("Welcome to K9Query!");
        System.out.println("...................");

        try {


            String path = new File(".").getCanonicalPath();
            String appConfigPath = path + "/src/com/avenstewart/config.properties";

            Properties appProps = new Properties();
            appProps.load(new FileInputStream(appConfigPath));
            simulate_downtime = appProps.getProperty("simulate_downtime");
            USER_AGENT = appProps.getProperty("USER_AGENT");

        } catch (Exception e) {
            System.out.println("Encountered Exception: " + e);
        }

        getBreedSelection();
        System.out.println("Goodbye!");
    }
}
