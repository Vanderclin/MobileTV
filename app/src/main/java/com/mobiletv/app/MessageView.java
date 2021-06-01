package com.mobiletv.app;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MessageView
{
    private String key;
    private String name;
    private String message;
    private String time;

    public MessageView()
	{

    }

    public MessageView(String key, String name, String message, String time)
	{
        key = key;
        name = name;
        message = message;
        time = time;
    }

    public String getKey()
	{
        return key;
    }

    public String getName()
	{
        return name;
    }

    public String getMessage()
	{
        return message;
    }

	public String getTime() {
		return time;
	}
}
