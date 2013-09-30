package org.cgiar.ilri.lab;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SenderService extends Service {
	public static final String TAG = "SENDER";
	//private final static String SERVER_URL = "http://192.168.2.232/~jason/scratchcardreader/get.php";
	private final static String SERVER_URL = "http://future-system-352.appspot.com/";
	Checker checker;
	long waitTime = 100;
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate called for SenderService, starting the service");
		super.onCreate();
		if(checker == null){
			checker = new Checker();
		}
		monitorFiles();
	}
	
	private void monitorFiles() {
		Log.d(TAG, "Monitoring started");
		if(checker.getStatus() != AsyncTask.Status.RUNNING) {
			Log.d(TAG, "Starting monitoring thread");
			checker.execute(1);
		}
		else {
			Log.d(TAG, "Monitoring thread already started");
		}
	}
	
	private boolean sufficientNoOfFiles(){
		TelephonyManager telephonyManager=((TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE));
		String operatorName=telephonyManager.getNetworkOperatorName();
		File samplesDir = new File(MainActivity.DATA_PATH + SampleSaver.SAMPLE_DIR+File.separator+operatorName+File.separator);
		if(samplesDir.exists()) {
			File[] allImages = samplesDir.listFiles();
			if(allImages.length >= SampleSaver.MAX_SIZE){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy called on SenderService");
		checker.cancel(false);
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		checker.cancel(false);
		this.stopSelf();
	}
	
	private class Checker extends AsyncTask<Integer, Integer, Integer> {
		@Override
		protected Integer doInBackground(Integer... params) {
			ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			Log.d(TAG, "checking..");
			if(sufficientNoOfFiles()) {
				Log.d(TAG, "files sufficient");
				if(mWifi.isConnected()) {
					Log.d(TAG, "On wifi, sending files");
					sendSamples();
				}
				else {
					Log.d(TAG, "not connected to WIFI, not sending files");
				}
			}
			else {
				Log.d(TAG, "files not sufficient");
			}
			return 1;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if(!isCancelled()) {
				Log.d(TAG, "Stopping SenderService");
				stopSelf();
			}
		}

		private void sendSamples() {

			TelephonyManager telephonyManager=((TelephonyManager)SenderService.this.getSystemService(Context.TELEPHONY_SERVICE));
			String operatorName=telephonyManager.getNetworkOperatorName();
			File samplesDir = new File(MainActivity.DATA_PATH + SampleSaver.SAMPLE_DIR+File.separator+operatorName+File.separator);
			if(samplesDir.exists()) {
				File[] allImages = samplesDir.listFiles();
				if(allImages.length >= SampleSaver.MAX_SIZE){
					byte[] buffer = new byte[1024];
					String zipFileName = null;
					try {
						Log.d(TAG, "SENDING ZIP FILE TO SERVER");
						Date date = new Date();
						
						zipFileName = MainActivity.DATA_PATH + SampleSaver.SAMPLE_DIR+File.separator+operatorName+File.separator+String.valueOf(date.getTime())+operatorName+".zip";
						FileOutputStream fos = new FileOutputStream(zipFileName);
						ZipOutputStream zos = new ZipOutputStream(fos);
						for(int i = 0; i < allImages.length; i++) {
							FileInputStream fis = new FileInputStream(allImages[i]);
							zos.putNextEntry(new ZipEntry(allImages[i].getName()));
							int length;
							while((length = fis.read(buffer))>0) {
								zos.write(buffer, 0 ,length);
							}
							zos.closeEntry();
							fis.close();
						}
						zos.close();
						
						HttpURLConnection connection = null;
						DataOutputStream outputStream = null;
						DataInputStream inputStream = null;
						String lineEnd = "\r\n";
						String twoHyphens = "--";
						String boundary =  "*****";
						
						int bytesRead, bytesAvailable, bufferSize;
						int maxBufferSize = 1*1024*1024;

						FileInputStream fileInputStream = new FileInputStream(new File(zipFileName));
						
						URL url = new URL(SERVER_URL);
						connection = (HttpURLConnection) url.openConnection();
						connection.setDoInput(true);
						connection.setDoOutput(true);
						connection.setUseCaches(false);
						
						connection.setRequestMethod("POST");
						connection.setRequestProperty("Connection", "Keep-Alive");
						connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
						
						outputStream = new DataOutputStream( connection.getOutputStream() );
						outputStream.writeBytes(twoHyphens + boundary + lineEnd);
						outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + zipFileName +"\"" + lineEnd);
						outputStream.writeBytes(lineEnd);
						
						bytesAvailable = fileInputStream.available();
						bufferSize = Math.min(bytesAvailable, maxBufferSize);
						buffer = new byte[bufferSize];
						
						bytesRead = fileInputStream.read(buffer, 0, bufferSize);
						
						while (bytesRead > 0) {
							outputStream.write(buffer, 0, bufferSize);
							bytesAvailable = fileInputStream.available();
							bufferSize = Math.min(bytesAvailable, maxBufferSize);
							bytesRead = fileInputStream.read(buffer, 0, bufferSize);
						}
						
						outputStream.writeBytes(lineEnd);
						outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
						Log.d(TAG, "ZIP FILE SENT");
						
						Log.d(TAG, "Server Response code : "+String.valueOf(connection.getResponseCode()));
						Log.d(TAG, "Response String : "+connection.getResponseMessage());
						fileInputStream.close();
						outputStream.flush();
						outputStream.close();
						
						Log.d(TAG, "deleting all files");
						File file = new File(zipFileName);
						file.delete();
						for (int i = 0; i < allImages.length; i++) {
							allImages[i].delete();
						}
					} 
					catch (Exception e) {
						// TODO Auto-generated catch block
						if(zipFileName!=null) {
							Log.d(TAG, "something went wrong, deleting zip file");
							File file = new File(zipFileName);
							file.delete();
						}
						Log.e(TAG, e.getMessage());
						e.printStackTrace();
					}
				}
			}
		
		}
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
