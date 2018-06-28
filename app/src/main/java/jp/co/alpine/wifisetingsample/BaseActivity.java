
package jp.co.alpine.wifisetingsample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;


public abstract class BaseActivity extends Activity {

	protected String TAG;

	private ProgressDialog mProgressDialog;

	/**
	 * Handler wrapper
	 */
	public static class HandlerEx extends Handler {
		private final WeakReference<BaseActivity> mHostActivity;

		public HandlerEx(BaseActivity host) {
			mHostActivity = new WeakReference<>(host);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mHostActivity == null)
				return;

			BaseActivity host = mHostActivity.get();
			if (host == null) {
				Log.i(HandlerEx.class.getSimpleName(), "Host activity is null!");
				return;
			}

			host.handleMessage(msg);
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TAG = this.getClass().getSimpleName();

		try {
			getActionBar().hide();
		}
		catch (Exception ex) {
//			ex.printStackTrace();
		}

		setupView();
	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.i(TAG, "onStart");
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		Log.i(TAG, "onRestart");
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.i(TAG, "onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();

		Log.i(TAG, "onPause");
	}


	protected abstract void setupView();

	/**
	 * The based wrapper message handler
	 *
	 * @param msg
	 */
	protected void handleMessage(Message msg) {}
	protected void showProgressDialog(Context context, String message) {

		mProgressDialog = ProgressDialog.show(context, null, message, true);
		mProgressDialog.setCancelable(false);
	}

	protected void hiddenProgressDialog() {
		if (null != mProgressDialog) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	//show alert
	protected void showAlertDialog(Context context, String mess){
		if(context== null){
			return;
		}
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setMessage(mess);
		dialog.setNegativeButton("OK", null);
		dialog.show();
	}
}
