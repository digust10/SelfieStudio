package guri.br.selfiestudio;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater controlInflater = null;
    public static Button buttonTakePicture;
    ViewGroup cameraLayout;
    private int buttonWidth;
    private int buttonHeidht;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_android_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView)findViewById(R.id.camerapreview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.control, null);
        LayoutParams layoutParamsControl
                = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);

        cameraLayout = (ViewGroup) findViewById(R.id.camera_layout);

        buttonTakePicture = (Button)findViewById(R.id.takepicture);
        buttonTakePicture.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                buttonWidth = buttonTakePicture.getWidth();
                buttonHeidht = buttonTakePicture.getHeight();
                Camera.Parameters p = camera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                //p.set("orientation", "portrait");
                //p.set("rotation", 90);
                camera.setParameters(p);
                camera.takePicture(myShutterCallback,
                        myPictureCallback_RAW, myPictureCallback_JPG);

                buttonDisappear();
                buttonAppear();
            }});
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void buttonDisappear() {
        buttonTakePicture.setClickable(false);
        TransitionManager.beginDelayedTransition(cameraLayout);
        buttonTakePicture.setVisibility(View.INVISIBLE);
        LayoutParams sizeRules = buttonTakePicture.getLayoutParams();
        sizeRules.width = 1;
        sizeRules.height = 1;
        buttonTakePicture.setLayoutParams(sizeRules);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 300);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void buttonAppear(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                buttonTakePicture.setVisibility(View.VISIBLE);
                buttonTakePicture.setClickable(true);

                TransitionManager.beginDelayedTransition(cameraLayout);
                ViewGroup.LayoutParams sizeRules = buttonTakePicture.getLayoutParams();
                sizeRules.width = buttonWidth;
                sizeRules.height = buttonHeidht;
                buttonTakePicture.setLayoutParams(sizeRules);
            }
        }, 3000);
    }

    protected void onPause() {
        super.onPause();
        //MainActivity.paraTudo();
    }

    /*private void releaseCamera(){
        if (camera != null){
            camera.release();
            //camera = null;
        }
    }*/


    ShutterCallback myShutterCallback = new ShutterCallback(){

        @Override
        public void onShutter() {
            // TODO Auto-generated method stub

        }};

    PictureCallback myPictureCallback_RAW = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
        }};

    PictureCallback myPictureCallback_JPG = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            //Bitmap bitmap = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
            if (isExternalStorageWritable()) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File f = getAlbumStorageDir("SelfieStudio");
                File myExternalFile = new File(f, "SS_" + timeStamp + ".jpg");
                try {
                    // salvando a foto no storage
                    FileOutputStream fos = new FileOutputStream(myExternalFile);
                    fos.write(arg0);
                    fos.close();


                    /*Intent mediaScan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScan.setDataAndType(Uri.parse(myExternalFile.getPath()), "image/*");
                    sendBroadcast(mediaScan);*/
                    MediaScannerConnection.scanFile(getApplicationContext(),
                            new String[]{myExternalFile.toString()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> uri=" + uri);
                                }
                            });
                    // escrevendo o array de bytes da foto pelo outputstream
                    if (MainActivity.mThreadComunicacao != null){
                        //BitmapFactory bitmapFactory = new BitmapFactory();
                        //BitmapFactory.Options options = new BitmapFactory.Options();
                        //options.inJustDecodeBounds = true;
                        //options.inSampleSize = 4;
                        //Bitmap image = bitmapFactory.decodeByteArray(arg0, 0, arg0.length, options);

                        Bitmap image = decodeSampledBitmapFromByteArray(arg0, 1136, 640);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                        byte[] imageBytes = stream.toByteArray();


                        //int tamanhoDaImagem = imageBytes.length;
                        byte[] oneByte = new byte[1];

                        DataOutputStream outputstream = MainActivity.mThreadComunicacao.getOutputStream();
                        //outputstream.write(tamanhoDaImagem);
                        synchronized (outputstream){outputstream.write(imageBytes);}
                        //Envia um byte para sinalizar o fim da transferência da imagem.
                        SystemClock.sleep(2000);
                        synchronized (outputstream){outputstream.write(oneByte);}

                        camera.stopPreview();
                        camera.startPreview();

                        //outputstream.write(oneByte);
                    }

                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.erro_save_image), Toast.LENGTH_SHORT);
                    Log.e("ERRO AO SALVAR ARQUIVO", e.getMessage());
                }
            }
        }};

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromByteArray(byte[] bytes, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("DIRECTORYYY", "Directory not created");
        }
        return file;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        if(previewing){
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null){
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                previewing = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera = Camera.open();
        //Camera.Parameters parameters = camera.getParameters();
        //parameters.set("jpeg-quality", 70);
        //parameters.setPictureFormat(PixelFormat.JPEG);
        //parameters.setPictureSize(2048, 1232);
        //camera.setParameters(parameters);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        //if (camera != null) {
        camera.stopPreview();
        camera.release();
        camera = null;
        //}

        previewing = false;
    }

    @Override
    public void onBackPressed() {
        MainActivity.paraTudo();
        super.onBackPressed();
    }
}