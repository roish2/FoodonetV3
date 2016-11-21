package com.roa.foodonetv3.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.model.Publication;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class AddPublicationService extends IntentService {
    private static final String TAG = "AddPublicationService";

    public AddPublicationService() {
        super("AddPublicationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d("AddPublicationService","entered service");
            String jsonPublication = intent.getStringExtra(Publication.PUBLICATION_KEY);

            //todo use localID to later save the correct server id to the database
            long publicationLocalID = intent.getLongExtra(Publication.PUBLICATION_UNIQUE_ID_KEY,-1);
            HttpsURLConnection connection = null;
            BufferedReader reader = null;
            StringBuilder urlAddressBuilder = new StringBuilder(getResources().getString(R.string.foodonet_server));
            if(publicationLocalID<0){
                /** if the publication id is negative - it is a new publication to add */
                urlAddressBuilder.append(getResources().getString(R.string.foodonet_publications));
                urlAddressBuilder.append(getResources().getString(R.string._json));
            } else{
                /** if the publication id is positive - it is an edit of an existing publication */
                urlAddressBuilder.append(getResources().getString(R.string.foodonet_publications));
                urlAddressBuilder.append(String.format(Locale.US,"/%1$d",publicationLocalID));
                urlAddressBuilder.append(getResources().getString(R.string._json));
            }

            Log.d(TAG,"address: " + urlAddressBuilder.toString());
            try {
                URL url = new URL(urlAddressBuilder.toString());
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.addRequestProperty("Accept","application/json");
                connection.addRequestProperty("Content-Type","application/json");
                connection.setDoOutput(true);
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"utf-8"));
                writer.write(jsonPublication);
                writer.flush();
                writer.close();
                os.close();
                StringBuilder builder = new StringBuilder();
                if(connection.getResponseCode()!= HttpsURLConnection.HTTP_OK){
                    //do something
                }
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while((line = reader.readLine())!= null){
                    builder.append(line);
                }
                //todo save the response server id to the database, replacing the local one and / or update the version number of an edit


                Log.d("SERVER RESPONSE", builder.toString());
            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
            }
            finally {
                if(connection!= null){
                    connection.disconnect();
                }
                if(reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG,e.getMessage());
                    }
                }
            }
        }
    }
}
