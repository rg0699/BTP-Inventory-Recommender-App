package com.store.inventory.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.store.inventory.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneNumberActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    private static String code = "";
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        Intent intent = getIntent();
        final String string = (String) intent.getSerializableExtra("phone");
        TextView verify = (TextView) findViewById(R.id.verify);
        verify.setText("Verify " + string);
        TextView phone = (TextView) findViewById(R.id.phone);
        phone.setText(string);

        EditText text = (EditText) findViewById(R.id.code);
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 6) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(code, s.toString());
                    mAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            check(string);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(VerifyPhoneNumberActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        PhoneAuthProvider.getInstance().verifyPhoneNumber(string, 60, TimeUnit.SECONDS, this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        mAuth.signInWithCredential(credential).addOnCompleteListener(VerifyPhoneNumberActivity.this,
                                new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        check(string);
                                    }
                                });
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(VerifyPhoneNumberActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        code = s;
                    }
                });
    }

    private void check(final String string) {
        //showMessage(mCurrentUser.getUid());
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(mCurrentUser.getUid())) {
                    Intent intent = new Intent(VerifyPhoneNumberActivity.this, SetProfileData.class);
                    intent.putExtra("phone", string);
                    startActivity(intent);
                    finish();
                }
                else {
                    Intent intent = new Intent(VerifyPhoneNumberActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void wrongNumber(View view) {
        onBackPressed();
    }

    private void showMessage(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT)
                .show();
    }
}
