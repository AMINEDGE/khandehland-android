package com.hjri.khandeland.messages.networking;

import com.hjri.khandeland.messages.BuildConfig;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class Routes {

    private static final String CONTENT_TYPE_JSON = "Content-Type: application/json";
    private static final String ACCEPT_JSON = "Accept: application/json";

    private static final String API_V1_ENDPOINT = "api/v1/";

    public static final String MAIN_URL = BuildConfig.DEBUG ? "http://192.168.1.34:8000/" : "https://khandehland.hjri.ir/";
    public static final String BASE_URL = MAIN_URL + API_V1_ENDPOINT;



    // AppStart

    public interface AppStart {

        @Headers({ CONTENT_TYPE_JSON, ACCEPT_JSON })
        @POST("get-configs")
        Call<ConfigResponseBody> getConfigs(@Body SingleVersionBody body);

        @Headers({ CONTENT_TYPE_JSON, ACCEPT_JSON })
        @PUT("message/sync")
        Call<SyncResponseBody> sync(@Header("Authorization") String authHeader, @Body SyncBody body);
    }

    // Auth

    public interface Auth {

        @Headers({ CONTENT_TYPE_JSON, ACCEPT_JSON })
        @POST("register")
        Call<RegisterResponseBody> register(@Body RegisterBody body);

        @Headers({ CONTENT_TYPE_JSON, ACCEPT_JSON })
        @POST("oauth/token")
        Call<LoginResponseBody> login(@Body LoginBody body);
    }

    // Message

    public interface Message {

        @Headers({ CONTENT_TYPE_JSON, ACCEPT_JSON })
        @POST("message/list/{skip}/{take}")
        Call<MessageListResponseBody> listMessages(@Body SingleVersionBody body, @Path("skip") Integer skip, @Path("take") Integer take);

        @Headers({ CONTENT_TYPE_JSON, ACCEPT_JSON })
        @POST("message")
        Call<Void> submitMessage(@Header("Authorization") String authHeader, @Body MessageBody body);
    }

    // Favorite

    public interface Favorite {

        @Headers({ CONTENT_TYPE_JSON, ACCEPT_JSON })
        @POST("message/{id}/favorite/add")
        Call<Void> addToFavorite(@Header("Authorization") String authHeader, @Path("id") Integer messageId, @Body SingleVersionBody body);

        @Headers({ CONTENT_TYPE_JSON, ACCEPT_JSON })
        @HTTP(method = "DELETE", path = "message/{id}/favorite/remove", hasBody = true)
        Call<Void> removeFromFavorite(@Header("Authorization") String authHeader, @Path("id") Integer messageId, @Body SingleVersionBody body);
    }


}
