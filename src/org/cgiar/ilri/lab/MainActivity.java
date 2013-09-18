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
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener
{
	public static final String DATA_PATH = Environment.getExternalStorageDirectory()+File.separator;
	
	private Preview preview=null;
	private Camera camera;
	private FrameLayout previewFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
    }
    
    @Override
	protected void onResume() 
    {
		super.onResume();
		if(preview==null)
		{
			RelativeLayout mainLayout=(RelativeLayout)this.findViewById(R.id.main_layout);
			View upperLimit=(View)this.findViewById(R.id.upper_limit);
			View lowerLimit=(View)this.findViewById(R.id.lower_limit);
			ImageView lastImageIV = (ImageView)this.findViewById(R.id.last_image);
			previewFrameLayout=(FrameLayout)this.findViewById(R.id.preview);
			preview=new Preview(this,mainLayout,previewFrameLayout,upperLimit,lowerLimit,lastImageIV);
	        previewFrameLayout.addView(preview);
	        previewFrameLayout.setOnClickListener(this);
		}
		/*int previewHeight=preview.getHeightOfCamera();
		if(previewHeight!=-1)
		{
			Log.d("CAMERA", "Fetched preview height "+String.valueOf(previewHeight));
			ViewGroup.LayoutParams upperLimitLP=upperLimit.getLayoutParams();
			ViewGroup.LayoutParams lowerLimitLP=lowerLimit.getLayoutParams();
			int extraSpace=(int)(previewHeight*(1-activeImageHeight));
			upperLimitLP.height=(int)(extraSpace/2);
			lowerLimitLP.height=(int)(extraSpace/2);
			upperLimit.setLayoutParams(upperLimitLP);
			lowerLimit.setLayoutParams(lowerLimitLP);
		}
		else
		{
			Log.d("CAMERA", "Preview height not initialized");
		}*/
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
			preview.autoFocus();
		}
	}
	
}
