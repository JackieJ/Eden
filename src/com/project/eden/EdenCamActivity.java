package com.project.eden;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

//Java OpenCV Wrapper Library
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;


public class EdenCamActivity extends Activity {
	
	private FrameLayout layout;
	//face recognition following regular OpenCV process
	private FaceRecognition faceView;
    //action class after face is captured.
	private FaceCapture faceCapture;
		
	int counter_display = 0;
    AlertDialog alert_dialog;
    public static int screensize_x = 1280;
	public static int screensize_y = 768;
	public static int buffer_zone_value = 20;
	public static int offset_x = 20;
	public static int offset_y = 20;
	public static ArrayList<String> info_list = new ArrayList<String>();
	public void addItem() {
	info_list.add("School");
	info_list.add("Relationship");
	info_list.add("Interests");
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create our Preview view and set it as the content of our activity.
        try {
            layout = new FrameLayout(this);
            faceView = new FaceRecognition(this);
            faceCapture = new FaceCapture(this, faceView);
            layout.addView(faceCapture);
            layout.addView(faceView);
            setContentView(layout);
        } catch (IOException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
        }
        
        
    }
	
	 public void displayDialog(int center_x, int center_y, int x, int y) {
		 if (counter_display==0) {
			 addItem();
			 counter_display = 1;
		 }
      // Use the Builder class for convenient dialog construction
		 //ContextThemeWrapper context_wrapper = new ContextThemeWrapper(this, R.style.Theme_Dialog);
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Name")
      //builder.setTitle(R.string.name)
      // use setAdapter() ListAdapter
      //List<String> list = Arrays.asList("foo", "bar", "waa");
      //CharSequence[] cs = list.toArray(new CharSequence[list.size()]);
      //System.out.println(Arrays.toString(cs));
      	.setItems(info_list.toArray(new CharSequence[info_list.size()]), new DialogInterface.OnClickListener() {
      	   //.setItems(R.array.info_array, new DialogInterface.OnClickListener() {
      		   public void onClick(DialogInterface dialog, int id) {
      			   
      		   }
      	   })
             .setPositiveButton("View On Facebook", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
              	   String url = "";
              	   viewOnFacebook (url);
                 }
             })
             .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     //dialog.cancel();
                 }
             });
      alert_dialog = builder.create();
      int[] i_coord = new int[2];
      i_coord = dynamicPosition (center_x, center_y, alert_dialog);
      alert_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);     
      WindowManager.LayoutParams params = alert_dialog.getWindow().getAttributes();
		int i_x, i_y;
		i_x = i_coord[0];
		i_y = i_coord[1];
		params.x = center_x + i_x*offset_x; //x coordinate of the rect
		params.y = center_y + i_y*offset_y;
		//alert_dialog.getWindow().setLayout(200,300);
		//params.copyFrom(alert_dialog.getWindow().getAttributes());
		//params.width = 150;
		//params.height = 500;
		//params.x= 0;
		//params.y= 0 ;
		alert_dialog.getWindow().setAttributes(params);
		alert_dialog.show();
      // Create the AlertDialog object and return it
  }
	
	 public int[] dynamicPosition (int x, int y, AlertDialog alert_dialog) {
			int i_x;
			int i_y;
			int[] i_coord = new int[2];
			if ((screensize_x-buffer_zone_value) <= x && x <= (screensize_y+buffer_zone_value)) {
				if (y >= screensize_y/2) {
					i_x = 0;
					i_y = 1;
				}
				else {
					i_x = 0;
					i_y = -1;
				}
			} else if (x <= screensize_x/2 && y <= screensize_y/2) {
				i_x = 1;
				i_y = 1;
			} else if (x >= screensize_x/2 && y <= screensize_y/2) {
				i_x = -1;
				i_y = 1;
			} else if (x <= screensize_x/2 && y >= screensize_y/2) {
				i_x = 1;
				i_y = -1;
			} else {
				i_x = -1;
				i_y = -1;
			}
			i_coord[0] = i_x;
			i_coord[1] = i_y;
			return i_coord;
			/*
			 alertDialog.show();
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

			lp.copyFrom(alertDialog.getWindow().getAttributes());
			lp.width = 150;
			lp.height = 500;
			lp.x=-170;
			lp.y=100;
			alertDialog.getWindow().setAttributes(lp);
			 */
		}
		
		public void viewOnFacebook (String url) {
			Uri uriUrl = Uri.parse(url);
			Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
			startActivity(launchBrowser);
		}
}

class FaceRecognition extends View implements Camera.PreviewCallback {
	
	public static final int SUBSAMPLING_FACTOR = 4;

    private IplImage grayImage;
    private CvHaarClassifierCascade classifier;
    private CvMemStorage storage;
    private CvSeq faces;
    private EdenCamActivity cam_activity; 
    
