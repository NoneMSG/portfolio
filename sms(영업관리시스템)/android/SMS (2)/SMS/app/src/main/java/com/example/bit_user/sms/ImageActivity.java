package com.example.bit_user.sms;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.io.File;

public class ImageActivity extends AppCompatActivity {
    byte[] image;
    String filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ImageView image = (ImageView)findViewById(R.id.image);

        filePath= getIntent().getStringExtra("filePath");

        File file = new File(filePath);
        if(file.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            image.setImageBitmap(Bitmap.createScaledBitmap(bitmap,1208,920,false));
        }

    }

    public Bitmap byteArrayToBitmap(byte[] $byteArray ) {
        Bitmap bitmap = BitmapFactory.decodeByteArray( $byteArray, 0, $byteArray.length ) ;

        return bitmap ;
    }
}
