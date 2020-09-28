package com.freedom.androidexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.freedom.androidexample.APIService.BASE_URL;

public class MainActivity extends AppCompatActivity {

    Retrofit retrofit;
    Retrofit retrofitAWS;
    TextView textView;
    ImageView videoImageView;
    APIService apiService;
    APIService apiServiceAWS;
    Disposable disposableCommands;
    Disposable disposableData;
    Disposable disposableVideo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        videoImageView = findViewById(R.id.videoImageView);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();


        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(APIService.class);

        retrofitAWS = new Retrofit.Builder()
                .baseUrl("https://video.freedomrobotics.ai/")
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        apiServiceAWS = retrofitAWS.create(APIService.class);


        disposableCommands = Observable.interval(1000, 5000,
                TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::callFreedomCommandsEndpoint, this::onError);

        disposableData = Observable.interval(1000, 5000,
                TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::callSendFreedomDataEndpoint, this::onError);

        disposableVideo = Observable.interval(1000, 500,
                TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::callFreedomVideoEndpoint, this::onError);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (disposableCommands.isDisposed()) {
            disposableCommands = Observable.interval(1000, 5000,
                    TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::callFreedomCommandsEndpoint, this::onError);
        }
        if (disposableData.isDisposed()) {
            disposableData = Observable.interval(1000, 5000,
                    TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::callSendFreedomDataEndpoint, this::onError);
        }
        if (disposableVideo.isDisposed()) {
            disposableVideo = Observable.interval(1000, 500,
                    TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::callFreedomVideoEndpoint, this::onError);
        }
    }

    private void callFreedomCommandsEndpoint(Long aLong) {
        Observable<List<FreedomCommand>> observable = apiService.getFreedomCommand(Constants.mc_token, Constants.mc_secret);
        observable.subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResultsFreedomCommands, this::handleError);
    }

    private void callFreedomVideoEndpoint(Long aLong) {
        Observable<List<List<String>>> observable = apiService.getFreedomVideoFrameURL(Constants.mc_token, Constants.mc_secret);
        observable.subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResultsFreedomVideoURL, this::handleError);
    }

    private void callSendFreedomDataEndpoint(Long aLong) {
        List<FreedomMessage> list = new ArrayList<>();
        FreedomMessage gps_data = new FreedomMessage();
        gps_data.topic = "/location";
        gps_data.type = "sensor_msgs/NavSatFix";
        NavSatFix location = new NavSatFix();
        location.latitude = 37.5494221;
        location.longitude = -122.30597979999999;
        gps_data.data = location;
        list.add(gps_data);
        FreedomMessage number = new FreedomMessage();
        number.topic = "/number_example";
        number.type = "std_msgs/Float32";
        NumberExample number_example = new NumberExample();
        Random random = new Random();
        number_example.data = 1000 * random.nextFloat();
        number.data = number_example;
        list.add(number);
        Observable<FreedomResponse> observable = apiService.sendFreedomData(Constants.mc_token, Constants.mc_secret, list);
        observable.subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResultsFreedomData, this::handleError);
    }

    private void onError(Throwable throwable) {
        Toast.makeText(this, "OnError in Observable Timer",
                Toast.LENGTH_LONG).show();
    }

    private void handleResultsFreedomCommands(List<FreedomCommand> commands) {

        if (!commands.isEmpty()) {
            FreedomCommand command = commands.get(0);
            textView.setText(command.message.msg);

        } else {
            textView.setText("WAITING FOR COMMANDS");
            Toast.makeText(this, "NO RESULTS FOUND",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void handleResultsFreedomVideoURL(List<List<String>> videoFrameURLs) {
        if(videoFrameURLs != null) {
            String videoURL = videoFrameURLs.get(0).get(1).split("https://video.freedomrobotics.ai/")[1];

            Observable<String> observable = apiServiceAWS.getFreedomVideoFrame(videoURL);
            observable.subscribeOn(Schedulers.newThread()).
                    observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResultsFreedomVideo, this::handleError);
        }
    }

    private void handleResultsFreedomVideo(String videoFrameBase64) {
        if(videoFrameBase64 != null) {
            //decode base64 string to image
            byte[] imageBytes = Base64.decode(videoFrameBase64, Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            videoImageView.setImageBitmap(decodedImage);
            videoImageView.requestLayout();
            videoImageView.getLayoutParams().height = 480;
            videoImageView.getLayoutParams().width = 640;
        }
    }



    private void handleResultsFreedomData(FreedomResponse response) {
        //Add your response handler here
    }

    private void handleError(Throwable t) {
        Toast.makeText(this, t.getMessage(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        disposableCommands.dispose();
        disposableData.dispose();
        disposableVideo.dispose();
    }
}
