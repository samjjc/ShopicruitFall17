package com.example.johnny.shopicruitfall17;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.johnny.shopicruitfall17.models.Order;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class MainActivity extends AppCompatActivity {

    TextView tvTotalRevenue;
    TextView tvTotalQuantity;
    TextView tvSingleRevenue;
    TextView tvSingleQuantity;

    double totalRevenue = 0;
    double singleRevenue = 0;

    private final String token = "c32313df0d0ef512ca64d5b336a0d7c6";

    private CompositeSubscription mSubscriptions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSubscriptions = new CompositeSubscription();
        initViews();
        //I didn't really know whether you wanted me to parse only the first page or all of them,
        //so i just did both
        loadSinglePageData();
        loadAllData();
    }

    private void initViews() {
        tvTotalRevenue = (TextView) findViewById(R.id.total_revenue);
        tvTotalQuantity = (TextView) findViewById(R.id.total_quantity);
        tvSingleRevenue = (TextView) findViewById(R.id.single_revenue);
        tvSingleQuantity = (TextView) findViewById(R.id.single_quantity);
    }


    //===============================SINGLE PAGE======================================================

    //parse json and get relevant values
    //i hate that i'm using global variables, and that i'm not doing this in the response
    //YOU CAN'T EVEN REDUCE A STREAM UNTIL API 24
    //the nice why that i would have done without global variables
    //is only possible with api 24+
    private void loadSinglePageData() {
        RetrofitInterface mRetrofit = getRetrofit();
        mSubscriptions.add(mRetrofit.getData(1, token)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .flatMap(response -> Observable.from(response.getOrders()))
                .doOnNext(order -> singleRevenue += order.getPrice())
                .flatMap(order -> Observable.from(order.getLineItems()))
                .filter(item -> item.getTitle().equals( "Aerodynamic Cotton Keyboard"))
                .reduce(0,(total,item) -> total + item.getQuantity())
                .subscribe(this::handleSingleResponse,this::handleError));
    }

    //print output
    private void handleSingleResponse(int i) {
        String revenueString = Double.toString(Math.round(singleRevenue * 100.0) / 100.0);
        tvSingleRevenue.setText("The revenue from the first page:\n$"+revenueString);
        tvSingleQuantity.setText("number of aerodynamic keyboards sold on first page:\n" +Integer.toString(i));

    }


    //===============================ALL PAGES======================================================
    //i already did a mini rant in my single data function explaining why this is so ugly
    private void loadAllData(){
        mSubscriptions.add(getAllPages(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .flatMap(order -> Observable.from(order))
                .doOnNext(order -> totalRevenue += order.getPrice())
                .flatMap(order -> Observable.from(order.getLineItems()))
                .filter(item -> item.getTitle().equals( "Aerodynamic Cotton Keyboard"))
                .reduce(0,(total,item) -> total + item.getQuantity())
                .subscribe(this::handleResponse,this::handleError));
    }

    //print output
    private void handleResponse(int i) {
        String revenueString = Double.toString(Math.round(totalRevenue * 100.0) / 100.0);
        tvTotalRevenue.setText("Total revenue:\n$"+revenueString);
        tvTotalQuantity.setText("Total number of aerodynamic keyboards sold:\n"+Integer.toString(i));
    }

    //recursive function that gets all pages and returns one big observable list of orders
    private Observable<List<Order>> getAllPages(int page){
        return getRetrofit().getData(page, token)
                .map(response -> response.getOrders())
                .concatMap(orders -> {
                    if(orders.isEmpty()) {
                        return Observable.just(orders);
                    }else{
                        return Observable.just(orders).concatWith(getAllPages(page+1));
                    }
                });
    }

    //===============================OTHER FUNCTIONS======================================================



    private void handleError(Throwable throwable) {
        Log.e("ee",throwable.toString());
    }

    //making the retrofit object
    private RetrofitInterface getRetrofit() {

        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        return new Retrofit.Builder()
                .baseUrl(RetrofitInterface.BASE_URL)
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(RetrofitInterface.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }
}
