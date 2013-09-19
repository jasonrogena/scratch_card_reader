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

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;

public class SampleSender extends AsyncTask<Context, Integer, Boolean>{
	private final static String SERVER_URL = "http://192.168.2.232/~jason/scratchcardreader/get.php";
	@Override
	protected Boolean doInBackground(Context... params) {
		TelephonyManager telephonyManager=((TelephonyManager)params[0].getSystemService(Context.TELEPHONY_SERVICE));
		String operatorName=telephonyManager.getNetworkOperatorName();
		File samplesDir = new File(MainActivity.DATA_PATH + SampleSaver.SAMPLE_DIR+File.separator+operatorName+File.separator);
		if(samplesDir.exists()) {
			File[] allImages = samplesDir.listFiles();
			if(allImages.length >= SampleSaver.MAX_SIZE){
				byte[] buffer = new byte[1024];
				try {
					Date date = new Date();
					
					String zipFileName = MainActivity.DATA_PATH + SampleSaver.SAMPLE_DIR+File.separator+operatorName+File.separator+String.valueOf(date.getTime())+operatorName+".zip";
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
					
					/*fileInputStream.close();
					outputStream.flush();
					outputStream.close();
					File file = new File(zipFileName);
					file.delete();*/
					//TODO: delete the zip file
				} 
				catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}
}
