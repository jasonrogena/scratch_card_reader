package org.cgiar.ilri.lab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener
{
	public static final String DATA_PATH ="/sdcard/";// Environment.getExternalStorageDirectory().toString();
	
	private Preview preview;
	private Camera camera;
	private ShutterCallback shutterCallback;
	private PictureCallback rawCallback;
	private PictureCallback jpegCallback;
	private FrameLayout previewFrameLayout;
	private View extraSpace;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        
        preview=new Preview(this);
        previewFrameLayout=(FrameLayout)this.findViewById(R.id.preview);
        previewFrameLayout.addView(preview);
        previewFrameLayout.setOnClickListener(this);
        shutterCallback=new ShutterCallback()
        {
			
			@Override
			public void onShutter() {
				Log.d("CAMERA", "shutter called");
				
			}
		};
		rawCallback=new PictureCallback()
		{
			
			@Override
			public void onPictureTaken(byte[] data, Camera camera) 
			{
				Log.d("CAMERA", "Picture taken");	
			}
		};
		jpegCallback=new PictureCallback()
		{
			
			@Override
			public void onPictureTaken(byte[] data, Camera camera)
			{
				FileOutputStream outStream = null;
				try
				{
					OCRHandler handler=new OCRHandler();
					handler.execute(data);
					preview.camera.startPreview();
					
				} catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
				}
				Log.d("CAMERA", "onPictureTaken - jpeg");
				
			}
		};
    }
    


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



	@Override
	public void onClick(View v) 
	{
		if(v==previewFrameLayout)
		{
			AutoFocusCallback autoFocusCallback=new AutoFocusCallback() 
			{
				
				@Override
				public void onAutoFocus(boolean success, Camera camera)
				{
					if(success)
					{
						preview.camera.takePicture(shutterCallback,null,jpegCallback);
					}
				}
			};
			preview.camera.autoFocus(autoFocusCallback);
		}
	}
	
	private class OCRHandler extends AsyncTask<byte[], Integer, String>
	{

		@Override
		protected String doInBackground(byte[]... data)
		{
			try
			{
				FileOutputStream outStream = new FileOutputStream(String.format(DATA_PATH+"%d.jpg", System.currentTimeMillis()));
				Log.d("CAMERA", "byte size = "+String.valueOf(data[0].length));
				Bitmap bitmap=BitmapFactory.decodeByteArray(data[0], 0, data[0].length);
				Matrix matrix=new Matrix();
				matrix.postRotate(90);
				Bitmap rotatedImage=Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				int halfHeight=(int)rotatedImage.getHeight()/2;
				int croppedHeight=(int)(rotatedImage.getHeight()*0.15);
				int y=halfHeight-(int)(croppedHeight/2);
				Bitmap croppedImage=Bitmap.createBitmap(rotatedImage, 0, y, rotatedImage.getWidth(), croppedHeight);
				croppedImage.compress(CompressFormat.JPEG, 100, outStream);
				outStream.close();
				Log.d("CAMERA", "cropped image width = "+String.valueOf(croppedImage.getWidth()));
				if (!(new File(DATA_PATH + "tessdata/eng.traineddata")).exists()) 
				{
					try 
					{
						Log.d("CAMERA", "copying eng to file system");
						AssetManager assetManager = getAssets();
						InputStream in = assetManager.open("tessdata/eng.traineddata");
						//GZIPInputStream gin = new GZIPInputStream(in);
						OutputStream out = new FileOutputStream(DATA_PATH + "tessdata/eng.traineddata");
						// Transfer bytes from in to out
						byte[] buf = new byte[1024];
						int len;
						//while ((lenf = gin.read(buff)) > 0) {
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						in.close();
						//gin.close();
						out.close();

						Log.d("CAMERA", "Finished copying eng");
					} 
					catch (IOException e) 
					{
						Log.e("CAMERA", "Was unable to copy eng traineddata " + e.toString());
					}
				}
				else
				{
					Log.d("CAMERA", "eng already on sd");
				}
				Log.d("CAMERA", "Calling TessBaseAPI");
				TessBaseAPI baseAPI=new TessBaseAPI();
				baseAPI.setDebug(true);
				Log.d("CAMERA", "1");
				baseAPI.init(DATA_PATH, "eng");
				Log.d("CAMERA", "2");
				baseAPI.setImage(croppedImage);
				Log.d("CAMERA", "3");
				String result=baseAPI.getUTF8Text();
				Log.d("CAMERA", "4");
				baseAPI.end();
				Log.d("CAMERA", "TessBaseAPI finished analyzing");
				return result;
				
			} catch (Exception e) 
			{
				e.printStackTrace();
				System.err.print(e.getMessage());
			}
			finally
			{
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			if(result!=null)
			{
				Log.d("CAMERA", result);
				Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
			}
			else
			{
				Log.d("CAMERA", "no text found");
				Toast.makeText(MainActivity.this, "no text found", Toast.LENGTH_LONG).show();
			}
		}
	}
    
}
