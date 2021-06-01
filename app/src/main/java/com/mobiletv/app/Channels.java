package com.mobiletv.app;

public class Channels {

    private String Title;
    private String Description;
	private String Thumbnail;
    private String Url;
    private String Key;

    public Channels() {

    }

    public Channels(String title, String description, String thumbnail, String url, String key) {
        Title = title;
        Description = description;
        Thumbnail = thumbnail;
		Url = url;
		Key = key;
    }


    public String getTitle() {
        return Title;
    }

    public String getDescription() {
        return Description;
    }

	public String getThumbnail() {
		return Thumbnail;
	}

    public String getUrl() {
        return Url;
    }

    public String getKey() {
        return Key;
    }


    public void setTitle(String title) {
        Title = title;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setThumbnail(String thumbnail) {
        Thumbnail = thumbnail;
    }

	public void setUrl(String url) {
		Url = url;
	}

    public void setKey(String key) {
        Key = key;
    }
}
