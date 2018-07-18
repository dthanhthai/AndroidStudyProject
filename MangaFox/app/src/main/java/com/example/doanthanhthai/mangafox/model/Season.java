package com.example.doanthanhthai.mangafox.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ThaiDT1 on 7/17/2018.
 */

public class Season implements Serializable {
    public String name;
    public String url;
    public List<Episode> episodeList;
}
