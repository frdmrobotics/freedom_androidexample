package com.freedom.androidexample;
import com.google.gson.annotations.SerializedName;

public class FreedomCommand {
    @SerializedName("message")
    public Message message;
    @SerializedName("utc_time")
    public Double utc_time;
}
