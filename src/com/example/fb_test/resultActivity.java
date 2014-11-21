package com.example.fb_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.fb_test.MainActivity.Task;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

public class resultActivity extends Activity {
	
	private static final String TAG_DATA = "data";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_GENDER = "gender";
    private static final String TAG_PHONE = "phone";
    private static final String TAG_PHONE_MOBILE = "mobile";
    private static final String TAG_PHONE_HOME = "home";
    private static final String TAG_PHONE_OFFICE = "office";
	
	
	static ListView listview;
	static String amount;
	static String access_token;
	static String url;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		Intent i=getIntent();
		amount = i.getStringExtra("AMOUNT");
		access_token = i.getStringExtra("ACCESS_TOKEN");
		url = "https://graph.facebook.com/v2.2/me?fields=statuses.limit("+amount+")%7Bmessage%7D&access_token="+access_token;
		
		
		
		listview = (ListView)findViewById(R.id.result_list);
		
		GetPosts info = new GetPosts(resultActivity.this, listview);
		info.execute(url);	
	}
}


class GetPosts extends AsyncTask<String, Void, ArrayList<Post>>{

	Context c;
	ListView listview;
	GetPosts(Context context, ListView Listview){
		c = context;
		listview = Listview;
	}
	@Override
	protected ArrayList<Post> doInBackground(String... params) {
		// TODO Auto-generated method stub
		ArrayList<Post> posts = new ArrayList();
		String url = params[0];
        InputStream inputStream = null;
        String result = "";
        try {
 
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            result = convertInputStreamToString(inputStream);
 
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        
        try {
			JSONObject jObject = new JSONObject(result);
			String temp2 = jObject.getString("statuses");
			JSONObject jObject2 = new JSONObject(temp2);
			JSONArray jArray = jObject2.getJSONArray("data");
			
			for (int i=0; i < jArray.length(); i++)
			{
			    try {
			        JSONObject oneObject = jArray.getJSONObject(i);
			        // Pulling items from the array
			        Post post = new Post(oneObject.getString("id"), oneObject.getString("message"), 
			        		oneObject.getString("updated_time"));
				    posts.add(post);
			    } catch (JSONException e) {
			        // Oops
			    }
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return posts;
	}
	
	protected void onPostExecute(ArrayList<Post> posts) {
		// TODO Auto-generated method stub
		for(int i=0;i<posts.size();i++){
			Log.wtf("~~~~~~~~~~~~~~~", posts.get(i).message);
		}
		BarAdapter adapter = new BarAdapter(c, posts);
		listview.setAdapter(adapter);
	}
	
    private String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }
 
	
}

// bar adapter for list view
@SuppressLint("ViewHolder") class BarAdapter extends BaseAdapter
{
	ArrayList<Post> posts = new ArrayList();
	private Context context;
	
	BarAdapter(Context c, ArrayList<Post> POSTS){
		context = c;
		posts = POSTS;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return posts.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row=inflater.inflate(R.layout.single_row, parent, false);
		TextView id = (TextView)row.findViewById(R.id.id);
		TextView date = (TextView)row.findViewById(R.id.date);
		TextView message = (TextView)row.findViewById(R.id.message);
		
		final Post temp = posts.get(position);
		
		id.setText(temp.id);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
		Date convertedDate = new Date();
		try {
		      convertedDate = dateFormat.parse(temp.date);
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		date.setText(convertedDate.toString());
		message.setText(temp.message);
		
		return row;
	}
	
}
class Post{
	public String id;
	public String message;
	public String date;
	
	public Post(){
		id = null;
		message = null;
		date = null;
	}
	public Post(String ID, String Posts, String Date){
		id = ID;
		message = Posts;
		date = Date;
	}
}