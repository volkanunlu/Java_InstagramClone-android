package com.volkanunlu.instagramclonejava.view;

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
import com.google.firebase.auth.FirebaseUser;
import com.volkanunlu.instagramclonejava.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;  //elementlerime ulaşmak adına binding işlemimi yaptım.

    private FirebaseAuth auth; //firebase'den auth oluşturdum.Kullanıcı işlemleri için.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth=FirebaseAuth.getInstance();

        FirebaseUser firebaseUser= auth.getCurrentUser(); //daha önceki kullanıcı giriş yaptıysa bu oladabilir,olmayadabilir.

        if(firebaseUser!=null){
            Intent intent=new Intent(MainActivity.this,FeedActivity.class);
            startActivity(intent);
            finish();
        }

    }


    public void signInClicked(View view){

        String email=binding.emailText.getText().toString();
        String password=binding.passwordText.getText().toString();

        if(email.equals("")|| password.equals("")){

            Toast.makeText(this, "Please Enter email or password!", Toast.LENGTH_LONG).show();
        }
        else
        {       //kullanıcı giriş yaparkenki yazdığımız firebase metodu
          auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
              @Override
              public void onSuccess(AuthResult authResult) {

                  Intent intent=new Intent(MainActivity.this,FeedActivity.class);
                  startActivity(intent);
                  finish();
              }
          }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                  Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
              }
          });

        }


    }


    public void signUpClicked(View view){

        String email=binding.emailText.getText().toString();
        String password=binding.passwordText.getText().toString();

        if(email.equals("")|| password.equals(""))
        {
            Toast.makeText(this, "Enter email and Password!", Toast.LENGTH_LONG).show();

        }
        else{
               //bu yapıyı kullanma sebebi asenkron işlemlerde kullanıcıyı duraklatmamak,veriyi arka planda halletme isteği

                //kulllanıcı oluşturmak için firebase metodumuz
            auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override                                      //başarılı olduğumuzda çalıştırdığımız metot.
                public void onSuccess(AuthResult authResult) {
                    Intent intent=new Intent(MainActivity.this,FeedActivity.class); //kullanıcı oluştuğunda gideceği yer
                    startActivity(intent); //aktivite başlat.
                    finish(); //bu alanla işimiz bitti.
                }
                //hata olursa ne yapılsın.?!
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }   //e.getLocalizedMessage() kullanıcının anladığı dilden mesaj oluştur.
            });
        }




    }
}