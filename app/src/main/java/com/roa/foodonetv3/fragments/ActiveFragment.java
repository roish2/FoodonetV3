package com.roa.foodonetv3.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.roa.foodonetv3.R;
import com.roa.foodonetv3.activities.MainDrawerActivity;
import com.roa.foodonetv3.adapters.PublicationsRecyclerAdapter;
import com.roa.foodonetv3.commonMethods.ReceiverConstants;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.services.FoodonetService;

import java.util.ArrayList;

public class ActiveFragment extends Fragment {
    private static final String TAG = "ActiveFragment";

    private PublicationsRecyclerAdapter adapter;
    private FoodonetReceiver receiver;

    public ActiveFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = new FoodonetReceiver();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_active, container, false);

        /** set the recycler view and adapter for all publications */
        RecyclerView activePubRecycler = (RecyclerView) v.findViewById(R.id.activePubRecycler);
        activePubRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PublicationsRecyclerAdapter(getContext());
        activePubRecycler.setAdapter(adapter);
        return v;
    }



    @Override
    public void onResume() {
        super.onResume();
        /** set the broadcast receiver for getting all publications from the server */
        IntentFilter filter =  new IntentFilter(ReceiverConstants.BROADCAST_FOODONET);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);
        /** temp request publications update from the server on fragment resume */
        Intent intent = new Intent(getContext(), FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_GET_PUBLICATIONS_EXCEPT_USER);
        getContext().startService(intent);
        /** show that the list is being updated */
        if(getView()!= null){
            Snackbar.make(getView(), R.string.updating,Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    @Override
    public void onStop() {
        super.onStop();
//        searchView.set
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = new SearchView(((MainDrawerActivity) getContext()).getSupportActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
    }

    public class FoodonetReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            /** receiver for publications got from the service, temporary, as we'll want to move it to the activity probably */
            if(intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1)== ReceiverConstants.ACTION_GET_PUBLICATIONS_EXCEPT_USER){
                if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                    // TODO: 27/11/2016 add logic if fails
                    Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                } else{
                    ArrayList<Publication> publications = intent.getParcelableArrayListExtra(Publication.PUBLICATION_KEY);
                    adapter.updatePublications(publications);
                }
            }
        }
    }
}

