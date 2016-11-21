package com.roa.foodonetv3.activities;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.fragments.AddEditPublicationFragment;
import com.roa.foodonetv3.fragments.PublicationDetailFragment;
import com.roa.foodonetv3.model.Publication;

public class PublicationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "PublicationActivity";
    private FragmentManager fragmentManager;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publication);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        intent = getIntent();
        int openFragType = intent.getIntExtra(MainDrawerActivity.ACTION_OPEN_PUBLICATION,MainDrawerActivity.OPEN_ADD_PUBLICATION);
        fragmentManager = getSupportFragmentManager();
        if(savedInstanceState==null){
            openNewPublicationFrag(openFragType);
        }
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

    private void openNewPublicationFrag(int type){
        Publication publication;
        Bundle bundle;
        switch (type){
            case MainDrawerActivity.OPEN_ADD_PUBLICATION:
                AddEditPublicationFragment addPublicationFragment = new AddEditPublicationFragment();
                bundle = new Bundle();
                bundle.putInt(AddEditPublicationFragment.TAG,AddEditPublicationFragment.TYPE_NEW_PUBLICATION);
                addPublicationFragment.setArguments(bundle);
                fragmentManager.beginTransaction().add(R.id.container_publication, addPublicationFragment, "addEditPublicationFrag").commit();
//                fragmentManager.beginTransaction().add(R.id.container_publication, new AddEditPublicationFragment(), "addEditPublicationFrag").commit();
                break;
            case MainDrawerActivity.OPEN_EDIT_PUBLICATION:
                publication = getIntent().getParcelableExtra(Publication.PUBLICATION_KEY);
                AddEditPublicationFragment editPublicationFragment = new AddEditPublicationFragment();
                bundle = new Bundle();
                bundle.putInt(AddEditPublicationFragment.TAG,AddEditPublicationFragment.TYPE_EDIT_PUBLICATION);
                bundle.putParcelable(Publication.PUBLICATION_KEY,publication);
                editPublicationFragment.setArguments(bundle);
                fragmentManager.beginTransaction().add(R.id.container_publication, editPublicationFragment, "addEditPublicationFrag").commit();
                break;
            case MainDrawerActivity.OPEN_PUBLICATION_DETAIL:
                publication = getIntent().getParcelableExtra(Publication.PUBLICATION_KEY);
                PublicationDetailFragment publicationDetailFragment = new PublicationDetailFragment();
                bundle = new Bundle();
                bundle.putParcelable(Publication.PUBLICATION_KEY,publication);
                publicationDetailFragment.setArguments(bundle);
                fragmentManager.beginTransaction().add(R.id.container_publication, publicationDetailFragment, "publicationDetailFrag").commit();
                break;
        }
    }
}