    public FaceRecognition(EdenCamActivity context) throws IOException {
        super(context);
        cam_activity = context;
        // Load the classifier file from Java resources.
        File classifierFile = Loader.extractResource(getClass(),
            "/com/project/eden/haarcascade_frontalface_alt.xml",
            context.getCacheDir(), "classifier", ".xml");
        if (classifierFile == null || classifierFile.length() <= 0) {
            throw new IOException("Could not extract the classifier file from Java resource.");
        }

        // Preload the opencv_objdetect module to work around a known bug.
        Loader.load(opencv_objdetect.class);
        classifier = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
        classifierFile.delete();
        if (classifier.isNull()) {
            throw new IOException("Could not load the classifier file.");
        }
        storage = CvMemStorage.create();
    }

    public void onPreviewFrame(final byte[] data, final Camera camera) {
        try {
            Camera.Size size = camera.getParameters().getPreviewSize();
            processImage(data, size.width, size.height);
            camera.addCallbackBuffer(data);
        } catch (RuntimeException e) {
            // The camera has probably just been released, ignore.
        }
    }

    protected void processImage(byte[] data, int width, int height) {
        // First, downsample our image and convert it into a grayscale IplImage
        int f = SUBSAMPLING_FACTOR;
        if (grayImage == null || grayImage.width() != width/f || grayImage.height() != height/f) {
            grayImage = IplImage.create(width/f, height/f, IPL_DEPTH_8U, 1);
        }
        int imageWidth  = grayImage.width();
        int imageHeight = grayImage.height();
        int dataStride = f*width;
        int imageStride = grayImage.widthStep();
        ByteBuffer imageBuffer = grayImage.getByteBuffer();
        for (int y = 0; y < imageHeight; y++) {
            int dataLine = y*dataStride;
            int imageLine = y*imageStride;
            for (int x = 0; x < imageWidth; x++) {
                imageBuffer.put(imageLine + x, data[dataLine + f*x]);
            }
        }

        cvClearMemStorage(storage);
        faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
        postInvalidate();
    }

    //collect face coordinates
    public List<int[]> getFaceCoordX() {
    	List<int[]> fcoords = new ArrayList<int[]>();
    	
    	//float scaleX = (float)getWidth()/grayImage.width();
        //float scaleY = (float)getHeight()/grayImage.height();
    	
    	if (faces != null) {
    		int total = faces.total();
    		for (int i = 0; i < total; i++) {
    			CvRect r = new CvRect(cvGetSeqElem(faces, i));
    			int x = r.x(), y = r.y(), w = r.width(), h = r.height();
    			//lower bound x, lower bound y, upper bound x, upper bound y
    			int[] coord = new int[4];
    			coord[0] = x;
    			coord[1] = y;
    			coord[2] = x+w;
    			coord[3] = y+h;
    			fcoords.add(coord);
    		}
    	}
    	return fcoords;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setTextSize(40);

        String s = "Project Eden Baby:D";
        float textWidth = paint.measureText(s);
        canvas.drawText(s, (getWidth()-textWidth)/2, 40, paint);

        if (faces != null) {
            paint.setStrokeWidth(4);
            paint.setStyle(Paint.Style.STROKE);
            int scaleX = (int)getWidth()/grayImage.width();
            int scaleY = (int)getHeight()/grayImage.height();
            int total = faces.total();
            for (int i = 0; i < total; i++) {
                CvRect r = new CvRect(cvGetSeqElem(faces, i));
                int x = r.x(), y = r.y(), w = r.width(), h = r.height();
                paint.setColor(Color.GREEN);
                canvas.drawRect(x*scaleX, y*scaleY, (x+w)*scaleX, (y+h)*scaleY, paint);
                cam_activity.displayDialog(w*scaleX, h*scaleY, x, y);
            }
        }
    }
   
	
}

class FaceCapture extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    Camera.PreviewCallback previewCallback;
    
    FaceCapture(Context context, Camera.PreviewCallback previewCallback) {
        super(context);
        this.previewCallback = previewCallback;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }
    
    //touch the designated area to trigger picture event
    @Override
    public boolean onTouchEvent(MotionEvent e) {
    	//report touch position
    	float x = e.getX();
        float y = e.getY();
        
        //retrieve desired position
        
        int action1 = e.getAction();
        //take picture when the pressure releases
        if (action1 == MotionEvent.ACTION_UP) {
        	mCamera.takePicture(null, null, pictureCallback);
        }
    	
    	return true;
    }
    
    //picture callback set up 
    PictureCallback pictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// Picture processing
			Log.d("Camera","Picture Taken!");
			mCamera.startPreview();
		}
    };
    


    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        mCamera = Camera.open();
        try {
           mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;

        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();

        List<Size> sizes = parameters.getSupportedPreviewSizes();
        Size optimalSize = getOptimalPreviewSize(sizes, w, h);
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);

        mCamera.setParameters(parameters);
        if (previewCallback != null) {
            mCamera.setPreviewCallbackWithBuffer(previewCallback);
            Camera.Size size = parameters.getPreviewSize();
            byte[] data = new byte[size.width*size.height*
                    ImageFormat.getBitsPerPixel(parameters.getPreviewFormat())/8];
            mCamera.addCallbackBuffer(data);
        }
        mCamera.startPreview();
    }

}
