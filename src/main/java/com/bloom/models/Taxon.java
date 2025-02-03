package com.bloom.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bloom.java.service.StringMetrics;

public class Taxon {
    public String genus;
    public String species;
    public String subspecies;
    public String variety;
    public String cultivar;

    private Taxon(Builder builder) {
        this.genus = builder.genus;
        this.species = builder.species;
        this.subspecies = builder.subspecies;
        this.variety = builder.variety;
        this.cultivar = builder.cultivar;
    }

    public int distance(String query) {
        return StringMetrics.levenshteinDistance(this.toString(), query);
    }

    public int minDistanceAnyRank(String query) {
        List<Integer> distances = new ArrayList<Integer>();
        distances.add(StringMetrics.levenshteinDistance(this.genus, query));

        if (this.species != null) {
            distances.add(StringMetrics.levenshteinDistance(this.species, query));
        }

        if (this.subspecies != null) {
            distances.add(StringMetrics.levenshteinDistance(this.subspecies, query));
        }

        if (this.variety != null) {
            distances.add(StringMetrics.levenshteinDistance(this.variety, query));
        }

        if (this.cultivar != null) {
            distances.add(StringMetrics.levenshteinDistance(this.cultivar, query));
        }

        return Collections.min(distances);
    }

    @Override
    public String toString() {
        String s = genus;

        if (species != null) {
            s = s + " " + species;
        }

        if (subspecies != null) {
            s = s + " ssp. " + subspecies;
        }

        if (variety != null) {
            s = s + " var. " + variety;
        }

        if (cultivar != null) {
            s = s + " '" + cultivar + "'";
        }

        return s;
    }

    public static class Builder {
        public String genus;
        public String species = null;
        public String subspecies = null;
        public String variety = null;
        public String cultivar = null;
        
        public Builder setGenus(String genus) {
            this.genus = genus;
            return this;
        }

        public Builder setSpecies(String species) {
            this.species = species;
            return this;
        }

        public Builder setSubspecies(String subspecies) {
            this.subspecies = subspecies;
            return this;
        }

        public Builder setVariety(String variety) {
            this.variety = variety;
            return this;
        }

        public Builder setCultivar(String cultivar) {
            this.cultivar = cultivar;
            return this;
        }

        public Taxon build() {
            return new Taxon(this);
        }
    }
}
