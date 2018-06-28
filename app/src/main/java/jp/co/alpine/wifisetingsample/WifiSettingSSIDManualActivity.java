package jp.co.alpine.wifisetingsample;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import jp.co.alpine.wificonfiglibs.MyWifiManager;

public class WifiSettingSSIDManualActivity extends WifiSettingBase {
	private Spinner mSpinnerAuthentication;
	private EditText mEditTextWifiName;
	private EditText mEditTextWifiPassword;
	@Override
	protected void setupView() {
		setContentView(R.layout.activity_wifi_setting_ssidmanual);

		mSpinnerAuthentication = (Spinner) findViewById(R.id.spinner_authentation_method);
		mEditTextWifiName = (EditText) findViewById(R.id.edit_text_wifi_name);
		mEditTextWifiPassword = (EditText) findViewById(R.id.edit_text_wifi_passowrd);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.authentication_methods_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerAuthentication.setAdapter(adapter);

		mSpinnerAuthentication.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});

		mBtnOK = (Button) findViewById(R.id.btn_OK);
		mBtnOK.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MyWifiManager.WifiData mWifiInfo = new MyWifiManager.WifiData(null);
				mWifiInfo.ssid = mEditTextWifiName.getText().toString();
				mWifiInfo.password = mEditTextWifiPassword.getText().toString();
				mWifiInfo.security = (String) mSpinnerAuthentication.getSelectedItem();
				if (mWifiInfo.security.equals("None"))
					mWifiInfo.security = "Open";

				if (mWifiInfo.ssid.trim().length() == 0) {
					mWifiInfo.ssid = null;
					AlertDialog.Builder alert = new AlertDialog.Builder(WifiSettingSSIDManualActivity.this);
					alert.setMessage("please_input_SSID");
					alert.show();
					return;
				}

				connectWiFi(mWifiInfo);
			}
		});
	}
}
