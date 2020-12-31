package com.store.inventory.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.store.inventory.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

public class RegisterPhoneNumber extends AppCompatActivity {

    private String[] locales;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone_number);

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            FirebaseDatabase.getInstance().getReference("users").keepSynced(true);
            FirebaseDatabase.getInstance().getReference("inviteLink").keepSynced(true);
        }
        catch (Exception ex) {}

        //FirebaseDatabase.getInstance().getReference("inviteLink").setValue("abc");

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        createCountryDropDownMenu();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){}
                else finish();
        }
    }

    private void createCountryDropDownMenu() {
        ArrayList<String> countries= getCountryNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, countries);

        Spinner countrySpinner = (Spinner) findViewById(R.id.country);
        countrySpinner.setAdapter(adapter);
        countrySpinner.setSelection(countries.indexOf("India"));

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //String CountryID= locales[position];
                //showMessage(CountryID);
                String CountryID = "IN";
                String[] rl = getResources().getStringArray(R.array.CountryCodes);
                for(String x : rl){
                    String[] g = x.split(",");
                    if(g[1].trim().equals(CountryID.trim())){
                        TextView textView = (TextView) findViewById(R.id.countryCode);
                        textView.setText("+" + g[0]);
                        return;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void nextButton(View view) {
        TextView editText1 = (TextView) findViewById(R.id.countryCode);
        EditText editText2 = (EditText) findViewById(R.id.phone);

        Intent intent = new Intent(this, VerifyPhoneNumberActivity.class);
        intent.putExtra("phone", editText1.getText().toString() + editText2.getText().toString());
        startActivity(intent);
    }

    public ArrayList<String> getCountryNames() {

        locales = Locale.getISOCountries();
        ArrayList<String> countries = new ArrayList<>();

        for (String countryCode : locales) {

            Locale obj = new Locale("", countryCode);
            countries.add(obj.getDisplayCountry());
        }
        return countries;
    }

    private void showMessage(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT)
                .show();
    }
}
