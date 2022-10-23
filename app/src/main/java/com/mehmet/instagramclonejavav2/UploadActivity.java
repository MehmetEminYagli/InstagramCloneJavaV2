package com.mehmet.instagramclonejavav2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
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
import com.mehmet.instagramclonejavav2.databinding.ActivityUploadBinding;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    private  ActivityUploadBinding binding;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    Uri imageData;
    //Bitmap selectedBitmap;
    //launcher'ı kullanabilmek için önce burada çağırmamız lazım
    ActivityResultLauncher<Intent> activityResultLauncher; //neden intent diyoruz bu arkadaş galeriye gidip veri alma işini yaptığı için intent ile galeri sayfasını açıcaz
    //her olay için launcher oluştururuz
    ActivityResultLauncher<String> permissionLauncher; //buradada izin isteme sayfası için launcher oluşturucaz true ise git false ise şunu bunu yap diye

    //sonra bu launcher'ları initalieze etmemiz gerekiyor bunu unutmayalım ama bu olayın kodları biraz fazla olduğu için farklı bir method oluşturup orada
    //bu kodları yazalım sonra bu methodu oncreate altında çağıralım ki kod fazlalığı ve karmaşıklığı olmasın


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        //registerlauncher'ı oncreate altında çağırmaz isek launcher'lar çalışmaz
        registerLauncher();
        //storege'i initalieze edelim
        firebaseStorage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // = oluşturduğumuz obje  firebaseStorage den çekicez verileri depodan yani onun için bunu Referance edicez
        storageReference = firebaseStorage.getReference();

    }

    public void UploadButton(View view){

        if (imageData != null){
            //referans sistemi ile resimlerimizi kayıt edicez
            //referans denilen şey nereye storage 'de nereye ne kayıt etmemizi trackini tutan yani sırasını tutan bir objedir
            //bu objeniyi kullanabilmek için storagereferance 'i projeye eklememiz geerekiyor
            //firebaseStorage'de klasör oluşturmak için --> storageReference.child() <-- komutunu kullanırız sonrasında --> .putFile(koyulacakdosya) ilede veriyi depoda o klasörün içine kayıt ederiz
            // yine sonrasında nokta ile bir listener oluşturucaz başarılı ise şunu başarısız ise şunu yap dememiz gerekiyor
            //hadi yapalım let's go
            //resimleri yada verileri kayıt ederken onlara üniversal Unique bir isim vermemiz gerekiyor eğer aynı isimden birden fazla olursa en son kayıt edilen veri depoda durur öncekiler silinir
            //bunun için her veriye unique isim vermemiz gerekiyor :D

            //universal unique id
            //javada zaten boyle bir sınıf varmış
            UUID uuid = UUID.randomUUID(); //bu kod satırı bize uydurma ama benzersiz bir isim vericek
            //sonrasında benzersiz ismi dataya eklememiz gerekiyor dicez ki böyle kaydet
            String UniqueimageName = "images/"+ uuid +".jpg";

            storageReference.child(UniqueimageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //başarılı ise ne yapıcaz
                    //download url 'yi alıcaz sonra bu url'yi kullanarak başka kullanıcılarada göstericez
                    //benzersiz ismide verdiğimize göre şimdi verilerin url 'sini alarak kullanıcalara göstericez
                    //bunun için yine bir referance eklicez

                    StorageReference newReference = firebaseStorage.getReference(UniqueimageName);
                    //verinin ismini aldık şimfi url 'sini alıcaz
                    newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //BURAYA VERİ TABANINA KAYIT ETMEK İSTEDİĞİMİZ VERİLERİ YAZICAZ


                            //bu listener da bize uri veriyor biz bu uri ile verileri göstericez yazıcaz
                            //bundan sonrasında veriden almak istediklerimizi yazıyoruz
                            //url sini Uri kullnarak string yapıyourz
                            String DownloadUrl = uri.toString();

                            //sonra yorumu alıyoruz
                            String comment = binding.CommentText.getText().toString();

                            //birde kullanıcının kendisini alıcaz göstericez
                            FirebaseUser user = auth.getCurrentUser(); //ile kullanıcın adını aldık
                            //birde emailini alalım
                            String email = user.getEmail();

                            //şimdi anahtar kelimeleri ve değerleri HASHMAP kullanarak firestore 'a kopyalayabileceğiz

                                    //<anahtar kelime string olucak , değer herhangi bir şey olabilir>
                            HashMap<String , Object> postData = new HashMap<>();
                            postData.put("userEmail", email);
                            postData.put("DownloadURL",DownloadUrl);
                            postData.put("Comment" ,comment);
                            //tarihi firebase 'i kullanarak alıcaz ve kayıt edicez FieldValue.serverTimestamp() kodu ile o anki server saatini alıcaz yani date'i alıcaz
                            postData.put("Date", FieldValue.serverTimestamp());

                            //hashmap ile anahtar kelimeleri ve değerleri birleştirdim şimdi firebase veri tabanına kayıt edicem
                            firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    //kayıt yapıldı ise post sayfasını kapatabiliriz yani bu sayfayı kapatıcaz anasayfaya geçicez

                                    Intent intent = new Intent(UploadActivity.this,FeedActivity.class);
                                    //flag ekliyoruz
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //komutu ile herşeyi kapatıcak ki arka planda gereksiz yere çalışan bir sayfa olmasın
                                    startActivity(intent);



                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                //başarısız ise bir toast göstericez
                                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                                }
                            });


                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //resim yükleme başarısız ise toast mesajı gösterizcez
                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
            });


        }


    }

    public  void  SelectImage(View view){

        //izin varmı onu kontrol edicez resim seçme kısmında eğer izin var ise galeriye atıcak bizi eğer yok ise izin isticek bizden

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
    //eğer izin yok ise ne yapıcağımızı yazıyoruz
            //neden izin vermesi gerektiğini söylememiz gerekiyor kullanıcıya
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                // eğer bu if komutu true ise snackbar ile neden izin istediğimiz söylicez

                                                            //Snackbar.LENGTH_INDEFINITE => belirsiz bir süre göster kullanıcı ok yada cancel tuşuna basana kadar göster yapıcaz
                Snackbar.make(view,"izin versene oglim",Snackbar.LENGTH_INDEFINITE).setAction("izin ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //burada da izni isticez
                        //izin launcher'ını yazdık burada çağırabiliriz tekrar kod yazmak yerine
                        //                      (hangi izni istediğimiz yazmak yeterlidir)
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }else{
                //true değilde false döndürürse yine izni isticez
                //aynı izin isteme launcher'ını burayada çağıracağız
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }else{
            //en genelde izin verildiyse zaten direk galeriye gidicez
            //galeriye nasıl gidicez komutları burada
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //startactivity yerine bir launcher yazıcaz
            //launcherı oluşturduk ekleyelim
            activityResultLauncher.launch(intentToGallery);
        }
    }

    private  void  registerLauncher(){
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult() ,  new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK){ // sonuç okeyse yani herşey tamam ise izinler verildiyse ne olucağını yazıyoruz buraya

                    Intent intentFromResult = result.getData(); //ile resmin datasını aldık
                    //şimdi oluşturduğumuz intentin içi boş mu değilmi onu kontrol ediyoruz yani kullanıcı resim seçtimi seçmedimi onu kontrol edicez
                    if (intentFromResult != null){
                        //eğer intentin içi boş değilse yani dolu ise bir şey seçildi ise ne olucağını buraya yazıyoruz

                        imageData = intentFromResult.getData(); //şimdi bu getdata bize URİ diye bir veri döndürüyor bunu kullanabilmek için en üstte bu URİ 'ı bir tanıtmamız gerekiyor
                        //imagedata diye bir uri değişkeni oluşturdum bunuda her yerde kullanabilmek için genel olarak aldım

                        //şimdide resmi kullanıcıya gösterme kısmını halledelim
                        binding.imageView.setImageURI(imageData); //bitmap ilede yapabiliriz bunu bu aklımızın bir kenarında bulunsun ilerde yaparsın belki
                        //sadece yukarıdaki satır bu proje için yeterlidir bitmap'e çevirmeye gerek yok

                                                                                                                                                                                                                             //sonra try catch ile bir sorun varmı yokmu diye kontrol ederek işlemleri yapmamız lazım


                                                                                                                                                                                                                            /*
                                                                                                                                                                                                                            try {
                                                                                                                                                                                                                                //şimdi skd ile sınırlarımız vardı onu hatırlayalım sdk 28 üstü için farklı kod altı için farklı kod yazıyorduk
                                                                                                                                                                                                                                if (Build.VERSION.SDK_INT >=28){ //28 ve üstü için gerekli kodlar
                                                                                                                                                                                                                                        //resim için bir kaynak oluşturuyoruz
                                                                                                                                                                                                                                    ImageDecoder.Source source = ImageDecoder.createSource(UploadActivity.this.getContentResolver(),imageData);
                                                                                                                                                                                                                                    //bu kaynagi kullanarak herhangi bir bitmap oluşturabiliriz
                                                                                                                                                                                                                                    selectedBitmap = ImageDecoder.decodeBitmap(source);
                                                                                                                                                                                                                                    binding.imageView.setImageBitmap(selectedBitmap);
                                                                                                                                                                                                                                }else //28 altı için gerekli kodlar{
                                                                                                                                                                                                                                    selectedBitmap = MediaStore.Images.Media.getBitmap(UploadActivity.this.getContentResolver(),imageData);
                                                                                                                                                                                                                                    binding.imageView.setImageBitmap(selectedBitmap);
                                                                                                                                                                                                                                }
                                                                                                                                                                                                                            }catch (Exception e){
                                                                                                                                                                                                                                e.printStackTrace();
                                                                                                                                                                                                                                //bir sorun olursa anlayabileceğimiz bir dilden bize söyle
                                                                                                                                                                                                                            }buda bitmap şekli */
                    }
                }
            }
        });

    permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result){
                //izin verilirse ne olucak yine intent ile galeriye gidicez yani

                                    //(NASIL GİDİYORUZ , NEREYE GİDİYORUZ)
                Intent intentToGallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //sonrasında oluşturduğumuz activityresultlauncherı çağırırız
                activityResultLauncher.launch(intentToGallery);

            }else {
                //izin vermez ise ne olacağını buraya yazıyoruz
                Toast.makeText(UploadActivity.this,"izin verirmisin lütfen",Toast.LENGTH_LONG).show();
            }
        }
    });
    }
}