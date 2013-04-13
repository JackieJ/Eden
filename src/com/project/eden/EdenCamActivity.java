package com.project.eden;

import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

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
import android.graphics.Bitmap;




@SuppressLint("NewApi")
public class EdenCamActivity extends Activity {

    //memory cache for bitmap
    private LruCache<String, Bitmap> mMemoryCache;

    private FrameLayout layout;
    //face recognition following regular OpenCV process
    private FaceRecognition faceReco;
    //action class after face is captured.
    private FaceCapture faceCapture;
    
    public boolean isStarted = false;
    
    //UI
     
    int counter_display = 0;
    AlertDialog alert_dialog;
    public static int screensize_x = 1280;
    public static int screensize_y = 768;
    public static int buffer_zone_value = 20;
    public static int offset_x = 20;
    public static int offset_y = 20;
    public static ArrayList<String> info_list = new ArrayList<String>();
    public void addItem() {
    	info_list.add("UCLA");
    	info_list.add("Single");
    	info_list.add("Aerobatic Gymnastics");
    }
	public int boxX1 = -1, boxY1 = -1, boxX2 = -1, boxY2 = -1;
	public static Rect rect_face_reg;
    public void displayDialog(int center_x, int center_y, int x, int y) {
        if (counter_display==0) {
            addItem();
            counter_display = 1;
        }
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Jackie Jin")

            .setItems(info_list.toArray(new CharSequence[info_list.size()]), new DialogInterface.OnClickListener() {
                    //.setItems(R.array.info_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
      			   
                    }
                })
            .setPositiveButton("View On Facebook", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String url = "http://www.facebook.com/jackie.jin2";
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

    }

    public void viewOnFacebook (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }
    private OnClickListener mCorkyListener = new OnClickListener() {
        public void onClick(View v) {
          // do something when the button is clicked
        }

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			
		}
    };
    
    //Face tracker
    public int faceTracker = 0;
    
    //queue for the bitmap memory-cached keys
    public ArrayDeque<String> cachedKeyQueue = new ArrayDeque<String>();
    public int MAXCACHENUM = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //caching setup 
    	final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create our Preview view and set it as the content of our activity.
        try {
            layout = new FrameLayout(this);
            faceReco = new FaceRecognition(this);
            faceCapture = new FaceCapture(this, faceReco);
            layout.addView(faceCapture);
            layout.addView(faceReco);
            setContentView(layout);
        } catch (IOException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
        }
        
        
    }

    //bitmap memory cache handling
    //Current: the keys are numbers 
    //TODO: the keys should be the meta data scraped by the facebook API
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);

        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public Bitmap removeBitmapFromMemCache(String key) {
    	return mMemoryCache.remove(key);
    }
    
}

class FaceRecognition extends View implements Camera.PreviewCallback {

    public static final int SUBSAMPLING_FACTOR = 4;

