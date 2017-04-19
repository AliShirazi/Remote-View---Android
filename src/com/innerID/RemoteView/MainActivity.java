package com.innerID.RemoteView;


/**
 * @filename: innerID RemoteDemo
 * @author: Ali Shirazi
 * @date: 04-04-14
 * @copyright IDair, llc 2014
 *
 * REQUIRES OPENCV LIBRARY IMPORTED AS ANDROID LIBRARY PROJECT
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.CompressionTools;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import com.idair.core.LicenseNotValid;
import com.idair.core.innerID;
import com.idair.message.FingerprintMessage;
import com.innerID.RemoteDemo.R;
import com.innerID.RemoteView.AboutDialog;
import com.squareup.wire.ByteString;
import com.squareup.wire.Wire;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


//REQUIRES OPENCV LIBRARY IMPORTED AS ANDROID LIBRARY PROJECT
public class MainActivity extends Activity 
{
	//Instance variables.
	static
	{
		System.loadLibrary("opencv_java");
	}
	
	Bitmap input;	//Will hold the converted image after the innerID Camera Capture
	Bitmap returned;
	ImageView displayCapture; //ImageView that will show the processed image(input)
	ImageButton beginCapture; //ImageButton that will launch the innerID Camera Capture
	TextView displayQualityScore;
	FingerprintMessage downloadFp;
	String result = "No Response!";
	StringBuilder strBuilder = new StringBuilder();
	
	/*Boolean variable used to re-display the converted image
	if a capture has taken place since innerID Camera Capture initially launched.
    This also ensures that the converted image will show even
    after a configuration change(screen-rotate, etc)*/
	
	static boolean newImageCapture = false;  
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		//Set the displayCapture ImageView to the ImageView defined in the layout.
		displayCapture =(ImageView) findViewById(R.id.displayCapture);
		beginCapture = (ImageButton) findViewById(R.id.beginCapture);

		displayQualityScore = (TextView) findViewById(R.id.qualityScoreDisplay);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	
	
