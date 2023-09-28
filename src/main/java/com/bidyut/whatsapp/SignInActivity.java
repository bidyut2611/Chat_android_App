package com.bidyut.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bidyut.whatsapp.Models.Users;
import com.bidyut.whatsapp.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;

public class SignInActivity extends AppCompatActivity {
ActivitySignInBinding binding;
ProgressDialog progressDialog;
FirebaseAuth auth;
GoogleSignInClient mGoogleSignInClient;
FirebaseDatabase database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();

        database =FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Login to your Account");


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
         .requestIdToken(getString(R.string.app_name))
         .requestEmail()
         .build();
        mGoogleSignInClient=GoogleSignIn.getClient(this,gso);


        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();




                auth.signInWithEmailAndPassword(binding.etEmail.getText().toString(), binding.etPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        binding.tvClickSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });


        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });




        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);

        }
    }

   int RC_SIGN_IN=65;
   private void signIn(){
        Intent signInIntent=mGoogleSignInClient.getSignInIntent();
       startActivityForResult(signInIntent,RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
       super.onActivityResult(requestCode,resultCode,data);

        if (requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);
           try {
                GoogleSignInAccount account=task.getResult(ApiException.class);
               Log.d("TAG","firebaseAuthWithGoogle:" + account.getId());
               firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
               Log.w("TAG","Google sign in failed",e);
            }
        }
       }
private void firebaseAuthWithGoogle(String idToken){
    AuthCredential credential= GoogleAuthProvider.getCredential(idToken,null);
    auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
       @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()){
                Log.d("TAG","signInWithCredential:success");
              FirebaseUser user=auth.getCurrentUser();



Users users=new Users();
    users.setUserId(user.getUid());
    users.setUserName(user.getDisplayName());
    users.setProfilepic(user.getPhotoUrl().toString());
    database.getReference().child("Users").child(user.getUid()).setValue(users);




                Intent intent=new Intent(SignInActivity.this,MainActivity.class);
                startActivity(intent);
                Toast.makeText(SignInActivity.this,"sign in with google",Toast.LENGTH_SHORT).show();

            }else {
                Log.w("TAG","signInWithCredential:failed",task.getException());
               Toast.makeText(SignInActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
               Snackbar.make(binding.getRoot(),"Autentication failed",Snackbar.LENGTH_SHORT).show();
           }
       }
   });


}



}