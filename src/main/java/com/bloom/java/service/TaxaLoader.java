package com.bloom.java.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.bloom.models.Taxon;


// Read and load taxa data into memory as a List of Taxon objects.
// Uses the Singleton pattern
public class TaxaLoader {
    private static TaxaLoader instance;
    private List<Taxon> taxa = new ArrayList<Taxon>();

    // Path to file with format, with a header row
    // genus,species,subspecies,variety,cultivar

    // All entries are unique
    // only genus is required
    // See models/Taxon.java for more

    // Exclude hybrids

    // Format is derived to process Calscape data, not necessarily to
    // reflect scientific accuracy.
    private static String filePath = "taxa.csv";

    private TaxaLoader() {
        this.taxa = loadData();
    }

    public static TaxaLoader getInstance() {
        if (instance == null) {
            synchronized (TaxaLoader.class) {
                if (instance == null) {
                    instance = new TaxaLoader();
                }
            }
        }
        return instance;
    }

    private List<Taxon> loadData() {
        List<Taxon> result = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            reader.skip(1); // Skip header row
            String[] line;
            while ((line = reader.readNext()) != null) {
                for (int i = 0; i < line.length; i++) {
                    if (line[i].trim().isEmpty()) {
                        line[i] = null;
                    }
                }

                Taxon taxon = new Taxon.Builder()
                                .setGenus(line[0])
                                .setSpecies(line[1])
                                .setSubspecies(line[2])
                                .setVariety(line[3])
                                .setCultivar(line[4])
                                .build();
                
                result.add(taxon);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        
        return result;

    }

    public List<Taxon> getData() {
        return taxa;
    }
}