    //OpenCV face detection
    private IplImage grayImage;
    private CvHaarClassifierCascade classifier;
    private CvMemStorage storage;
    private CvSeq faces;
    EdenCamActivity eContext;
    
    
    public FaceRecognition(EdenCamActivity context) throws IOException {
        super(context);
        eContext = context;
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
        eContext.faceTracker = faces.total();
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
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setTextSize(40);
        paint.setAntiAlias(true);


        String s = "Project Eden Baby";
        float textWidth = paint.measureText(s);
        canvas.drawText(s, (getWidth()-textWidth)/2, 40, paint);
        int mainPad = 30;
        int mainPad2 = 10;
        int mainHeight = 96;
        int mainWidth = 128;
        int mainTopX = 20;
        int mainTopY = 20;
        int mainBotX = mainTopX + mainWidth;
        int mainBotY = mainTopY + mainHeight;
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        if (eContext.cachedKeyQueue.size() != 0 && faces != null) {
            //align the icons from images cached in memory
            //create a queue iterator
            //iterate through eContext.cachedKeyQueue
        	
        	// Create Sidebar
            Iterator<String> it = eContext.cachedKeyQueue.iterator();
            while (it.hasNext())
                {
                    paint.setARGB(120,216,216,216);
                    canvas.drawRect(mainTopX-mainPad2, mainTopY-mainPad2, mainBotX+mainPad2, mainBotY+mainPad2, paint);
            
                    Bitmap img = eContext.getBitmapFromMemCache(it.next());
                    Rect box = new Rect(mainTopX, mainTopY, mainBotX, mainBotY);
                    canvas.drawBitmap(img, null, box, null);
        
                    mainTopY += mainHeight + mainPad;
                    mainBotY += mainHeight + mainPad;
                }
            
            //canvas.drawBitmap(bitmap, src, dst, paint)
        }


        
        if (faces != null) {
            float padding = 20;
        
            paint.setStrokeWidth(4);
            paint.setStyle(Paint.Style.STROKE);
            float scaleX = (float)getWidth()/grayImage.width();
            float scaleY = (float)getHeight()/grayImage.height();
            float topLeftX, topLeftY, botRightX, botRightY;
            float faceWidth;
            float x,y,w,h;
            float boxWidth = 150;
            float boxHeight = 150;
            int total = faces.total();
            float tx = -1, ty = -1, tw = -1, th = -1;
            
            for (int i = 0; i < total; i++) {
            	CvRect r = new CvRect(cvGetSeqElem(faces, i));
            	x = r.x(); 
            	y = r.y(); 
            	w = r.width(); 
                h = r.height();
                if (w*h > tw*th) {
                	tw = w;
                	th = h;
                	tx = x;
                	ty = y;
                }
            }
                x = tx;
                y = ty;
                w = tw;
                h = th;
                if (x != -1 && y != -1 && w != -1 && h != -1)
                {
                topLeftX = x*scaleX;
                topLeftY = y*scaleY;
                botRightX = (x+w)*scaleX;
                botRightY = (y+h)*scaleY;
                faceWidth = botRightX - topLeftX;
                //paint.setColor(Color.GREEN);
                paint.setARGB(120,216,216,216);
                
                canvas.drawRect(topLeftX, topLeftY, botRightX, botRightY, paint);
                eContext.rect_face_reg = new Rect((int)topLeftX, (int)topLeftY, (int)botRightX, (int)botRightY);
                // Draw on left or right of face
                //paint.setColor(Color.BLUE);
                paint.setARGB(180,236,236,236);

                paint.setTextSize(45);
                paint.setStrokeWidth(2);
                Typeface face = null;
                face.defaultFromStyle(3); 

                String faceName = "Jackie Jin";
                String faceSchool = "UCLA";
                String faceStatus = "Single";
                String faceInterests = "Aerobatic Gymnastics";
                paint.setTypeface(face);
                
                if ((topLeftX + boxWidth + faceWidth + padding) < (float)getWidth()) {
                    //for (int j = 0; j < 30; j++)
                        {
                            //canvas.drawRect(topLeftX + faceWidth + padding, topLeftY, 
                            //topLeftX + boxWidth + faceWidth + padding, topLeftY + boxHeight, paint);
                            //canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.facebook_logo), topLeftX + faceWidth + padding, topLeftY, null);
                        	canvas.drawText(faceName, topLeftX + faceWidth + padding, topLeftY, paint);
                        	canvas.drawText(faceSchool, topLeftX + faceWidth + padding, topLeftY+padding*3, paint);
                        	canvas.drawText(faceStatus, topLeftX + faceWidth + padding, topLeftY+padding*6, paint);
                        	canvas.drawText(faceInterests, topLeftX + faceWidth + padding, topLeftY+padding*9, paint);

                        }
                	}
                else {
                    //for (int j = 0; j < 30; j++)
                        {
                            //canvas.drawRect(topLeftX - boxWidth - padding, topLeftY, 
                            //botRightX - faceWidth - padding, topLeftY + boxHeight, paint);
                           //canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.facebook_logo), topLeftX - boxWidth - padding, topLeftY, null);
                       		//canvas.drawText("Jackie Jin", topLeftX + faceWidth + padding,topLeftY, paint);
                    	canvas.drawText(faceName, topLeftX + faceWidth + padding, topLeftY, paint);
                    	canvas.drawText(faceSchool, topLeftX + faceWidth + padding, topLeftY+padding*3, paint);
                    	canvas.drawText(faceStatus, topLeftX + faceWidth + padding, topLeftY+padding*6, paint);
                    	canvas.drawText(faceInterests, topLeftX + faceWidth + padding, topLeftY+padding*9, paint);

                        }
                }
                }
                eContext.isStarted = true;
                eContext.boxX1 = (int)(w*scaleX);
                eContext.boxY1 = (int)(h*scaleY);
                eContext.boxX2 = (int)x;
                eContext.boxY2 = (int)y;
                
            }
        }
    }

