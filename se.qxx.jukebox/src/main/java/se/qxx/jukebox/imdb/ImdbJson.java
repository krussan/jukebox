package se.qxx.jukebox.imdb;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1

import java.io.IOException;
import java.util.ArrayList;

public class ImdbJson {

    public static ImdbRoot parseJson(String json) throws IOException {
        ObjectMapper om = new ObjectMapper();
        return om.readValue(json, ImdbRoot.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Actor{
        @JsonProperty("@type")
        public String type;
        public String url;
        public String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AggregateRating{
        @JsonProperty("@type")
        public String type;
        public int ratingCount;
        public int bestRating;
        public int worstRating;
        public double ratingValue;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author{
        @JsonProperty("@type")
        public String type;
        public String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Creator{
        @JsonProperty("@type")
        public String type;
        public String url;
        public String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Director{
        @JsonProperty("@type")
        public String type;
        public String url;
        public String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemReviewed{
        @JsonProperty("@type")
        public String type;
        public String url;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Review{
        @JsonProperty("@type")
        public String type;
        public ItemReviewed itemReviewed;
        public Author author;
        public String dateCreated;
        public String inLanguage;
        public String name;
        public String reviewBody;
        public ReviewRating reviewRating;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReviewRating{
        @JsonProperty("@type")
        public String type;
        public int worstRating;
        public int bestRating;
        public int ratingValue;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImdbRoot{
        @JsonProperty("@context")
        public String context;
        @JsonProperty("@type")
        public String type;
        public String url;
        public String name;
        public String image;
        public String description;
        public Review review;
        public AggregateRating aggregateRating;
        public String contentRating;
        public ArrayList<String> genre;
        public String datePublished;
        public String keywords;
        public Trailer trailer;
        public ArrayList<Actor> actor;
        public ArrayList<Director> director;
        public ArrayList<Creator> creator;
        public String duration;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Thumbnail{
        @JsonProperty("@type")
        public String type;
        public String contentUrl;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Trailer{
        @JsonProperty("@type")
        public String type;
        public String name;
        public String embedUrl;
        public Thumbnail thumbnail;
        public String thumbnailUrl;
        public String description;
    }

}
