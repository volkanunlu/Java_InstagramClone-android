package com.volkanunlu.instagramclonejava.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.volkanunlu.instagramclonejava.databinding.ActivityUploadBinding;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {


    private FirebaseStorage firebaseStorage;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private StorageReference storageReference;

    Uri imageData;
    ActivityResultLauncher<Intent> activityResultLauncher;  //galeriye gidip veriyi alıp getirdiğimiz intent
    ActivityResultLauncher<String> permissionLauncher;    //izin alma işlemlerinde kullanacağım.
    private ActivityUploadBinding binding;
    //  Bitmap selectedImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUploadBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);  //binding işlemim
        registerLauncher();   //register çağırarak diğer yerlerde kullanıma hazır hale getirdim.

        firebaseStorage= FirebaseStorage.getInstance(); //storage kullanım firebase
        firebaseAuth= FirebaseAuth.getInstance(); //auth kullanım firebase
        firebaseFirestore=FirebaseFirestore.getInstance();  //firestore kullanım firebase
        storageReference=firebaseStorage.getReference(); //bunu referans için kullanıcam.
    }


    public void uploadButtonClicked(View view){

        if(imageData!=null){ //resimi seçti mi seçmedi mi emin olmak istiyorum.

            //universal unique id --> her seferinde eklenen resmi bir önceki ile değiştirmesin eski yeni hepsini alabilsin diye
            //kullandığımız sınıflardan biri.

            UUID uuid= UUID.randomUUID(); //uydurma bir isim oluşturuyor.

            String imageName= "images/" + uuid +".jpg"; //değişkenimin ismini verdi uuid ile. her seferinde ayrı bir isim olacak böylece.

                            //klasör oluştur//alt dizin image.jpg olarak kaydet //asenkron çalışmam lazım büyük veri kaydı yapıyorum.
            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                //Download url alıcaz.

                StorageReference newReference=firebaseStorage.getReference(imageName); //kaydettiğim görselin referansına eriştim.
                newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        String downloadUrl=uri.toString(); //uri stringe çevirdim verisini aldım.
                        String comment=binding.commentText.getText().toString(); //kullanıcının yazdığı yorumu aldım.

                        FirebaseUser user = firebaseAuth.getCurrentUser(); //hangi kullanıcı bu eklemeyi yapan onuda alıyorum.
                        String email=user.getEmail(); //kullanıcının mailini aldım.

                        HashMap<String,Object> postData=new HashMap<>(); //neden object verdim stringde olabilir int de date de olabilir vs.
                                //anahtar kelime string olacak değer herhangi bir şey olabilir.

                        postData.put("email",email); //verileri sırası ile aktardım
                        postData.put("comment",comment);  //verileri sırası ile aktardım
                        postData.put("downloadUrl",downloadUrl);  //verileri sırası ile aktardım
                        postData.put("date", FieldValue.serverTimestamp()); //sistem saatini çeken firebase özelliği
                        firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                Intent intent=new Intent(UploadActivity.this,FeedActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        });


                    }
                });



                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {       //hata durumunda kullanıcının anlayacağı şekilde mesaj ver.
                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage() , Toast.LENGTH_LONG).show();

                }
            });

        }

    }

    public void selectedImage(View view){

      if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
          //Eğer izin yoksa ne yapacağız.

                    //mantığını göstermeli miyiz?
          if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){

              Snackbar.make(view,"Permission needed for gallery!",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {

                      //ask permission //izin isteyeceğiz.
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                  }
              }).show(); //kullanıcı kapatana kadar snackbar mesajı göster.

          }else{
                //ask permission  //izin isteyeceğiz.
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

          }



      }else {
          Intent intentToGallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
          activityResultLauncher.launch(intentToGallery);  //galeriye gitme işlemi
      }
    }

    private void registerLauncher(){

                                                            //veriyi almaya gittik                              //veriyi aldık sonuç ne olacak callbacki
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if(result.getResultCode()==RESULT_OK){  //sonuç kodunun kontrolü

                    Intent intentFromResult=result.getData(); //veriyi aldık.
                    if(intentFromResult!=null){

                       imageData= intentFromResult.getData(); //resmin yerini aldık.
                       binding.imageView.setImageURI(imageData);


                   /*    try {   BITMAP OLUŞTURMA //
                            if(Build.VERSION.SDK_INT>=28){  //Versiyon kontrolü

                                ImageDecoder.Source source= ImageDecoder.createSource(UploadActivity.this.getContentResolver(),imageData);
                                selectedImage= ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);

                            }else{

                                selectedImage=MediaStore.Images.Media.getBitmap(UploadActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }


                       }catch (Exception e){
                           e.printStackTrace();
                       } */

                    }


                }

            }
        });
                                                        //izin isteme işlemi                        //izin istedikten sonra ne olacak
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

                if(result){             //izin verildi ise intent ile galeriye gitme işlemi
                    Intent intentToGallery=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }
                
                else{
                    Toast.makeText(UploadActivity.this, "Permission Needed!", Toast.LENGTH_LONG).show();
                }

            }
        });





    }


}