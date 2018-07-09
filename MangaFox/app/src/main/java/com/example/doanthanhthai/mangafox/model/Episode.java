package com.example.doanthanhthai.mangafox.model;

import java.io.Serializable;

/**
 * Created by DOAN THANH THAI on 7/5/2018.
 */

public class Episode implements Serializable {
    public String title;
    public String name;
    public int curNum;
    public String url;
    public String image;
    public int viewCount;
    public VideoContent videoContent;
}
