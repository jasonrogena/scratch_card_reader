package org.cgiar.ilri.lab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.util.Log;

public class SampleSaver extends AsyncTask<Bitmap, Integer, Boolean> {
	public static final int MAX_SIZE = 1;
	public static final String SAMPLE_DIR = "samples";
	private String operator;
	private int noOfCharacters;
	public SampleSaver(String operator, int noOfCharacters) {
		this.operator=operator;
		this.noOfCharacters=noOfCharacters;
	}
	@Override
	protected Boolean doInBackground(Bitmap... params) {
		if(noOfCharacters>2 && noOfCharacters <35) {
			// check if dir exists
			File samplesDir = new File(MainActivity.DATA_PATH + SAMPLE_DIR+File.separator+operator+File.separator);
			if (!samplesDir.exists()) {
				Log.d("CAMERA", "Creating samples dir");
				samplesDir.mkdirs();
			}
			//get all the images in the dir
			File[] allImages = samplesDir.listFiles();
			if(allImages.length >= MAX_SIZE) {
				String oldestName = "";
				int oldestFileIndex = -1;
				for(int i = 0; i <allImages.length; i++) {
					if(i == 0){
						oldestName = allImages[i].getName();
						oldestFileIndex = 0;
					}
					else {
						if(oldestName.compareTo(allImages[i].getName()) > 0) {
							oldestName = allImages[i].getName();
							oldestFileIndex = i;
						}
					}
				}
				if(oldestFileIndex!=-1) {
					allImages[oldestFileIndex].delete();
				}
			}
			//save the bitmap
			Date date=new Date();
			String timestamp=String.valueOf(date.getTime());
			try {
				FileOutputStream out=new FileOutputStream(MainActivity.DATA_PATH + SAMPLE_DIR+File.separator+operator+File.separator+timestamp+".png");
				params[0].compress(CompressFormat.PNG, 100, out);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			Log.d("CAMERA", "Data in image appears to be too distorted, not saving");
		}
		return null;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}

}
