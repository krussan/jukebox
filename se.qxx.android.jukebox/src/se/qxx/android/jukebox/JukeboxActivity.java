package se.qxx.android.jukebox;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import se.qxx.android.tools.Logger;
import se.qxx.android.jukebox.MovieProto.JukeboxRequestType;


public class JukeboxActivity extends Activity {
	private EditText text;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        text = (EditText)findViewById(R.id.editText1);
        
    }


    public void onConnectClickHandler(View view) throws InterruptedException {
    	Logger.Log().i("onConnectClicked");
    	
       	ProgressDialog d = ProgressDialog.show(this, "Jukebox", "Getting list of movies");

       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(new ProgressDialogHandler(this, d), JukeboxRequestType.ListMovies);
       	Thread t = new Thread(h);
       	t.start();
   		
    }
    
    // This method is called at button click because we assigned the name to the
 	// "On Click property" of the button
 	public void myClickHandler(View view) {
 		switch (view.getId()) {
 		case R.id.button1:
 			RadioButton celsiusButton = (RadioButton) findViewById(R.id.radio0);
 			RadioButton fahrenheitButton = (RadioButton) findViewById(R.id.radio1);
 			if (text.getText().length() == 0) {
 				Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_LONG).show();
 				return;
 			}
 			
 			float inputValue = Float.parseFloat(text.getText().toString());
 			if (celsiusButton.isChecked()) {
 				text.setText(String.valueOf(convertFahrenheitToCelsius(inputValue)));
 				celsiusButton.setChecked(false);
 				fahrenheitButton.setChecked(true);
 			}
 			else {
 				text.setText(String.valueOf(convertCelsiusToFahrenheit(inputValue)));
 				celsiusButton.setChecked(true);
 				fahrenheitButton.setChecked(false);
 			}
 		}
 	}
 	
 	public void fahrenheitClickHandler(View view) {
 		Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_LONG).show();
 		
 	}
 	
	private float convertFahrenheitToCelsius(float fahrenheit) {
		return ((fahrenheit - 32) * 5 / 9);
	}

	// Converts to fahrenheit
	private float convertCelsiusToFahrenheit(float celsius) {
		return ((celsius * 9) / 5) + 32;
	}

}