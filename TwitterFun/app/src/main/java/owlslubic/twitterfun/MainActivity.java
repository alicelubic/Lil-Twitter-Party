package owlslubic.twitterfun;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import owlslubic.twitterfun.models.Tweet;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DetailFrag.MyInterface {


    private final static String TAG = MainActivity.class.getName();

    public String mAccessToken;

    private Button searchButton;
    private EditText editText;
    private ListView listView;
    DetailFrag.MyInterface mCallback;
    TweetAdapter mAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchButton = (Button) findViewById(R.id.search_button);
        editText = (EditText) findViewById(R.id.search_edit_text);
        listView = (ListView) findViewById(R.id.tweets_list);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTweets();
            }
        });


        mCallback = (DetailFrag.MyInterface) this;

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TwitterAppData.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        TwitterApiEndpointInterface twitterApi = retrofit.create(TwitterApiEndpointInterface.class);

        String plainString = TwitterAppData.CONSUMER_KEY + ":" + TwitterAppData.CONSUMER_SECRET;
        byte[] data = new byte[0];
        try {
            data = plainString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String base64 = Base64.encodeToString(data, Base64.NO_WRAP);

        Log.d(TAG, "Ready to make bearer req: " + base64);

        Call<ResponseBody> call = twitterApi.authorizeApplication("Basic " + base64, "application/x-www-form-urlencoded;charset=UTF-8", "client_credentials");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Call completed!");
                try {
                    String responseString = response.body().string();
                    Log.d(TAG, "Token: " + responseString);
                    JSONObject object = new JSONObject(responseString);
                    mAccessToken = object.getString("access_token");
                    Log.d(TAG, "Access Token: " + mAccessToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Bearer token call failed");
                Log.d(TAG, t.getMessage().toString());
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showTweetDetail(mAdapter.getTweetIdFromListView(position));
                Log.d(TAG, "onItemClick: tweet id is: " + mAdapter.getTweetIdFromListView(position));

            }
        });

    }


    private void getTweets() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TwitterAppData.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        TwitterApiEndpointInterface twitterApi = retrofit.create(TwitterApiEndpointInterface.class);

        String searchName = editText.getText().toString();
        Call<ResponseBody> call = twitterApi.userTimeline("Bearer " + mAccessToken, searchName, 2);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Call completed!");
                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Response: " + responseBody);
                    JSONArray tweets = new JSONArray(responseBody);

                    ArrayList<Tweet> tweetList = new ArrayList<Tweet>();

                    for (int i = 0; i < tweets.length(); i++) {
                        JSONObject object = tweets.getJSONObject(i);
                        String text = object.getString("text");
                        String date = object.getString("created_at");

                        String id = object.getString("id_str");
                        Log.d(TAG, "onResponse: response body object id is: " + object.getString("id_str"));

                        Tweet tweet = new Tweet();
                        tweet.setText(text);
                        tweet.setCreatedAt(date);
                        tweet.setId(Long.parseLong(id));
                        tweetList.add(tweet);
                    }

                    mAdapter = new TweetAdapter(MainActivity.this, tweetList, mCallback);
                    listView.setAdapter(mAdapter);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Get tweet call failed");
                Log.d(TAG, t.getMessage().toString());
            }
        });

    }

    @Override
    public void showTweetDetail(long id) {
        //setup json parser
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        //this gives you better logging i think
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        //retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TwitterAppData.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        //assign our api interface to retrofit
        TwitterApiEndpointInterface twitterApi = retrofit.create(TwitterApiEndpointInterface.class);


        Call<ResponseBody> call = twitterApi.showTweet("Bearer " + mAccessToken, id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: showTweet api call Succeeded!");
                Log.d(TAG, "onResponse: response.isSuccessful() = " + response.isSuccessful());

                //get the response info, parse JSON
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Response: " + responseBody);
                        JSONObject object = new JSONObject(responseBody);
                        String text = object.getString("text");
                        String date = object.getString("created_at");

                        //don't need to make a new object because he only did that to add it to a list
//                    Tweet tweet = new Tweet();
//                    tweet.setText(text);
//                    tweet.setCreatedAt(date);

                        //this is where I will launch frag and make the call all at once!
                        launchDetailFrag(text, date);
                    } else {
                        Toast.makeText(MainActivity.this, "Response<ResponseBody> response was not successful...", Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: showTweet api call failed");
                Log.d(TAG, "onFailure: error : " + t.getMessage());
                Toast.makeText(MainActivity.this, "api call failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void launchDetailFrag(String text, String date) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        DetailFrag frag = new DetailFrag();
        Bundle args = new Bundle();
        args.putString("text", text);
        args.putString("date", date);
        frag.setArguments(args);

        transaction.add(R.id.framelayout_container, frag, "tag")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void detachFrag(String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment frag = getSupportFragmentManager().findFragmentByTag(tag);
        transaction.detach(frag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .commit();
    }

    @Override
    public void onClick(View v) {

    }




}



