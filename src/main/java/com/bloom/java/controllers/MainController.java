package com.bloom.java.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bloom.java.service.CalfloraService;
import com.bloom.java.service.TaxaLoader;

@Controller
public class MainController {
    
    // Homepage
    // given a Calflora CRN, retrieve that plants observations and bloom period
    // then plot a histogram of observations and bloom period
    @GetMapping("/")
    public String index(
            @RequestParam(value = "taxon", required = false, defaultValue = "Lupinus albifrons") String taxon,
            @RequestParam(value = "crn", required = false, defaultValue = "5097") String crn,
            Model model) {
        List<String> observations = CalfloraService.getObservations(taxon);
        
        List<Date> dates = new ArrayList<Date>();

        for (int i = 1; i < observations.size(); i++) {
            String[] columns = observations.get(i).split(",");
            String dateString = columns[1].replace("\"", "").trim();

            // Parse the date string
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(dateString);
                dates.add(date);
            } catch (ParseException e) {
                continue;
            }
        }

        model.addAttribute("observations", observations);

        // Count dates by month
        List<Integer> observationsByMonth = 
            countDatesByMonth(
                dates);
        model.addAttribute("observations", observationsByMonth);
        model.addAttribute("observationsTotal", observations.size());

        int maxObservation = Collections.max(observationsByMonth);

        List<Integer> blooms = CalfloraService.getBlooms(crn).stream()
            .map(bloom -> bloom * maxObservation)
            .collect(Collectors.toList());

        model.addAttribute("blooms", blooms);

        model.addAttribute("taxon", taxon);
        model.addAttribute("crn", crn);

        return "observations";
    }

    // Given a string query
    // Return the 100 closest species

    // Query is differentiated as string (a word within the scientific name)
    // or the whole scientific name depending on the presence of a space

    // TODO: Measure quality of response
    @GetMapping("/fuzzy")
    public String fuzzy(
            @RequestParam(value = "q", required = false, defaultValue = "") String query, Model model) {
                List<Map<String, Object>> taxa;

                // minor optimization that solves for when the search term is
                // a single unspecified rank that's spelled correctly.
                if (query.contains(" ")) {
                    // DRY this, only the distance method is different
                    taxa = TaxaLoader.getInstance().getData().stream()
                    .map(item -> new AbstractMap.SimpleEntry<>(item, item.distance(query))) // Map to (string, distance)
                    .sorted(Comparator.comparingInt(Map.Entry::getValue)) // Sort by distance
                    .limit(100) // Take the top `limit` items
                    .map(entry -> Map.of("taxa", entry.getKey(), "distance", entry.getValue())) // Convert to JSON-like map
                    .toList(); // Collect as List
                } else {
                    // DRY this, only the distance method is different
                    taxa = TaxaLoader.getInstance().getData().stream()
                    .map(item -> new AbstractMap.SimpleEntry<>(item, item.minDistanceAnyRank(query))) // Map to (string, distance)
                    .sorted(Comparator.comparingInt(Map.Entry::getValue)) // Sort by distance
                    .limit(100) // Take the top `limit` items
                    .map(entry -> Map.of("taxa", entry.getKey(), "distance", entry.getValue())) // Convert to JSON-like map
                    .toList(); // Collect as List
                }
                
                model.addAttribute("taxa", taxa);
                model.addAttribute("query", query);

                return "fuzzy";
            }


    // Count the dates by month, where 0 = January, 11 = December
    // TODO: Refactor this into some data processing utility
    private static List<Integer> countDatesByMonth(List<Date> dates) {
        List<Integer> monthCount = new ArrayList<>(Collections.nCopies(12, 0)); // 12 months, initialize to 0

        for (Date date : dates) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int month = cal.get(Calendar.MONTH); // 0 = January, 1 = February, ..., 11 = December

            monthCount.set(month, monthCount.get(month) + 1); // Increment count for the corresponding month
        }

        return monthCount;
    }
}
