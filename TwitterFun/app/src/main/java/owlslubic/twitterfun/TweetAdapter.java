package owlslubic.twitterfun;

/**
 * Created by owlslubic on 10/1/16.
 */


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import java.util.ArrayList;

import owlslubic.twitterfun.models.Tweet;


public class TweetAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Tweet> tweets;
    private DetailFrag.MyInterface listener;

    public TweetAdapter(Context context, ArrayList<Tweet> tweets, DetailFrag.MyInterface listener) {
        this.context = context;
        this.tweets = tweets;
        this.listener = listener;
        Log.d(TweetAdapter.class.getName(), "Tweets size: " + this.tweets.size());
    }

    @Override
    public int getCount() {
        return tweets.size();
    }

    @Override
    public Object getItem(int position) {
        return tweets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
//        return tweets.get(position).getId();
    }

    public long getTweetIdFromListView(int position){
        return tweets.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(
                    android.R.layout.simple_list_item_2, null);
        } else {
            view = convertView;
        }

        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

        text2.setTextColor(Color.BLUE);

        text1.setText(tweets.get(position).getText());
        text2.setText(tweets.get(position).getCreatedAt());

        return view;
    }





}
