package guri.br.selfiestudio;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lucas on 26/05/2015.
 */
public class ControleActivity extends Activity{

    Button takePicture;
    ViewGroup controleLayout;
    ViewGroup buttonLayout;

    private static final int TIRAR_FOTO = 0;
    private static final int RECEBER_FOTO = 1;
    private static final int MSG_DESCONECTOU = 2;

    int buttonWidth;
    int buttonHeidht;

    public static TelaHandler mTelaHandler;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controle);

        controleLayout = (ViewGroup) findViewById(R.id.controle_layout);
        buttonLayout = (ViewGroup) findViewById(R.id.controle_button_layout);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mTelaHandler = new TelaHandler();

        takePicture = (Button) findViewById(R.id.tirar_foto);


        takePicture.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                buttonWidth = takePicture.getWidth();
                buttonHeidht = takePicture.getHeight();

                Toast.makeText(getApplicationContext(), "RECEBENDO FOTO", Toast.LENGTH_SHORT).show();

                String num = "-1";
                try {
                    if (MainActivity.os != null) {
                        MainActivity.os.write(num.getBytes());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    mTelaHandler.obtainMessage(MSG_DESCONECTOU, e.getMessage() + "[0]").sendToTarget();
                }

                buttonDisappear();
                //buttonAppear();

                //SystemClock.sleep(1700);

            }
        });


    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void buttonDisappear() {

        takePicture.setClickable(false);
        TransitionManager.beginDelayedTransition(buttonLayout);
        ViewGroup.LayoutParams sizeRules = takePicture.getLayoutParams();

        sizeRules.width = 1;
        sizeRules.height = 1;
        takePicture.setLayoutParams(sizeRules);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                takePicture.setVisibility(View.INVISIBLE);
            }
        }, 300);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void buttonAppear(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                takePicture.setVisibility(View.VISIBLE);
                takePicture.setClickable(true);

                TransitionManager.beginDelayedTransition(buttonLayout);
                ViewGroup.LayoutParams sizeRules = takePicture.getLayoutParams();
                sizeRules.width = buttonWidth;
                sizeRules.height = buttonHeidht;
                takePicture.setLayoutParams(sizeRules);
            }
        }, 100);
    }

    public class TelaHandler extends Handler {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case TIRAR_FOTO:
                    Toast.makeText(getApplicationContext(), "Dispositivou solicitou tirar foto",
                            Toast.LENGTH_SHORT).show();
                    CameraActivity.buttonTakePicture.performClick();
                    break;
                case RECEBER_FOTO:
                    //Toast.makeText(getApplicationContext(), "VAI RECEBER FOTO",Toast.LENGTH_SHORT).show();
                    try {
                        byte[] fotoBytes = (byte[]) msg.obj;
                        // renderiza a imagem na tela
                        ImageView image = (ImageView) findViewById(R.id.image);
                        Bitmap bMap = BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
                        image.setImageBitmap(bMap);
                        image.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

                        buttonAppear();

                        // salva a imagem no storage
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        File myExternalFile = new File(CameraActivity.getAlbumStorageDir("SelfieStudio"), "SS_" + timeStamp + ".jpg");
                        try {
                            FileOutputStream fos = new FileOutputStream(myExternalFile);
                            fos.write(fotoBytes);
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
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.erro_save_image), Toast.LENGTH_SHORT);
                            Log.e("ERRO AO SALVAR ARQUIVO", e.getMessage());
                        }

                    } catch (Exception e) {
                        Log.d("ERROOR SHOW IMAGE", e.getMessage());
                    }

                    break;
                case MSG_DESCONECTOU:
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_desconectou) + ": " + msg.obj.toString(),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    }

    @Override
    public void onBackPressed() {
        MainActivity.paraTudo();
        super.onBackPressed();
    }
}
