package com.store.inventory.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.store.inventory.R;

public class ContactUs extends AppCompatActivity {

    private Button submit;
    private EditText name, email, phone, message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.contact_us);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Send Feedback");
        submit = findViewById(R.id.submit);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        message = findViewById(R.id.message);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(name.getText()) || TextUtils.isEmpty(email.getText())
                        || TextUtils.isEmpty(message.getText())){
                    showMessage("Please Fill all the Fields!");
                }
                else {
                    Intent Email = new Intent(Intent.ACTION_SEND);
                    Email.setType("text/email");
                    Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "rg260699@gmail.com" });
                    Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback: "
                            + name.getText().toString() + " " + email.getText().toString());
                    Email.putExtra(Intent.EXTRA_TEXT, message.getText().toString());
                    startActivity(Intent.createChooser(Email, "Send Feedback:"));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMessage(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT)
                .show();
    }
}
