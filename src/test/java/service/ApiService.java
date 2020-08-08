package service;

import model.AuthorizeResponse;
import model.SaveDataRequest;
import model.SaveDataResponse;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface ApiService {

    @GET("/ping/")
    Call<ResponseBody> ping();

    @Multipart
    @POST("/authorize/")
    Call<AuthorizeResponse> authorize(@PartMap Map<String, RequestBody> params);

    @FormUrlEncoded
    @POST("/api/save_data/")
    Call<SaveDataResponse> saveDataWithUrlencodedBody(@Header("Authorization") String authorization, @Field("payload") RequestBody payload);

    @POST("/api/save_data/")
    Call<SaveDataResponse> saveDataWithJsonBody(@Header("Authorization") String authorization, @Body SaveDataRequest body);
}
