package com.store.inventory.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.Query;
import com.store.inventory.R;
import com.store.inventory.adapters.TabsAdapter;
import com.store.inventory.fragments.PlaceholderFragment;
import com.store.inventory.models.Product;
import com.store.inventory.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_SECTION_TITLE = "section_title";

    static ViewPager viewPager;
    static TabLayout tabLayout;

    public static ArrayList<String> categoriesList;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference mDatabase;
    private ValueEventListener listener;
    private StorageReference storageRef;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                addUserChangeListener(user);
            }
        };

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        categoriesList = new ArrayList<>();
        categoriesList.add(getResources().getString(R.string.item_1));
        categoriesList.add(getResources().getString(R.string.item_2));

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //setNavItems(navigationView);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        if (viewPager != null) {
            setupViewPager(viewPager);
            tabLayout.setupWithViewPager(viewPager);
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mCurrentUser == null){
                    startActivity(new Intent(getApplicationContext() , RegisterPhoneNumber.class));
                    finish();
                }
                else {
                    Intent i = new Intent(getApplicationContext() , EditActivity.class);
                    i.putExtra("what" , "add");
                    i.putExtra("supplierId", mCurrentUser.getUid());
                    startActivity(i);
                }
            }
        });

    }

    private void setNavItems(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        int i=0;
        Menu submenu = menu.addSubMenu("Categories");
        for(String cat : categoriesList){
            submenu.add(cat);
        }
        navigationView.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_search) {

            return true;
        }else if (id == R.id.action_notifications) {

            return true;
        }else {

        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {

        TabsAdapter adapter = new TabsAdapter(getSupportFragmentManager());
        int tmp = 1;
        for(String cat : categoriesList){

            PlaceholderFragment fragment = PlaceholderFragment.newInstance(cat, tmp);
            adapter.addFragment(fragment, cat);
            tmp++;

        }
        viewPager.setAdapter(adapter);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_item1) {
            viewPager.setCurrentItem(0);
        } else if (id == R.id.nav_item2) {
            viewPager.setCurrentItem(1);
        }else if(id == R.id.nav_share_app){
            shareApplication();
        }
        else if(id == R.id.nav_delete_account){
            showDeleteConfirmationDialog();
        }
        else if(id == R.id.nav_sign_out){
            signOut();
        }
        else if(id == R.id.nav_my_inventory){
            Intent i = new Intent(getApplicationContext(),MyInventoryActivity.class);
            i.putExtra("type", item.getTitle().toString());
            startActivity(i);
        }
        else if(id == R.id.nav_my_dashboard){
            Intent i = new Intent(getApplicationContext(),MyDashboardActivity.class);
            startActivity(i);
        }
        else if(id == R.id.nav_contact_us){
            Intent i = new Intent(getApplicationContext(),ContactUs.class);
            startActivity(i);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void addUserChangeListener(FirebaseUser user) {
        // User data change listener

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        final View headerView = navigationView.getHeaderView(0);
        final TextView navUsername = headerView.findViewById(R.id.nav_user_name);
        final TextView navUserPhone = headerView.findViewById(R.id.nav_user_phone);
        final ImageView navUserPhoto = headerView.findViewById(R.id.nav_user_photo);

        if (user == null) {
            navUsername.setText("LogIn");
            navUsername.setTextSize(18);
            navUserPhone.setVisibility(View.INVISIBLE);
            navigationView.getMenu().findItem(R.id.nav_delete_account).setVisible(false);
            headerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), RegisterPhoneNumber.class));
                    finish();
                }
            });
        }
        else {
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user == null) {
                        Toast.makeText(
                                getApplicationContext(),
                                "User data is null!",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                    else {
                        navUsername.setText(user.name);
                        navUserPhone.setText(user.phone);
                        if(user.photo!=null){
                            Glide.with(getApplicationContext()).load(user.photo).into(navUserPhoto);
                        }
                        if(user.type.equals("buyer")){
                            fab.setVisibility(View.INVISIBLE);
                            navigationView.getMenu().findItem(R.id.nav_my_inventory).setTitle("My WishList");
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Failed to read user!"
                                    + " Please try again later",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            };
            mDatabase.child("users").child(user.getUid()).addValueEventListener(listener);
        }
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.menu_delete_account);
        builder.setMessage(R.string.remove_account);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                reAuth();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void reAuth(){

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential("","");
        mCurrentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    deleteAccount();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Something went wrong. Try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteAccount() {

        if (mCurrentUser != null) {
            String userId = mCurrentUser.getUid();
            deleteUserData(userId);
            mCurrentUser.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Your Account is deleted:(", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void deleteUserData(String userId) {
        mDatabase.child("users").child(userId).removeValue();
        storageRef.child("profilePictures/" + userId + ".jpg").delete();
        Query query = mDatabase.child("products").orderByChild("supplier_id").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //product_list.clear();
                for (DataSnapshot productSnap: dataSnapshot.getChildren()) {
                    productSnap.getRef().removeValue();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
        Toast.makeText(
                getApplicationContext(),
                "Logged Out",
                Toast.LENGTH_SHORT)
                .show();
    }

    private void shareApplication() {

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "https://drive.google.com/file/d/1jdq61sX9_5-_C-YlrMKgIJpAge41AG9D/view?usp=sharing";
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App link:");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share app via"));
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
        //addUserChangeListener(mCurrentUser);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
        if (listener != null){
            mDatabase.child("users").child(mCurrentUser.getUid()).removeEventListener(listener);
        }
    }

    private void showMessage(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT)
                .show();
    }

}