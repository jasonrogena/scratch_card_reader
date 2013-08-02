package org.cgiar.ilri.lab;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class Preview extends SurfaceView implements SurfaceHolder.Callback 
{
	private static final String TAG = "Preview";

	SurfaceHolder mHolder;
	public Camera camera;
	private int heightOfCamera=0;

	Preview(Context context) 
	{
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) 
	{
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		camera = Camera.open();
		try 
		{
			camera.setPreviewDisplay(holder);
			camera.setPreviewCallback(new PreviewCallback()
			{

				public void onPreviewFrame(byte[] data, Camera arg1)
				{
					/*FileOutputStream outStream = null;
					try 
					{
						outStream = new FileOutputStream(String.format(
								"/sdcard/%d.jpg", System.currentTimeMillis()));
						outStream.write(data);
						outStream.close();
						Log.d(TAG, "onPreviewFrame - wrote bytes: "
								+ data.length);
					} 
					catch (FileNotFoundException e) 
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					} 
					finally
					{
					}*/
					Preview.this.invalidate();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		camera.stopPreview();
		camera = null;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = camera.getParameters();
		List<Camera.Size> previewSizes=parameters.getSupportedPreviewSizes();
		int previewWidth=0;
		int previewHeight=0;
		for (int i = 0; i < previewSizes.size(); i++)
		{	
			if(previewSizes.get(i).width>previewWidth)
			{
				previewHeight=previewSizes.get(i).height;
				previewWidth=previewSizes.get(i).width;
			}
		}
		List<Camera.Size> pictureSizes=parameters.getSupportedPictureSizes();
		int pictureHeight=0;
		int pictureWidth=0;
		for(int i=0; i<pictureSizes.size(); i++)
		{
			if(pictureSizes.get(i).width>pictureWidth)
			{
				pictureWidth=pictureSizes.get(i).width;
				pictureHeight=pictureSizes.get(i).height;
			}
		}
		parameters.setPreviewSize(previewWidth, previewHeight);
		parameters.setPictureSize(pictureWidth, pictureHeight);
		parameters.setPictureFormat(ImageFormat.JPEG);
		parameters.setJpegQuality(100);
		Log.d("CAMERA", "preview size :"+String.valueOf(previewWidth)+" x "+String.valueOf(previewHeight));
		Log.d("CAMERA", "picture size :"+String.valueOf(pictureWidth)+" x "+String.valueOf(pictureHeight));
		heightOfCamera=previewSizes.get(0).width;
		camera.setParameters(parameters);
		camera.setDisplayOrientation(90);
		camera.startPreview();
	}
	
	public int getHeightOfCamera()
	{
		return heightOfCamera;
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		Paint p = new Paint(Color.RED);
		//Log.d(TAG, "draw");
		canvas.drawText("PREVIEW", canvas.getWidth() / 2,
				canvas.getHeight() / 2, p);
	}
}