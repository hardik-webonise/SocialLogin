package com.example.sociallogin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import br.com.condesales.EasyFoursquareAsync;
import br.com.condesales.listeners.AccessTokenRequestListener;
import br.com.condesales.listeners.ImageRequestListener;
import br.com.condesales.listeners.UserInfoRequestListener;
import br.com.condesales.models.User;
import br.com.condesales.tasks.users.UserImageRequest;

import com.atawdisk.instagram.OAuthActivity;
import com.example.sociallogin.beans.LoggedInUser;
import com.example.sociallogin.twitter.TwitterApp;
import com.example.sociallogin.twitter.TwitterApp.TwDialogListener;
import com.example.sociallogin.utils.Keys;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;

public class MainActivity extends Activity implements
		AccessTokenRequestListener, ImageRequestListener {
	private Button fb, twitter, gmail, foursquare, instgram;
	private TextView firstName, lastName, email, gender, hastagInfo;

	private LoggedInUser user;
	private Context context;
	/***************** TWITTER ************************/

	private TwitterApp mTwitter;

	/***************** FOUR SQUARE ************************/

	private EasyFoursquareAsync async;

	/***************** INSTAGRAM ************************/
	private final int INSTA_LOGIN = 10;
	private final static String clientId = "d7b048aa9aa74f0eaf0d1986ab750ac2"; // your
	// clientId
	// get
	// from
	// instagram
	private final static String clientSecret = "ab5b865d26064462a6531312bb52e90b"; // your
	// clientSecret
	// get
	// from
	// instagram
	private final static String redirectUri = "oauth://mydomain";
	private String accessToken;

	/***************** FACEBOOK ************************/
	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	// http://stackoverflow.com/questions/17114210/how-can-we-track-hashtags-with-the-new-facebook-hashtag-implementation
	// https://developers.facebook.com/docs/reference/api/search/
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		fb = (Button) findViewById(R.id.fb);
		twitter = (Button) findViewById(R.id.twitter);
		gmail = (Button) findViewById(R.id.gmail);
		foursquare = (Button) findViewById(R.id.foursquare);
		instgram = (Button) findViewById(R.id.instgram);

		firstName = (TextView) findViewById(R.id.firstName);
		lastName = (TextView) findViewById(R.id.lastName);
		email = (TextView) findViewById(R.id.email);
		gender = (TextView) findViewById(R.id.gender);
		hastagInfo = (TextView) findViewById(R.id.hastagInfo);
		hastagInfo.setMovementMethod(new ScrollingMovementMethod());
		mTwitter = new TwitterApp(this, Keys.TWIT_CONS_KEY,
				Keys.TWIT_CONS_SEC_KEY);
		mTwitter.setListener(mTwLoginDialogListener);

		twitter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mTwitter.hasAccessToken()) {
					String username = mTwitter.getUsername();
					LoggedInUser user = new LoggedInUser();
					user.setFirstName(username);
					setValues(user);
					new GetHashTagFromTwitter().execute();
				} else {
					mTwitter.authorize();
				}
			}
		});

		gmail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new GetHashTagFromGoogle().execute();
			}
		});

		foursquare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				async = new EasyFoursquareAsync(MainActivity.this);
				async.requestAccess(MainActivity.this);
			}
		});
		instgram.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,
						OAuthActivity.class);
				intent.putExtra("clientId", clientId);
				intent.putExtra("clientSecret", clientSecret);
				intent.putExtra("redirectUri", redirectUri);
				startActivityForResult(intent, INSTA_LOGIN);
			}
		});

		fb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

				Session session = Session.getActiveSession();
				if (session == null) {
					if (savedInstanceState != null) {
						session = Session.restoreSession(MainActivity.this,
								null, statusCallback, savedInstanceState);
					}
					if (session == null) {
						session = new Session(MainActivity.this);
					}
					Session.setActiveSession(session);
					if (session.getState().equals(
							SessionState.CREATED_TOKEN_LOADED)) {
						session.openForRead(new Session.OpenRequest(
								MainActivity.this).setCallback(statusCallback));
					}
				}

				updateView();
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == INSTA_LOGIN && resultCode == RESULT_OK) {
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(this);
			accessToken = pref.getString(OAuthActivity.TOKEN, null);
			if (accessToken == null) {

				new GetTagData().execute();
			}
		} else {
			Session.getActiveSession().onActivityResult(this, requestCode,
					resultCode, data);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	public void setValues(LoggedInUser user) {
		this.user = user;
		firstName.setText(user.getFirstName());
		lastName.setText(user.getLastName());
		email.setText(user.getEmail());
		gender.setText(user.getGender());
	}

	public String convertStreamToString(InputStream inputStream)
			throws IOException {
		if (inputStream != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(
						inputStream, "UTF-8"), 1024);
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				inputStream.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	/******************************** TWITTER ****************************************/

	private final TwDialogListener mTwLoginDialogListener = new TwDialogListener() {
		@Override
		public void onComplete(String value) {
			String username = mTwitter.getUsername();
			LoggedInUser user = new LoggedInUser();
			user.setFirstName(username);
			setValues(user);
			new GetHashTagFromTwitter().execute();
		}

		@Override
		public void onError(String value) {

			Toast.makeText(context, "Twitter connection failed",
					Toast.LENGTH_LONG).show();
		}
	};

	class GetHashTagFromTwitter extends AsyncTask<Void, Void, Void> {
		StringBuilder str = new StringBuilder();

		@Override
		protected Void doInBackground(Void... params) {
			// https://twitter.com/search?q=%40twitterapi
			// https://api.twitter.com/1.1/search/tweets.json?q=%40twitterapi
			try {
				Twitter twitter = new TwitterFactory().getInstance();

				AccessToken accessToken = new AccessToken(
						mTwitter.getAccessToken(),
						mTwitter.getAccessTokenSecret());
				twitter.setOAuthConsumer(Keys.TWIT_CONS_KEY,
						Keys.TWIT_CONS_SEC_KEY);
				twitter.setOAuthAccessToken(accessToken);

				Query query = new Query("#GOOGLE");
				QueryResult result;
				result = twitter.search(query);
				List<twitter4j.Status> tweets = result.getTweets();
				for (twitter4j.Status tweet : tweets) {
					str.append("@" + tweet.getUser().getScreenName() + " - "
							+ tweet.getText() + "\n");
					System.out.println("@" + tweet.getUser().getScreenName()
							+ " - " + tweet.getText());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			hastagInfo.setText(str.toString());
		}

		private void dump() {
			try {
				Log.v("", "Do In background");

				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(
						"https://api.twitter.com/1.1/search/tweets.json?q=%40twitterapi");

				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair("oauth_consumer_key",
						URLEncoder.encode(Keys.TWIT_CONS_KEY, "UTF-8")));
				nameValuePairs.add(new BasicNameValuePair("oauth_nonce", ""));
				nameValuePairs
						.add(new BasicNameValuePair("oauth_signature", ""));
				nameValuePairs.add(new BasicNameValuePair(
						"oauth_signature_method", URLEncoder.encode(
								"HMAC-SHA1", "UTF-8")));
				nameValuePairs.add(new BasicNameValuePair("oauth_timestamp",
						URLEncoder.encode("" + System.currentTimeMillis(),
								"UTF-8")));
				nameValuePairs.add(new BasicNameValuePair("oauth_token",
						URLEncoder.encode(mTwitter.getAccessToken(), "UTF-8")));
				nameValuePairs.add(new BasicNameValuePair("oauth_version",
						URLEncoder.encode("1.0", "UTF-8")));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = null;
				/*
				 * HttpClient client = new DefaultHttpClient(); HttpGet request
				 * = new HttpGet(); request.setURI(new URI(
				 * "https://api.twitter.com/1.1/search/tweets.json?q=%40twitterapi"
				 * ));
				 */
				response = httpclient.execute(httppost);

				InputStream is = response.getEntity().getContent();

				String strResponse = convertStreamToString(is);
				Log.v("", "$$$$ " + strResponse);
				/**/

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/******************************** GoOGLE ****************************************/

	class GetHashTagFromGoogle extends AsyncTask<Void, Void, Void> {
		String strHashTags;

		@Override
		protected Void doInBackground(Void... params) {
			// https://twitter.com/search?q=%40twitterapi
			// https://api.twitter.com/1.1/search/tweets.json?q=%40twitterapi
			try {

				HttpClient client = new DefaultHttpClient();
				String getURL = "https://www.googleapis.com/plus/v1/activities?query=%23awesome&key=AIzaSyBTzbQc_wuwesEjdcDn-ZpHku2-fOBgIcg";
				HttpGet get = new HttpGet(getURL);
				HttpResponse responseGet = client.execute(get);
				HttpEntity resEntityGet = responseGet.getEntity();
				if (resEntityGet != null) {
					// do something with the response
					String response = EntityUtils.toString(resEntityGet);
					Log.i("GET RESPONSE", response);
					strHashTags = response;
				}

				// Add your data

				/*
				 * HttpResponse response = null;
				 * 
				 * HttpClient client = new DefaultHttpClient(); HttpGet request
				 * = new HttpGet(); request.setURI(new URI(
				 * "https://api.twitter.com/1.1/search/tweets.json?q=%40twitterapi"
				 * ));
				 * 
				 * response = httpclient.execute(httppost);
				 * 
				 * InputStream is = response.getEntity().getContent();
				 * 
				 * String strResponse = convertStreamToString(is);
				 * 
				 * Log.v("", "$$$$$$ " + strResponse);
				 */
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			hastagInfo.setText(strHashTags);
		}

	}

	/******************************** FOURSQUARE ****************************************/

	@Override
	public void onAccessGrant(String accessToken) {
		// TODO with the access token you can perform any request to foursquare.
		// example:
		async.getUserInfo(new UserInfoRequestListener() {

			@Override
			public void onError(String errorMsg) {
				// TODO Some error getting user info
				Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG)
						.show();
			}

			@Override
			public void onUserInfoFetched(User user) {
				// TODO OWww. did i already got user!?
				LoggedInUser logUser = new LoggedInUser();
				if (user.getBitmapPhoto() == null) {
					UserImageRequest request = new UserImageRequest(
							MainActivity.this, MainActivity.this);
					// request.execute(user.getPhoto());
				} else {
					// userImage.setImageBitmap(user.getBitmapPhoto());
				}
				logUser.setFirstName(user.getFirstName());
				logUser.setLastName(user.getLastName());
				logUser.setGender(user.getGender());
				hastagInfo.setText("FourSquare doesnt support HashTags");
				setValues(logUser);
				Toast.makeText(MainActivity.this, "Got it!", Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	@Override
	public void onImageFetched(Bitmap bmp) {
		// userImage.setImageBitmap(bmp);
	}

	@Override
	public void onError(String errorMsg) {
		// TODO Auto-generated method stub

	}

	/******************************** INSTAGRAM ****************************************/
	class GetTagData extends AsyncTask<Void, Void, Void> {
		StringBuilder str = new StringBuilder();

		@Override
		protected Void doInBackground(Void... arg0) {
			URL example;
			String testTag = "qtp2384";
			try {
				example = new URL("https://api.instagram.com/v1/tags/"
						+ testTag + "/media/recent?access_token=" + accessToken);

				URLConnection tc = example.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						tc.getInputStream()));

				String line;

				while ((line = in.readLine()) != null) {
					str.append(line + "\n");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			hastagInfo.setText(str.toString());
		}

	}

	/******************************** FACEBOOK ****************************************/
	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			updateView();
		}
	}

	private void updateView() {
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			accessToken = session.getAccessToken();
			fb.setText(R.string.logout);
			fb.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogout();
				}
			});
			new GetHashTagData().execute();
		} else {
			fb.setText(R.string.login);
			fb.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogin();
				}
			});
		}
	}

	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this)
					.setCallback(statusCallback));
		} else {
			Session.openActiveSession(this, true, statusCallback);
		}
	}

	private void onClickLogout() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}
	}

	class GetHashTagData extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Log.v("", "***************************************************");
				URL url = new URL(
						"https://graph.facebook.com/search?q=POC&type=post&access_token="
								+ accessToken);
				InputStream is = url.openStream();
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));

				String tempStr = null;
				while ((tempStr = br.readLine()) != null) {
					Log.v("", "--> " + tempStr);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
}
