package com.bloom.java.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import com.bloom.java.service.CalfloraService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
@RequestMapping("/apiview")
public class ApiViewController {
    @GetMapping("/get-observations")
    public String getObservations(
            @RequestParam(value = "q", required = false, defaultValue = "Diplacus aurantiacus") String query,
            Model model) {
        List<String> observations = CalfloraService.getObservations(query);
        

        List<Date> dates = new ArrayList<Date>();

        for (int i = 1; i < observations.size(); i++) {
            String[] columns = observations.get(i).split(",");
            String dateString = columns[1].replace("\"", "").trim(); // Remove quotes

            // Parse the date string
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(dateString);
                dates.add(date);
            } catch (ParseException e) {
                e.printStackTrace(); // Handle parsing error
            }
        }

        model.addAttribute("observations", observations);

        Map<String, List<Date>> splitDates = splitDatesByYear(dates);

        // Count dates by month
        List<Double> pre1950Counts = percentageOfTotal(
            countDatesByMonth(
                splitDates.get("pre-1950")));
        List<Double> post1950Counts = percentageOfTotal(countDatesByMonth(splitDates.get("post-1950")));

        model.addAttribute("pre1950Counts", pre1950Counts);
        model.addAttribute("pre1950Total", splitDates.get("pre-1950").size());
        model.addAttribute("post1950Counts", post1950Counts);
        model.addAttribute("post1950Total", splitDates.get("post-1950").size());

        


        return "observations";
    }

    // Split dates by pre-1950 and post-1950
    private static Map<String, List<Date>> splitDatesByYear(List<Date> dates) {
        Map<String, List<Date>> splitDates = new HashMap<>();
        splitDates.put("pre-1950", new ArrayList<>());
        splitDates.put("post-1950", new ArrayList<>());

        for (Date date : dates) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);

            if (year < 1950) {
                splitDates.get("pre-1950").add(date);
            } else {
                splitDates.get("post-1950").add(date);
            }
        }
        return splitDates;
    }

    // Count the dates by month, where 0 = January, 11 = December
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

    private static List<Double> percentageOfTotal(List<Integer> counts) {
        int total = counts.stream().mapToInt(Integer::intValue).sum();
        List<Double> percentages = new ArrayList<Double>();
        for (int count : counts) {
            double percentage = ((double) count / total) * 100;
            percentages.add(percentage);
        }

        return percentages;
    }
    
}