package com.example.johnny.shopicruitfall17;

import com.example.johnny.shopicruitfall17.models.Response;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by samuelcatherasoo on 2017-04-15.
 */

public interface RetrofitInterface {

    String BASE_URL = "https://shopicruit.myshopify.com/";

    @GET("admin/orders.json")
    Observable<Response> getData(@Query("page") int pageNumber, @Query("access_token") String token);

}
