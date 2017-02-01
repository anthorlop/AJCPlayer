package com.ajc.playerex.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ajc.playerex.R;
import com.ajc.playerex.activities.AudioActivity;
import com.ajc.playerex.activities.SecondAudioActivity;
import com.ajc.playerex.activities.VideoListActivity;
import com.ajc.playerex.activities.VideoPlayerActivity;
import com.ajc.playerex.app.ExampleApp;

import javax.inject.Inject;

import es.lombrinus.projects.mods.chromecastlib.CustomMediaRouteButton;
import es.lombrinus.projects.mods.chromecastlib.interfaces.AJCast;

public class MainActivity extends AppCompatActivity {

    public static final String APP_NAME = "PlayerExample";

    private static final int SELECT_PHOTO = 3423;

    private Uri selectedFile;

    @Inject
    AJCast chromeCast;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        chromeCast.init((CustomMediaRouteButton) findViewById(R.id.media_route_btn));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((ExampleApp) getApplication()).getComponentVideo().inject(this);

        chromeCast.build(this);

        // To change chromecast icons
        //chromeCast.setCastIcons();

        final Button play = (Button) findViewById(R.id.button);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AudioActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        final Button play2 = (Button) findViewById(R.id.button2);
        play2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondAudioActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        final Button openListVideos = (Button) findViewById(R.id.buttonListVideo);
        if (openListVideos != null) {
            openListVideos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, VideoListActivity.class);
                    MainActivity.this.startActivity(intent);
                }
            });
        }

        final EditText urlEt = (EditText) findViewById(R.id.urlVideo);
        final Button buttonVideoUrl = (Button) findViewById(R.id.buttonVideoUrl);
        if (buttonVideoUrl != null) {
            buttonVideoUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString(VideoPlayerActivity.VIDEO_ID, "0");
                    extras.putString(VideoPlayerActivity.VIDEO_URL, urlEt.getText().toString());
                    extras.putBoolean(VideoPlayerActivity.AUTO_PLAY_ID, true);
                    intent.putExtras(extras);
                    MainActivity.this.startActivity(intent);
                }
            });
        }

        final Button buttonVideoLocal = (Button) findViewById(R.id.buttonSelectVideo);
        if (buttonVideoLocal != null) {
            buttonVideoLocal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("video/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                }
            });
        }

    }

    private void goToVideo(String value) {
        Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
        Bundle extras = new Bundle();
        extras.putString(VideoPlayerActivity.VIDEO_ID, value);
        extras.putBoolean(VideoPlayerActivity.AUTO_PLAY_ID, true);
        intent.putExtras(extras);
        MainActivity.this.startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SELECT_PHOTO) {
            if (resultCode == RESULT_OK) {

                selectedFile = data.getData();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.System.canWrite(this)) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 2909);
                    } else {
                        // continue with your code
                        playLocalVideo();
                    }
                } else {
                    // continue with your code
                    playLocalVideo();
                }
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void playLocalVideo() {
        Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);

        Bundle extras = new Bundle();
        extras.putString(VideoPlayerActivity.VIDEO_PATH, getRealPathFromURI(selectedFile));
        intent.putExtras(extras);

        startActivity(intent);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String videoPath = contentUri.getPath();

        try{
            String[] proj = { MediaStore.Video.Media.DATA };
            //Cursor cursor = activity.managedQuery(contentUri, proj, null, null,
            //null);
            Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
            Log.i("cursor", "upload-->" + cursor);
            Log.i("contentUri", "upload-->" + contentUri);
            Log.i("proj", "upload-->" + proj);
            int position=0;

            if (cursor !=null && cursor.moveToPosition(position)) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                Log.i("column_index", "Upload-->" + column_index);
                videoPath = cursor.getString(column_index);  //I got a null pointer exception here.(But cursor hreturns saome value)
                Log.i("videoPath", "Upload-->" + videoPath);
                cursor.close();

            }

        } catch(Exception e){
            Log.e("MainActivity", e.toString());
            return contentUri.getPath();
        }

        return videoPath;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 2909: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("Permission", "Granted");
                    playLocalVideo();
                } else {
                    Log.e("Permission", "Denied");
                }
                return;
            }
        }
    }
}
