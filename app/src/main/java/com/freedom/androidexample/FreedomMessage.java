package com.freedom.androidexample;
import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

public class FreedomMessage {
    public String platform = "custom";
    @SerializedName("utc_time")
    public Long utcTime = Calendar.getInstance().getTimeInMillis() / 1000;
    @SerializedName("expiration_secs")
    public int expirationSecs = 60;
    public String topic;
    public String type;
    public Object data;
}
