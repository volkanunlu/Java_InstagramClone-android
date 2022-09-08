package com.volkanunlu.instagramclonejava.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.volkanunlu.instagramclonejava.R;
import com.volkanunlu.instagramclonejava.adapter.PostAdapter;
import com.volkanunlu.instagramclonejava.databinding.ActivityFeedBinding;
import com.volkanunlu.instagramclonejava.model.Post;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {

    private FirebaseAuth auth; //kullanıcı verilerine erişmem gerek.
    private FirebaseFirestore firebaseFirestore;
    ArrayList<Post> postArrayList; //verilerimi saklayacağım recyclerView'e düzgünce aktarmak adına.
    private ActivityFeedBinding binding;
    PostAdapter postAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityFeedBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);  //binding işlemi tamam.
        auth=FirebaseAuth.getInstance(); //kullanıcı bilgilerimi aldım.
        firebaseFirestore=FirebaseFirestore.getInstance();
        getData();
        postArrayList=new ArrayList<>(); //boş bir şekilde verdim.
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this)); //bu ekranda gösterim yapacağım için bu viewde verdim.
        postAdapter=new PostAdapter(postArrayList);  //adaptera veriyi verdim
        binding.recyclerView.setAdapter(postAdapter); //recycler view'e de adapterı verdim.

    }

    private void getData(){

        //diğer bir yol
        //DocumentReference documentReference=firebaseFirestore.collection("Posts").document("deneme");
       // CollectionReference collectionReference=firebaseFirestore.collection("Posts");

        //firebase kendi içerisinde bize filtreleme olanakları sağlar, whereGreaterThan (numara vardır , 5 den büyükleri getir gibi gibi),
        // whereArrayContrains(dizisi şunları içerenleri getir
                                                        // filtreledik.  //en son tarih çıksın en başta
        firebaseFirestore.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){ //hata mesajı boş değilse kullanıcıya göstericez

                    Toast.makeText(FeedActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                }

                    if(value!=null) //Gelen bir değer var ise
                    {
                       for(DocumentSnapshot snapshot: value.getDocuments()){
                           //  value.getDocuments() //bana bir dizi,liste veriyor içerisinde dökümanlarım var.

                           Map<String,Object> data=snapshot.getData();

                           //casting işlemi string tanımladım object alıyorum emin nisin sorgusu
                           String userEmail=(String) data.get("useremail");
                           String comment= (String) data.get("comment");
                           String downloadUrl=(String) data.get("downloadUrl");

                           Post post=new Post(userEmail,comment,downloadUrl);

                           postArrayList.add(post); //verileri dizime aktardım, sonra recyclerView'e aktarmam lazım.


                       }

                       postAdapter.notifyDataSetChanged(); //haber ver recyccler view'e yeni veri geldi diyor.

                    }


            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) { //activitye bağlama işlemi.

        MenuInflater menuInflater=getMenuInflater(); //xml ve kodu bağlıyor.
        menuInflater.inflate(R.menu.option_menu,menu); //menüyü bağladık.

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //item seçilince ne olacak.

        if(item.getItemId()==R.id.add_post){
            //Upload activity tarafına gidecek.

            Intent intentUpload=new Intent(FeedActivity.this,UploadActivity.class);
            startActivity(intentUpload);
            //finish vermedim çünkü kullanıcı upload etmekten vazgeçebilir.
        }

        else if(item.getItemId()==R.id.signout){

            auth.signOut(); //çıkış yap.

            Intent intentToMain=new Intent(FeedActivity.this,MainActivity.class);
            startActivity(intentToMain);
            finish();  //finish verdim çünkü kullanıcı çıkış yaptı buraya geri dönememesi lazım.



        }



        return super.onOptionsItemSelected(item);



    }
}