class FaceCapture extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    Camera.PreviewCallback previewCallback;
    EdenCamActivity mainContext;
    private int keyCounter = 0;
    FaceCapture(Context context, Camera.PreviewCallback previewCallback) {
        super(context);
        mainContext = (EdenCamActivity) context;
        this.previewCallback = previewCallback;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }
    Rect rect1 = new Rect(10,10,148,116);
    Rect rect2 = new Rect(10,126,148,252);
    Rect rect3 = new Rect(10,242,148,388);
    Rect rect4 = new Rect(10,358,148,524);
    Rect rect5 = new Rect(10,474,148,660);
    //touch the designated area to trigger picture event
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //report touch position
        float x = e.getX();
        float y = e.getY();
        
        if(mainContext.cachedKeyQueue.size()==1){
        	if (rect1.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=10151352918320723&set=oa.441715655907536&type=1&theater");
        	}
        } else if (mainContext.cachedKeyQueue.size()==2) {
        	if (rect1.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=10151352918320723&set=oa.441715655907536&type=1&theater");
        	} else if (rect2.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=10200591369440018&set=oa.441715655907536&type=1&theater");
        	}
        } else if (mainContext.cachedKeyQueue.size()==3) {
        	if (rect1.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=10151352918320723&set=oa.441715655907536&type=1&theater");
        	} else if (rect2.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=10200591369440018&set=oa.441715655907536&type=1&theater");
        	} else if (rect3.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=976799309646&set=oa.441715655907536&type=1&theater");
        	}
        } else if (mainContext.cachedKeyQueue.size()==4) {
        	if (rect1.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=10151352918320723&set=oa.441715655907536&type=1&theater");
        	} else if (rect2.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=10200591369440018&set=oa.441715655907536&type=1&theater");
        	} else if (rect3.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=976799309646&set=oa.441715655907536&type=1&theater");
        	} else if (rect4.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=10151304240820988&set=oa.441715655907536&type=1&theater");
        	}
        } else if (mainContext.cachedKeyQueue.size()==5) {
        	if (rect1.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=10151352918320723&set=oa.441715655907536&type=1&theater");
        	} else if (rect2.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=10200591369440018&set=oa.441715655907536&type=1&theater");
        	} else if (rect3.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=976799309646&set=oa.441715655907536&type=1&theater");
        	} else if (rect4.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=10151304240820988&set=oa.441715655907536&type=1&theater");
        	} else if (rect5.contains((int)x,(int)y)) {
        		mainContext.viewOnFacebook("https://www.facebook.com/photo.php?fbid=10200938171801255&set=oa.441715655907536&type=1&theater");
        	}
        }
        
        //retrieve desired position
        
        int action1 = e.getAction();
        //take picture when the pressure releases
        if (action1 == MotionEvent.ACTION_UP && mainContext.faceTracker != 0) {
        	if (mainContext.boxX1 != -1 &&
            		mainContext.boxX2 != -1 &&
            		mainContext.boxY1 != -1 &&
            		mainContext.boxY2 != -1) {
        		if (mainContext.rect_face_reg.contains((int)x, (int)y)) {
            	mainContext.displayDialog(mainContext.boxX1, mainContext.boxY1, mainContext.boxX2, mainContext.boxY2);
        		}
        		}
            
        	mCamera.takePicture(null, null, pictureCallback);
        }
    	
        return true;
    }
    
    //picture callback set up 
    PictureCallback pictureCallback = new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                // Picture processing
                Log.d("Camera","Icon Picture Taken! Load the image to Cache!");
                mCamera.startPreview();

                //decode raw bytes to bitmap format
                Bitmap loadImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                /*************************/
                /* Image Comparison TODO */
                /*************************/
                
                
                
                int qSize = mainContext.cachedKeyQueue.size();
                
                
                
                if (qSize < mainContext.MAXCACHENUM) {
                    //add the preview frame to cache
                    Log.d("CACHE NOT FULL","CACHE NOT FULL!");
                    mainContext.addBitmapToMemoryCache(String.valueOf(keyCounter), loadImage);
                    mainContext.cachedKeyQueue.addLast(String.valueOf(keyCounter));
                } else {
                    //shift the list from top to bottom
                    //remove the early preview and add the lastest one
                    Log.d("CACHE FULL",String.valueOf(keyCounter));
                    String removeKey = mainContext.cachedKeyQueue.pollFirst();
                    mainContext.removeBitmapFromMemCache(removeKey);
                	
                    //add new key pair at the end
                    mainContext.addBitmapToMemoryCache(String.valueOf(keyCounter), loadImage);
                    mainContext.cachedKeyQueue.addLast(String.valueOf(keyCounter));
                }
                //DEBUG
                Iterator<String>  it = mainContext.cachedKeyQueue.iterator();
                while  (it.hasNext()) {
                    Log.d("image",String.valueOf(it.next()));
                }
                Log.d("SizeQQ", String.valueOf(mainContext.cachedKeyQueue.size()));
                
                keyCounter++;
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