package config;

import allure.AllureAttachment;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

import static config.Constants.BASE_URL;

public class RetrofitConfig {
    public static Retrofit buildRetrofitClient() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(new OkHttpClient().newBuilder()
                        .addNetworkInterceptor(new HttpLoggingInterceptor(AllureAttachment::logCollector)
                                .setLevel(HttpLoggingInterceptor.Level.BODY))
                        .readTimeout(5, TimeUnit.SECONDS)
                        .writeTimeout(5, TimeUnit.SECONDS)
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .build())
                .build();
    }
}
