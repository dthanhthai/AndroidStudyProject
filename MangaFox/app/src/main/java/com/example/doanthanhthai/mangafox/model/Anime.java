package com.example.doanthanhthai.mangafox.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by DOAN THANH THAI on 7/5/2018.
 */

public class Anime implements Serializable {
    public String title;
    public String orderTitle;
    public String url;
    public String image;
    public String bannerImage;
    public String coverImage;
    public int maxEpisode;
    public int minEpisode;
    public String episodeInfo;
    public int rank;
    public String rate;
    public int year;
    public String description;
    public String genres;
    public String duration;
    public List<Season> seasonList;
    public List<Episode> episodeList;
    public Episode episode;
}
