package com.example.fb_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.AppEventsLogger;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

import android.support.v7.app.ActionBarActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements OnClickListener {
	
	static TextView amount_likes;
	static TextView amount_comments;
	Button login_button, display;
	ImageView personal_pic;
	Facebook fb;
	SharedPreferences sp;
	TextView welcome;
	TextView posts;
	NumberPicker np;
	
	
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // initialize fb object
        final String APP_ID = getString(R.string.APP_ID);
    	fb = new Facebook(APP_ID);        
        
        sp = getPreferences(MODE_PRIVATE);
        String access_token = sp.getString("access_token", null);
        long expires = sp.getLong("access_expires", 0);
        
        if(access_token != null){
        	fb.setAccessToken(access_token);
        }
        if(expires != 0){
        	fb.setAccessExpires(expires);
        }
        
        // initialize variables 
        np = (NumberPicker) findViewById(R.id.number_picker);
        np.setMaxValue(50);
        np.setMinValue(1);
        welcome = (TextView) findViewById(R.id.welcome);
        posts = (TextView) findViewById(R.id.posts);
        login_button = (Button) findViewById(R.id.login);
        display = (Button) findViewById(R.id.view_posts);
        personal_pic = (ImageView) findViewById(R.id.picture);
        login_button.setOnClickListener(this);
        updateButton();
        display.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(getBaseContext(), resultActivity.class);
            	intent.putExtra("AMOUNT", np.getValue()+"");
            	intent.putExtra("ACCESS_TOKEN", fb.getAccessToken());
            	startActivity(intent);
            }
        });
    }
 
	private void updateButton() {
		// TODO Auto-generated method stub
		if(fb.isSessionValid()){
			login_button.setText("Logout");
			personal_pic.setVisibility(ImageView.VISIBLE);
			
			personalInfo info = new personalInfo();
			info.execute(fb);	
			
			
		}else{
			login_button.setText("Login");
			personal_pic.setVisibility(ImageView.INVISIBLE);
		}
		
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(fb.isSessionValid()){
			Thread thread = new Thread(){
	            public void run() {
	                try {
	                    fb.logout(MainActivity.this);
	                } catch (MalformedURLException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                } catch (IOException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                }
	            };
	            };
	            thread.start();
	            try {
					thread.join();
		    		updateButton();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		}else{
			// login to facebook
			fb.authorize(this,new String[] {"user_status"}, new DialogListener() {
				
				@Override
				public void onFacebookError(FacebookError e) {
					// TODO Auto-generated method stub
					Toast.makeText(MainActivity.this, "Facebook error", Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onError(DialogError e) {
					// TODO Auto-generated method stub
					Toast.makeText(MainActivity.this, "onError", Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onComplete(Bundle values) {
					// TODO Auto-generated method stub
					Editor editor = sp.edit();
					editor.putString("access_token", fb.getAccessToken());
					editor.putLong("access_expires", fb.getAccessExpires());
					editor.commit();
					updateButton();
				}
				
				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
					Toast.makeText(MainActivity.this, "User canceled", Toast.LENGTH_SHORT).show();
				}
			});
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		fb.authorizeCallback(requestCode, resultCode, data);
	}
	
	private class personalInfo extends AsyncTask<Facebook, Void, Task>{

		@Override
		protected Task doInBackground(Facebook... params) {
			// TODO Auto-generated method stub
			Facebook fb = params[0];
			JSONObject obj = null;
			URL posts_url = null;
			URL personal_img_url = null;
			String status_url = null;
			
			String jsonUser = null;
			try {
				jsonUser = fb.request("me");
				obj = Util.parseJson(jsonUser);
				String id = obj.optString("id");
				String name = obj.optString("name");
				//Log.wtf("!~!~!~!~!~!~", name);
				//welcome.setText("Welcome, "+name);
				personal_img_url = new URL("https://graph.facebook.com/"+id+"/picture?type=large");
				
				Bitmap bmp = BitmapFactory.decodeStream(personal_img_url.openConnection().getInputStream());
				
				posts_url = new URL("https://graph.facebook.com/"+id+" "+sp.getString("access_token", null));
				status_url = "https://graph.facebook.com/v2.2/me/statuses?limit=5&access_token="+fb.getAccessToken();
				
				String user_status = GET(status_url).toString();

				ArrayList<String> temp = new ArrayList();
				
			
				
				Task task = new Task(name, bmp,user_status);

				return task;
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FacebookError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//personal_pic.setImageBitmap(bmp);
			return null;
		}	
		
		@Override
		protected void onPostExecute(Task result) {
			// TODO Auto-generated method stub
			//Log.wtf("!!!!!~~~~~~",result.pic+"");
			if(result != null){
				personal_pic.setVisibility(ImageView.VISIBLE);
				personal_pic.setImageBitmap(result.pic);
				welcome.setText("Welcome, "+result.name);
				//Log.wtf("++++", result.posts);
				//posts.setText(result.posts);
				display.setVisibility(Button.VISIBLE);
				np.setVisibility(NumberPicker.VISIBLE);
				
			}
		}
		
	    public String GET(String url){
	        InputStream inputStream = null;
	        String result = "";
	        try {
	 
	            // create HttpClient
	            HttpClient httpclient = new DefaultHttpClient();
	 
	            // make GET request to the given URL
	            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
	 
	            // receive response as inputStream
	            inputStream = httpResponse.getEntity().getContent();
	 
	            // convert inputstream to string
	            if(inputStream != null)
	                result = convertInputStreamToString(inputStream);
	            else
	                result = "Did not work!";
	 
	        } catch (Exception e) {
	            Log.d("InputStream", e.getLocalizedMessage());
	        }
	 
	        return result;
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
	 
	    // check network connection
	    public boolean isConnected(){
	        ConnectivityManager connMgr = (ConnectivityManager) getSystemService("connectivity");
	            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	            if (networkInfo != null && networkInfo.isConnected()) 
	                return true;
	            else
	                return false;   
	    }


		
	}
	
	public class Task{
		public String name;
		public Bitmap pic;
		public String posts;
		public String date;
		public int amount_likes;
		public int amount_comments;
		
		public Task(){
			name = null;
			pic = null;
			posts = null;
			date = null;
			amount_likes = 0;
			amount_comments = 0;
		}
		public Task(String Name, Bitmap Pic, String Posts){
			name = Name;
			pic = Pic;
			posts = Posts;
		}
	}
	

}
