package com.freedom.androidexample;


import java.util.List;
import io.reactivex.Observable;
import retrofit2.http.GET;;
import retrofit2.http.PUT;
import retrofit2.http.Header;
import retrofit2.http.Body;
import retrofit2.http.Url;

public interface APIService {

    String BASE_URL = "https://api.freedomrobotics.ai/";

    @GET("accounts/" + Constants.account_id + "/devices/" + Constants.device_id + "/commands")
    Observable<List<FreedomCommand>> getFreedomCommand( @Header("mc_token") String mc_token, @Header("mc_secret") String mc_secret);

    @GET("accounts/" + Constants.account_id + "/devices/" + Constants.device_id + "/videos?pre_signed=true")
    Observable<List<List<String>>> getFreedomVideoFrameURL( @Header("mc_token") String mc_token, @Header("mc_secret") String mc_secret);

    @GET
    Observable<String> getFreedomVideoFrame( @Url String url);

    @PUT("accounts/" + Constants.account_id + "/devices/" + Constants.device_id + "/data")
    Observable<FreedomResponse> sendFreedomData( @Header("mc_token") String mc_token, @Header("mc_secret") String mc_secret,
                                                        @Body List<FreedomMessage> messages);
}
