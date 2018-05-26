package com.reyansh.audio.audioplayer.free.Lastfmapi.Models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by REYANSH on 3/13/2017.
 */

public class ArtistModel {

    @SerializedName("artist")
    public Artist artist;

    public static class Image {
        @SerializedName("#text")
        public String url;
        @SerializedName("size")
        public String size;
    }

    public static class Stats {
        @SerializedName("listeners")
        public String listeners;
        @SerializedName("playcount")
        public String playcount;
    }


    public static class Similar {
        @SerializedName("artist")
        public ArrayList<Artist> artist;

        public static class Artist {
            @SerializedName("name")
            public String name;
            @SerializedName("url")
            public String url;
            @SerializedName("image")
            public ArrayList<Image> image;
        }
    }

    public static class Tag {
        @SerializedName("name")
        public String name;
        @SerializedName("url")
        public String url;
    }

    public static class Tags {
        @SerializedName("tag")
        public ArrayList<Tag> tag;
    }

    public static class Link {
        @SerializedName("#text")
        public String link;
        @SerializedName("rel")
        public String rel;
        @SerializedName("href")
        public String href;
    }

    public static class Links {
        @SerializedName("link")
        public Link link;
    }

    public static class Bio {
        @SerializedName("links")
        public Links links;
        @SerializedName("published")
        public String published;
        @SerializedName("summary")
        public String summary;
        @SerializedName("content")
        public String content;
    }

    public static class Artist {
        @SerializedName("name")
        public String name;
        @SerializedName("mbid")
        public String mbid;
        @SerializedName("url")
        public String url;
        @SerializedName("image")
        public ArrayList<Image> image;
        @SerializedName("streamable")
        public String streamable;
        @SerializedName("ontour")
        public String ontour;
        @SerializedName("stats")
        public Stats stats;
        @SerializedName("similar")
        public Similar similar;
        @SerializedName("tags")
        public Tags tags;
        @SerializedName("bio")
        public Bio bio;
    }
}