/*	An onClickListener for the "Launch Camera Capture" button. When this is 'clicked' by
	the user, it will launch innerID's Camera Capture. If invalid License file is detected, 
	the conversion will not begin and a Log message will be written with that error.*/
	public void launchCameraCapture(View view)
	{
		beginCapture.setEnabled(false);
		
		/*Launch the innerID Camera Capture, passing in 2 as the requestCode parameter. It
		will be used in the onActivityResult*/
		try 
		{
			innerID.launchCameraCapture(this, 2);
		} 
		catch (LicenseNotValid e) 
		{
			e.printStackTrace();
			beginCapture.setEnabled(true);
		}
		
	}
	
	/* onActivityResult is responsible for running commands after a launched Activity has 
	 * returned, including innerID's capture. The requestCode parameter passed in launchCameraCapture()
	 * above will be used to make sure the appropriate commands will be run after the innerID Camera
	 * Capture.
	 *
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		beginCapture.setEnabled(true);
        /* Run the following commands if the requestCode is 
		the one used by innerID Camera Capture and that it returned successfully*/
		
		if(requestCode == 2 & resultCode == RESULT_OK)
		{
			
			try 
			{
				//Change the static Boolean to true to indicate a new capture image had been done.
				newImageCapture = true;
				/*Open capturedImage from private application data using FileInput Stream
				 *This will always be the name of the captured image*/
				FileInputStream inputImage = openFileInput("capturedImage");
				
				
				//Decode the inputImage stream into a Bitmap
				input = BitmapFactory.decodeStream(inputImage);
				//Close inputImage stream to avoid memory leak.
				inputImage.close();
				
				UploadTask uploadTask = new UploadTask();
				uploadTask.execute();
				
			   
			}
		
			catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
			
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    // Handle item selection
	    switch (item.getItemId()) 
	    {
	        case R.id.about:
	        AboutDialog showAboutDialog = new AboutDialog();
	        showAboutDialog.show(getFragmentManager(), "aboutDialog");
	        default:
	        return super.onOptionsItemSelected(item);
	        	
	        
	    }
	}
	
	private class UploadTask extends AsyncTask<Void, Void, Void>
	{
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute()
		{
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setIcon(0);
			dialog.setMessage(getString(R.string.uploading));
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			
			try
			{
				long startTime = System.currentTimeMillis();
				String srvUrl = "http://alitesting-env.elasticbeanstalk.com/upload";
				//"http://172.16.29.124:8080/ServletTesting/upload";
				Wire new_wire = new Wire();
				//This should be FALSE, but Phoenix on RemoteServer is not correctly set up and true returns the intended 
				//grayscale
				boolean processBin = true;
				
				byte[] outputPixelData = {0};
				
				ByteArrayOutputStream uploadImageByteArray = new ByteArrayOutputStream();
				input.compress(Bitmap.CompressFormat.JPEG, 100, uploadImageByteArray);
				uploadImageByteArray.flush();
				uploadImageByteArray.close();
				
				ByteString uploadImage = ByteString.of(uploadImageByteArray.toByteArray());
				ByteString outputImage = ByteString.of(outputPixelData);
			
				FingerprintMessage uploadFp = new FingerprintMessage.Builder().rows(input.getHeight()).columns(input.getWidth()).bytelength(input.getHeight() * input.getWidth() * 4).pixelData(uploadImage).outputPixelData(outputImage).processBinarization(processBin).build();
				URL url = new URL(srvUrl);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				try 
				{
				     urlConnection.setDoOutput(true);
				     urlConnection.setDoInput(true);
				     urlConnection.setFixedLengthStreamingMode(uploadFp.toByteArray().length);
				     urlConnection.setRequestMethod("POST");
				     urlConnection.connect();
				     OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
				     out.write(uploadFp.toByteArray());
				     out.flush();
				     out.close();
				     
				     InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				     byte[] downloadFpRaw = IOUtils.toByteArray(in);
				     Log.d("downloadFpRaw length", Integer.toString(downloadFpRaw.length));
				     downloadFp = new_wire.parseFrom(downloadFpRaw, FingerprintMessage.class);
				     in.close();
				     long endtime = System.currentTimeMillis();
				     Log.d("AsyncTask do took this long: ", Long.toString(endtime-startTime));
				}
				finally
				{
					urlConnection.disconnect();
				}
			}
					
			catch (Exception e)
			{
				Log.e("innerID RemoteDemo", "CameraActivity.UploadTask", e);
				result = "Error: " + e.getMessage();
			}
			return null;
		
	
		}	
		
		@Override
		protected void onPostExecute(Void ignore)
		{
			dialog.dismiss();
			
			byte[] finalOutput = new byte[input.getWidth() * input.getHeight() * 4];
			Mat oneChannel = new Mat(input.getHeight(), input.getWidth(), CvType.CV_8UC1);
			Mat fourChannel = new Mat(input.getHeight(), input.getWidth(), CvType.CV_8UC4);
			long startTime = System.currentTimeMillis();
			try 
			{
				byte[] decompressedPixel = CompressionTools.decompress(downloadFp.outputPixelData.toByteArray());
				oneChannel.put(0, 0, decompressedPixel);
			} 
			catch (DataFormatException e) 
			{
				e.printStackTrace();
			}
			long endTime = System.currentTimeMillis();
			Log.d("Decompression time", Long.toString(endTime-startTime));
			Imgproc.cvtColor(oneChannel, fourChannel, Imgproc.COLOR_GRAY2RGBA);
			fourChannel.get(0, 0, finalOutput);
			/*The outputPixelData byte array is now filled with 4-channel pixel data. To correctly reconstruct a Bitmap from convertImage,
			 * the Bitmap copyPixelsFromBuffer method is needed. */

			//Create a ByteBuffer using the outputPixelData as input for the ByteBuffer wrap function
			ByteBuffer buffer2 = ByteBuffer.wrap(finalOutput);

			//Create a blank Bitmap with the correct parameters as the input image, including the Bitmap Config of ARGB_8888
			Bitmap returnFromConvert = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
			
			//Copy the outputPixelData pixels into the blank Bitmap using the ByteBuffer constructed earlier.
			returnFromConvert.copyPixelsFromBuffer(buffer2);
			
			//Display the created Bitmap in the ImageView of the Activity
			displayCapture.setImageBitmap(returnFromConvert);
			
			displayQualityScore.setText("Quality Score: " + Integer.toString(downloadFp));
		
		}
	}
}
