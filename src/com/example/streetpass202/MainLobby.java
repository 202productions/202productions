package com.example.streetpass202;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.parse.ParseUser;

public class MainLobby extends Activity {

	ParseUser currentUser;
	GPSTracker gpstracker;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_lobby);
		displayUsername();
		
		// get the gps coordinates
		gpstracker = new GPSTracker(this);
		displayCurrentPosition();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main_lobby, menu);
		return true;
	}
	
	public void displayUsername() {
		// there should already be a user logged in if it got to this point
		currentUser = ParseUser.getCurrentUser();
		String currentUserString = currentUser.getString("username");
		TextView usernameText = (TextView) findViewById(R.id.username_text);
		// display the current user's login name
		usernameText.setText(currentUserString);
	}

	public void displayCurrentPosition() {
		if (gpstracker.canGetLocation) {
			double latitude = gpstracker.getLatitude();
			double longitude = gpstracker.getLongitude();
		}
	}
}
