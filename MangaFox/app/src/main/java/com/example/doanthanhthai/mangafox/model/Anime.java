package com.example.doanthanhthai.mangafox.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by DOAN THANH THAI on 7/5/2018.
 */

public class Anime implements Serializable {
    public String title;
    public String url;
    public String image;
    public int maxEpisode;
    public int minEpisode;
    public String episodeInfo;
    public String bannerImage;
    public Episode episode;
}
