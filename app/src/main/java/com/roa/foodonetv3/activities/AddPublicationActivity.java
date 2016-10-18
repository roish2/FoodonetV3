package com.roa.foodonetv3.activities;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.services.AddPublicationService;

public class AddPublicationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private EditText editTextTitleAddPublication,editTextLocationAddPublication,editTextPriceAddPublication,editTextShareWithAddPublication,editTextDetailsAddPublication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_publication);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        editTextTitleAddPublication = (EditText) findViewById(R.id.editTextTitleAddPublication);
        editTextLocationAddPublication = (EditText) findViewById(R.id.editTextLocationAddPublication);
        editTextShareWithAddPublication = (EditText) findViewById(R.id.editTextShareWithAddPublication);
        editTextDetailsAddPublication = (EditText) findViewById(R.id.editTextDetailsAddPublication);
        editTextPriceAddPublication = (EditText) findViewById(R.id.editTextPriceAddPublication);

        /** temporary button to add a test publication to the server */
        findViewById(R.id.buttonTestAdd).setOnClickListener(this);
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
    public boolean onNavigationItemSelected(MenuItem item) {
        /** handle the navigation actions in the common methods class */
        CommonMethods.navigationItemSelectedAction(this,item.getItemId());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        // temp button until we get a button in the toolbar !!!!
        // most of the information is hard coded right now, I'm just testing the layout and activation of the add function to the server
        String title = editTextTitleAddPublication.getText().toString();
        String location = editTextLocationAddPublication.getText().toString();
        String priceS = editTextPriceAddPublication.getText().toString();
        String shareWith = editTextShareWithAddPublication.getText().toString();
        String details = editTextDetailsAddPublication.getText().toString();
        if(title.equals("") || location.equals("") || shareWith.equals("")){
            Toast.makeText(this, R.string.post_please_enter_all_fields, Toast.LENGTH_SHORT).show();
        } else{
            double price;
            if(priceS.equals("")){
                price = 0.0;
            } else{
                try {
                    price = Double.parseDouble(priceS);
                    }
                catch (NumberFormatException e){
                    Log.e("AddPublicationActivity",e.getMessage());
                    Toast.makeText(this, R.string.post_toast_please_enter_a_price_in_numbers, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Publication publication = new Publication(-1,-1,title,details,location,(short)2,32.0907185,34.873032,"1476351454.0","1479029854.0","0500000000",
                    true,"8360c4c4be9e1398","",16,0,"Alon",price,"");
            Intent i = new Intent(this, AddPublicationService.class);
            i.putExtra(Publication.PUBLICATION_KEY,Publication.getPublicationJson(publication).toString());
            startService(i);
        }
    }
}