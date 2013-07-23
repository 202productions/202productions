package com.example.streetpass202;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class TitlePage extends Activity {

	private ParseUser currentUser;

	// The popups themselves
	private PopupWindow login_ppw;
	private PopupWindow password_ppw;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    Parse.initialize(this, "2ImxfeRw3Qt7eVMrulMweRWwkbdDnvyIYd8ZXbqZ", "veHt5pc6xPyq4imhDkyv6vMrxPxdhQ9sACBEnqUo"); 
		setContentView(R.layout.activity_title_page);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_title_page, menu);
		return true;
	}

	public void loginStart(View view) {
		buildPopup(false);
	}
	
	/**
	 * Handles what happens when user clicks the "Forgot your password" link.
	 * @param view Button that is pressed
	 */
	public final void forgotPassword(final View view) {
		// set up the layout inflater to inflate the popup layout
		LayoutInflater layoutInflater =
		(LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

		// the parent layout to put the layout in
		ViewGroup parentLayout = (ViewGroup) findViewById(R.id.title_page_layout);

		// inflate the password layout
		// Build the reset password popup
		View popupView = layoutInflater.inflate(R.layout.reset_pw_layout, null);
		password_ppw = new PopupWindow(popupView,
		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		password_ppw.showAtLocation(parentLayout, Gravity.TOP, 10, 50);
		// dismiss the login popup
		login_ppw.dismiss();
	}
	/**
	 * Build the login popup.
	 * @param dismisspsw Whether or not the reset popup is currently displayed.
	 */
	private void buildPopup(final boolean dismisspsw) {
		// set up the layout inflater to inflate the popup layout
		LayoutInflater layoutInflater =
		(LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

		// the parent layout to put the layout in
		ViewGroup parentLayout = (ViewGroup) findViewById(R.id.title_page_layout);

		if (dismisspsw) {
			password_ppw.dismiss();
		}
		// Build the login poup
		View popupView = layoutInflater.inflate(R.layout.login_popup, null);
		login_ppw = new PopupWindow(popupView,
		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		login_ppw.showAtLocation(parentLayout, Gravity.TOP, 10, 50);
		// inflate either the login layout
	}
	
	/**
	 * Handles what happens when user clicks the login button.
	 * @param view Button that is pressed
	 */
	public final void loginButton(final View view) {
		// Try to login
		String usernameString;
		try {
			usernameString = loginUser();
		} catch (ConnectionException e) {
			e.fillInStackTrace();
//			Intent intent = new Intent(this, ErrorScreen.class);
//			intent.putExtra("error", R.layout.activity_connection_error);
//			startActivity(intent);
			return;
		}
		// If login was successful, go to the multiplayer game
		if (!usernameString.equals("")) {
			Intent lobbyIntent = new Intent(this, MainLobby.class);
			startActivity(lobbyIntent);
		}
	}
	
	/**
	 * Handles what happens when user clicks the login button.
	 * @return the username to pass to the multiplayer activity.
	 * If this is "", the activity should NOT continue to the multiplayer screen.
	 * @throws InternetConnectionException 
	 */
	public final String loginUser() throws ConnectionException {
		// Get the username and password inputs
		final View contentView = login_ppw.getContentView();
		EditText usernameInput = (EditText) contentView.findViewById(R.id.username_login_input);
		EditText passwordInput = (EditText) contentView.findViewById(R.id.password_login_input);
		String usernameString = usernameInput.getText().toString().trim();
		final String passwordString = passwordInput.getText().toString();

		// Where to display an error message
		final TextView errorMessage = (TextView) contentView.findViewById(R.id.login_error_message);

		// Try to login with the given inputs
		ParseUser user;
		try {
			user = ParseUser.logIn(usernameString, passwordString);
		} catch (ParseException e) {
			e.fillInStackTrace();
			boolean errorOccured = false;
			List<ParseObject> usernameResults = new ArrayList<ParseObject>();
			List<ParseObject> passwordResults = new ArrayList<ParseObject>();
			ParseQuery query = ParseUser.getQuery();
			// try to find the username that the user typed in
			query.whereEqualTo("username", usernameString);
			try {
				query.count();
				usernameResults = query.find();
			} catch (ParseException e1) {
				// error occured trying to find the username
				errorOccured = true;
				e1.printStackTrace();
			} catch (NullPointerException e1) {
				errorOccured = true;
				e1.printStackTrace();
			}

			// try to find the password that the user typed in
			// associated with that username
			query.whereEqualTo("username", usernameString);
			query.whereEqualTo("password", passwordString);
			try {
				query.count();
				passwordResults = query.find();
			} catch (ParseException e1) {
				// error occured trying to find the password
				errorOccured = true;
				e1.printStackTrace();
			} catch (NullPointerException e1) {
				errorOccured = true;
				e1.printStackTrace();
			}

			// figure out the error
			if (errorOccured) {
				errorMessage.setText(R.string.error_login_unexp);
				return "";
			}
			if ((usernameResults.size() == 0) && (passwordResults.size() == 0)) {
				errorMessage.setText(R.string.error_login_both);
			} else if ((usernameResults.size() == 0) && (passwordResults.size() != 0)) {
				errorMessage.setText(R.string.error_login_uname);
			} else if ((usernameResults.size() != 0) && (passwordResults.size() == 0)) {
				errorMessage.setText(R.string.error_login_pswd);
			} else {
				// unexpected error occured

			}
			// signals an error occured
			return "";
		}

		// Check for verified email
		boolean emailVerified = user.getBoolean("emailVerified");
		if (!emailVerified) {
			errorMessage.setText(R.string.error_login_verif);
			ParseUser.logOut();
			currentUser = ParseUser.getCurrentUser();
			usernameString = "";
		} else {
			currentUser = user;
		}

		return usernameString;
	}
	
	/**
	 * Exits the login popup window.
	 * @param view the button clicked
	 */
	public final void exitLoginPopup(final View view) {
		login_ppw.dismiss();
	}

	/**
	 * Exits the password popup window.
	 * @param view the button clicked
	 */
	public final void exitPasswordPopup(final View view) {
		buildPopup(true);
	}
	
	/**
	 * Goes to the Registration page.
	 * @param view the button clicked
	 */
	public final void goToRegister(final View view) {
		Intent registerIntent = new Intent(this, RegisterPage.class);
		startActivity(registerIntent);
	}

}


