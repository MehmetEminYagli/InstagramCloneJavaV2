package com.mehmet.instagramclonejavav2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

public class FeedActivity extends AppCompatActivity {

    //firebase ile çıkış yapıcağımız için firebase'i aktive etmemiz gerekiyor
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        mAuth = FirebaseAuth.getInstance(); //firebase'i initailize ettik
    }


    //menüyü görmek istediğimiz aktivitiye eklememiz gerekiyor onu yapalım
    //bunun için iki tane fonksiyonu override etmemiz gerekiyordu


    //birincisi option menüyü oluşturmamız gerekiyor
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //menü inflateri çağırıyoruz
        MenuInflater menuInflater = getMenuInflater(); //ile menüyü çağırabiliyorduk
        menuInflater.inflate(R.menu.option_menu,menu); //ilede istediğimiz menüyü bağlayabiliyoruz

        //burada  oluşturuduğumuz menüyü istediğimiz aktiviteye bağladık
        return super.onCreateOptionsMenu(menu);
    }

    //İkincisi opsiyonlardan biri seçilirse ne olucağı
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.AddPost){
            //kullanıcı addpostta tıklandığında ne olucağını yazıyoruz
            //burada upload Aktivitiye gidicez
            Intent intent = new Intent(FeedActivity.this,UploadActivity.class);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.signout){
        //kullanıcı çıkış butonuna basınca ne olucağını yazıyoruz.
            //firebase'i kullanarak çıkış yapıcaz firebase dökümanlarında görebilirsin komutları


            mAuth.signOut(); //bu kadar çıkış işlemi :D


            Intent intent = new Intent(FeedActivity.this,MainActivity.class);
            startActivity(intent);
            finish();



        }

        //burada da menüde ayarladığımız butonlara tıklandığında ne olucağını yazıyoruz

        return super.onOptionsItemSelected(item);
    }
}