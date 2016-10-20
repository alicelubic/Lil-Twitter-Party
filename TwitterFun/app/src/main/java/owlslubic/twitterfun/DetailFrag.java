package owlslubic.twitterfun;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by owlslubic on 10/5/16.
 */

public class DetailFrag extends Fragment implements View.OnClickListener{
    private static final String TAG = "DetailFrag";
    String mText, mDate;
    TextView mTvText, mTvDate;
    MyInterface mCallback;
    CoordinatorLayout mCoordinator;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (MyInterface)context;


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_frag, container, false);
        mTvText = (TextView) view.findViewById(R.id.frag_tweet_text);
        mTvDate = (TextView) view.findViewById(R.id.frag_tweet_date);
        mCoordinator = (CoordinatorLayout) view.findViewById(R.id.coordinator);

        mText = getArguments().getString("text");
        mDate = getArguments().getString("date");

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTvText.setText(mText);
        mTvDate.setText(mDate);
        mCoordinator.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        mCallback.detachFrag("tag");
    }


    public interface MyInterface {
        public void showTweetDetail(long id);

        public void launchDetailFrag(String text, String date);

        public void detachFrag(String tag);
    }

}
