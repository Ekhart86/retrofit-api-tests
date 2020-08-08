package tests;

import config.RetrofitConfig;
import enums.ContentType;
import model.AuthorizeResponse;
import model.SaveDataRequest;
import model.SaveDataResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import retrofit2.Response;
import service.ApiService;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static config.Constants.VALID_LOGIN;
import static config.Constants.VALID_PASSWORD;
import static enums.ContentType.JSON;
import static enums.ContentType.URLENCODED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ApiTest {

    private final static ApiService apiService = RetrofitConfig.buildRetrofitClient().create(ApiService.class);
    private String token;

    @Test(description = "/ping/ - positive test")
    public void pingTest() throws IOException {
        Response<ResponseBody> response = apiService.ping().execute();
        assertThat(response.code(), equalTo(200));
        assertThat(response.body(), notNullValue());
        assertThat(response.message(), equalTo("OK"));
    }

    @Test(description = "/authorize/ - positive test")
    public void authorizePositiveTest() throws IOException {
        Response<AuthorizeResponse> response = apiService.authorize(getLoginFormParams(VALID_LOGIN, VALID_PASSWORD)).execute();
        assertThat(response.code(), equalTo(200));
        assertThat(response.body(), notNullValue());
        assertThat(response.body().getToken(), notNullValue());
        assertThat(response.body().getToken().length(), equalTo(36));
        token = "Bearer " + response.body().getToken();
    }

    @Test(description = "/api/save_data/ - application/x-www-form-urlencoded - negative test", dependsOnMethods = "authorizePositiveTest")
    public void saveDataUrlencodedNegativeTest() throws IOException {
        Response<SaveDataResponse> response = getSaveResponseWithStatus("ERROR", URLENCODED, 20);
        assertThat(response.body(), notNullValue());
        assertThat(response.body().getError(), equalTo("I dont like this payload"));
    }

    @Test(description = "/api/save_data/ - application/x-www-form-urlencoded - positive test", dependsOnMethods = "authorizePositiveTest")
    public void saveDataUrlencodedPositiveTest() throws IOException {
        Response<SaveDataResponse> response = getSaveResponseWithStatus("OK", URLENCODED, 20);
        assertThat(response.body(), notNullValue());
        assertThat(response.body().getId(), is(greaterThan(0)));
    }

    @Test(description = "/api/save_data/ - application/json - negative test", dependsOnMethods = "authorizePositiveTest")
    public void saveDataJsonNegativeTest() throws IOException {
        Response<SaveDataResponse> response = getSaveResponseWithStatus("ERROR", JSON, 20);
        assertThat(response.body(), notNullValue());
        assertThat(response.body().getError(), equalTo("I dont like this payload"));
    }

    @Test(description = "/api/save_data/ - application/json - positive test", dependsOnMethods = "authorizePositiveTest")
    public void saveDataJsonPositiveTest() throws IOException {
        Response<SaveDataResponse> response = getSaveResponseWithStatus("OK", JSON, 20);
        assertThat(response.body(), notNullValue());
        assertThat(response.body().getId(), is(greaterThan(0)));
    }

    @Test(description = "/authorize/ - negative test", dataProvider = "NegativeAuthorizeTests")
    public void authorizeNegativeTest(String username, String password) throws IOException {
        Response<AuthorizeResponse> response = apiService.authorize(getLoginFormParams(username, password)).execute();
        assertThat(response.code(), equalTo(403));
    }

    @DataProvider(name = "NegativeAuthorizeTests")
    public static Object[][] negativeAuthorizeTests() {
        return new Object[][]{{"supertest", ""}, {"", "superpassword"}, {"supertest", "fail_password"}, {"fail_user", "superpassword"}, {"", ""}};
    }

    private static HashMap<String, RequestBody> getLoginFormParams(String username, String password) {
        HashMap<String, RequestBody> params = new LinkedHashMap<>();
        params.put("username", RequestBody.create(MediaType.parse("text/plain"), username));
        params.put("password", RequestBody.create(MediaType.parse("text/plain"), password));
        return params;
    }

    private Response<SaveDataResponse> getSaveResponseWithStatus(String status, ContentType contentType, Integer iteration) throws IOException {
        Response<SaveDataResponse> response;
        for (int i = 0; i < iteration; i++) {
            String payload = String.valueOf(ThreadLocalRandom.current().nextInt(1, 100));
            if(contentType.equals(JSON)) {
                response = apiService.saveDataWithJsonBody(token, new SaveDataRequest(payload)).execute();
            } else if(contentType.equals(URLENCODED)) {
                response = apiService.saveDataWithUrlencodedBody(token, RequestBody.create(MediaType.parse("text/plain"), payload)).execute();
            } else {
                throw new IllegalArgumentException("Invalid Content Type " + contentType);
            }
            assertThat(response, notNullValue());
            assertThat(response.code(), equalTo(200));
            assertThat(response.body(), notNullValue());
            if (response.body().getStatus().equals(status)) return response;
        }
        throw new AssertionError("'" + status + "' status not found in " + iteration + "iterations");
    }
}
