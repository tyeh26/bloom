package com.bloom.java.service;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CalfloraService {
    public static List<String> getObservations(String binomial) {
        List<String> observations = new ArrayList<String>();

        String encodedBinomial = "";
        try {
            encodedBinomial = URLEncoder.encode(binomial, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String fileUrl = "https://www.calflora.org/app/download?xun=136445&format=CSV&cols=ID,Date&cell=t&georef=a&georeferenced=t&ostatus=a&pcount=1&doc_type=rs&natural_status=any&taxon=" + encodedBinomial + "&addnloc=t&cch=t&cnabh=t&wint=r";
        
        try {
            String localFilePath = "output.csv";
            // Step 1: Download the file
            downloadFile(fileUrl, localFilePath);

            // Step 2: Load and print the file contents
            observations = printFileContents(localFilePath);

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        return observations;
    }

    public static List<Integer> getBlooms(String crn) {
        List<Integer> months = new ArrayList<Integer>();
        try {
            String targetUrl = "https://www.calflora.org/app/taxon?crn=" + crn;
            URL url = new URL(targetUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            
            Document doc = Jsoup.parse(response.toString());
        
            // Select the div with id="c-bloom"
            Element bloomDiv = doc.select("div#c-bloom").first();
            
            if (bloomDiv == null) {
                return months;  // Return an empty array if #c-bloom is not found
            }

            // Select all path elements inside the bloom div
            Elements paths = bloomDiv.select("path");

            // Define the mapping of colors to values
            Map<String, Integer> colorMap = new HashMap<>();
            colorMap.put("#ffffff", 0);  // white
            colorMap.put("#95f5ff", 1);  // light blue

            // Iterate through the paths and map the color to the corresponding month
            int monthIndex = 0;
            for (Element path : paths) {
                String style = path.attr("style");
                String fillColor = style.replaceAll(".*fill:([^;]+).*", "$1").toLowerCase();  // Extract the fill color
                
                months.add(colorMap.getOrDefault(fillColor, 0));  // Default to 0 if color is unrecognized
                monthIndex++;

                if (monthIndex >= 12) {
                    break;  // Stop after processing 12 months
                }
            }


            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return months;
    }


    public static List<Map<String, String>> search(String query) {
        List<Map<String, String>> speciesList = new ArrayList<>();

        try {
            // The target URL
            String targetUrl = "https://www.calflora.org/app/userdata";
            
            // Create a URL object
            URL url = new URL(targetUrl);
            
            // Open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set HTTP method to POST
            connection.setRequestMethod("POST");

            // Set headers
            connection.setRequestProperty("X-GWT-Permutation", "98DE2A450D742E9C697BC3BB234F7808");
            connection.setRequestProperty("X-GWT-Module-Base", "https://www.calflora.org/entry/com.taxon.TaxonLookup4/");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
            connection.setRequestProperty("Content-Type", "text/x-gwt-rpc; charset=UTF-8");

            // Enable input and output streams for the connection
            connection.setDoOutput(true);

            // The data payload
            String payload = "7|0|10|https://www.calflora.org/entry/com.search.SearchB/|CCD946811B7108DD7F57B9B1BDC37CBB|com.cfapp.client.wentry.UserDataService|oneList|java.lang.String/2004016611|java.util.HashMap/1797211028|specieslist|136445|namesoup|" + query +  "|1|2|3|4|3|5|5|6|7|8|6|1|5|9|5|10|";

            // Write the payload to the output stream
            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes("UTF-8"));
                os.flush();
            }
            
            try (InputStream responseStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                String rsp = response.toString();
                String regex = "\"([A-Za-z]+(?: [a-z]+(?: var\\.[a-z]+)?)?)\",\"(\\d+)\"";

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(rsp);

                // List to hold maps of species and id
                

                // Iterate over all matches
                while (matcher.find()) {
                    // Create a map for each species and ID pair
                    Map<String, String> speciesMap = new HashMap<>();
                    speciesMap.put("species", matcher.group(1));  // Species name
                    speciesMap.put("crn", matcher.group(2));  // ID number

                    // Add the map to the list
                    speciesList.add(speciesMap);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return speciesList;
    }

    // Downloads a file from a given URL and saves it locally
    private static void downloadFile(String fileUrl, String localFilePath) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Check if the response is OK (HTTP 200)
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(localFilePath)) {

                byte[] buffer = new byte[1024];
                int bytesRead;

                // Read data in chunks and write to the local file
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } else {
            throw new IOException("Failed to download file. HTTP Response Code: " + responseCode);
        }
    }

    // Reads a file and prints its contents
    private static List<String> printFileContents(String filePath) throws IOException {
        List<String> contents = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contents.add(line);
            }
        }
        return contents;
    }
}
