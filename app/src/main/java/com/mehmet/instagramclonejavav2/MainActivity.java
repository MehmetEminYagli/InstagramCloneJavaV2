package com.mehmet.instagramclonejavav2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mehmet.instagramclonejavav2.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        mAuth = FirebaseAuth.getInstance();

    }

    public void SignInClicked(View view){
        String Email = binding.EmailText.getText().toString();
        String Password = binding.passwordText.getText().toString();

        if (Email.equals("") || Password.equals("")){
            Toast.makeText(this, "Email veya şifre boş olamaz", Toast.LENGTH_SHORT).show();
        }else {
            mAuth.signInWithEmailAndPassword(Email,Password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                //giriş başarılı ise şunnu yap
                Intent intent = new Intent(MainActivity.this,FeedActivity.class);
                startActivity(intent);
                finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                //giriş başarılı değilse şunu yap

                Toast.makeText(MainActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }


    }
    public void SignUpClicked(View view){
        String Email = binding.EmailText.getText().toString();
        String Password = binding.passwordText.getText().toString();

    if(Email.equals("") || Password.equals("")){
        Toast.makeText(this, "Email veya şifre Boş okamaz", Toast.LENGTH_SHORT).show();
    }else {
        mAuth.createUserWithEmailAndPassword(Email,Password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
        //başarılı ise şunu yap
                Intent intent = new Intent(MainActivity.this,FeedActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
        //başarısız ise şunu yap
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }





    }

}