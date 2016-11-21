package com.roa.foodonetv3.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.activities.MainDrawerActivity;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.services.AddPublicationService;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.IOException;

public class AddEditPublicationFragment extends Fragment implements View.OnClickListener{
    public static final String TAG = "AddEditPublicationFrag";
    private static final int INTENT_TAKE_PICTURE = 1;
    private static final int REQUEST_PLACE_PICKER = 10;
    public static final int TYPE_NEW_PUBLICATION = 1;
    public static final int TYPE_EDIT_PUBLICATION = 2;
    private EditText editTextTitleAddPublication,editTextPriceAddPublication,editTextShareWithAddPublication,editTextDetailsAddPublication;
    private TextView textLocationAddPublication;
    private long endingDate;
    private String mCurrentPhotoPath;
    private ImageView imagePictureAddPublication;
    private LatLng latlng;
    private Publication publication;
    private boolean isEdit;


    /**The TransferUtility is the primary class for managing transfer to S3*/
    private TransferUtility transferUtility;

    public AddEditPublicationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** instantiate the transfer utility for the s3*/
        transferUtility = CommonMethods.getTransferUtility(getContext());
        /** local image path that will be used for saving locally and uploading the file name to the server*/
        mCurrentPhotoPath = "";

        isEdit = getArguments().getInt(TAG, TYPE_NEW_PUBLICATION) != TYPE_NEW_PUBLICATION;
        if(isEdit){
            /** if there's a publication in the intent - it is an edit of an existing publication */
             if(savedInstanceState == null){
                /** also check if there's a savedInstanceState, if there isn't - load the publication, if there is - load from savedInstanceState */
                publication = getArguments().getParcelable(Publication.PUBLICATION_KEY);
                 latlng = new LatLng(publication.getLat(),publication.getLng());
             } else{
                // TODO: 19/11/2016 add savedInstanceState reader
             }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_edit_publication, container, false);

        editTextTitleAddPublication = (EditText) v.findViewById(R.id.editTextTitleAddPublication);
        textLocationAddPublication = (TextView) v.findViewById(R.id.textLocationAddPublication);
        textLocationAddPublication.setOnClickListener(this);
        editTextShareWithAddPublication = (EditText) v.findViewById(R.id.editTextShareWithAddPublication);
        editTextDetailsAddPublication = (EditText) v.findViewById(R.id.editTextDetailsAddPublication);
        editTextPriceAddPublication = (EditText) v.findViewById(R.id.editTextPriceAddPublication);
        v.findViewById(R.id.imageTakePictureAddPublication).setOnClickListener(this);
        imagePictureAddPublication = (ImageView) v.findViewById(R.id.imagePictureAddPublication);

        /** temporary button to add a test publication to the server */
        v.findViewById(R.id.buttonTestAdd).setOnClickListener(this);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mCurrentPhotoPath == null){
            /** check if there is no path yet, if not, load the default image */
            // TODO: 13/11/2016 fix image size error
            Picasso.with(getContext()).load(R.drawable.foodonet_image)
    //                .resize(imagePictureAddPublication.getWidth(),imagePictureAddPublication.getHeight())
    //                .centerCrop()
                    .into(imagePictureAddPublication);
        }
        if(isEdit){
            loadPublicationIntoViews();
        }
    }

    public void loadPublicationIntoViews(){
        // TODO: 19/11/2016 test
        editTextTitleAddPublication.setText(publication.getTitle());
        textLocationAddPublication.setText(publication.getAddress());
        editTextShareWithAddPublication.setText("currently not working");
        editTextDetailsAddPublication.setText(publication.getSubtitle());
        editTextPriceAddPublication.setText(String.valueOf(publication.getPrice()));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonTestAdd:
                /** button for uploading the publication to the server, if an image was taken,
                 *  start uploading to the s3 server as well, currently no listener for s3 finished upload*/
                uploadPublicationToServer();
                if(!mCurrentPhotoPath.equals("")){
                    beginS3Upload("file:"+mCurrentPhotoPath);
                } else{
                    Toast.makeText(getContext(), "no photo path", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.imageTakePictureAddPublication:
                /** starts the image taking intent through the default app*/
                dispatchTakePictureIntent();
                break;
            case R.id.textLocationAddPublication:
                /** start the google places autocomplete widget */
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                    Intent intent = intentBuilder.build(getActivity());
//                    Intent intent =
//                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
//                                    .build(getActivity());
                    // Start the Intent by requesting a result, identified by a request code.
                    startActivityForResult(intent, REQUEST_PLACE_PICKER);

                    // Hide the pick option in the UI to prevent users from starting the picker
                    // multiple times.
//                    showPickAction(false);

                } catch (GooglePlayServicesRepairableException e) {
                    GooglePlayServicesUtil
                            .getErrorDialog(e.getConnectionStatusCode(), getActivity(), 0);
                } catch (GooglePlayServicesNotAvailableException e) {
                    Toast.makeText(getActivity(), "Google Play Services is not available.",
                            Toast.LENGTH_LONG)
                            .show();
                }

                // END_INCLUDE(intent)
                break;
        }
    }

    private void dispatchTakePictureIntent() {
        /** starts the image taking intent through the default app*/
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = CommonMethods.createImageFile(getContext());
                mCurrentPhotoPath = photoFile.getPath();
                Log.d(TAG,"photo path: " + mCurrentPhotoPath);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG,ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.roa.foodonetv3.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, INTENT_TAKE_PICTURE);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode){
                case INTENT_TAKE_PICTURE:
                    /** an image was successfully taken, since we have the path already,
                     *  we'll run the editOverwriteImage method that scales down, shapes and overwrites the images in the path
                     *  returns true if successful*/
                    if(CommonMethods.editOverwriteImage(getContext(),mCurrentPhotoPath)){
                        /** let picasso handle the caching and scaling to the imageView */
                        Picasso.with(getContext())
                                .load("file:"+mCurrentPhotoPath)
                                .resize(imagePictureAddPublication.getWidth(),imagePictureAddPublication.getHeight())
                                .centerCrop()
                                .into(imagePictureAddPublication);
                        //                    imagePictureAddPublication.setImageBitmap(scaledBitmap);
                    }
                    break;
                case REQUEST_PLACE_PICKER:
                    /** a place was successfully received from the place autocomplete sdk, including the address, and latlng */
                    final Place place = PlacePicker.getPlace(getContext(),data);
                    final CharSequence address = place.getAddress();
                    latlng = place.getLatLng();
                    textLocationAddPublication.setText(address);
                    Toast.makeText(getContext(), "latlng: "+place.getLatLng().toString(), Toast.LENGTH_SHORT).show();
                        break;
            }
        }
    }


    public void uploadPublicationToServer() {
        /** upload the publication to the foodonet server */
        FirebaseUser user = MainDrawerActivity.getFireBaseUser();
        String title = editTextTitleAddPublication.getText().toString();
        String location = textLocationAddPublication.getText().toString();
        String priceS = editTextPriceAddPublication.getText().toString();
        String shareWith = editTextShareWithAddPublication.getText().toString();
        String details = editTextDetailsAddPublication.getText().toString();
        /** currently starting time is now */
        long startingDate = System.currentTimeMillis();
        if(endingDate == 0){
            /** default ending date is 2 days after creation */
            endingDate = startingDate + 172800000;
        }
        long localPublicationID;
        if(!isEdit){
            localPublicationID = CommonMethods.getNewLocalPublicationID();
        } else{
            localPublicationID = publication.getId();
        }
        if(title.equals("") || location.equals("") || shareWith.equals("") || latlng == null){
            Toast.makeText(getContext(), R.string.post_please_enter_all_fields, Toast.LENGTH_SHORT).show();
        } else {
            double price;
            if (priceS.equals("")) {
                price = 0.0;
            } else {
                try {
                    price = Double.parseDouble(priceS);
                } catch (NumberFormatException e) {
                    Log.e("PublicationActivity", e.getMessage());
                    Toast.makeText(getContext(), R.string.post_toast_please_enter_a_price_in_numbers, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // TODO: 08/11/2016 repair starting time and ending time. also currently some fields are hard coded for testing
            publication = new Publication(localPublicationID, -1, title, details, location, (short) 2, latlng.latitude, latlng.longitude,
                    String.valueOf(startingDate/1000), String.valueOf(endingDate/1000),
                    "0500000000", true, CommonMethods.getDeviceUUID(getContext()), CommonMethods.getFileNameFromPath(mCurrentPhotoPath), 16, 0, user.getDisplayName(), price, "");
            Intent i = new Intent(getContext(), AddPublicationService.class);
            i.putExtra(Publication.PUBLICATION_KEY, publication.getPublicationJson().toString());
            i.putExtra(Publication.PUBLICATION_UNIQUE_ID_KEY, publication.getId());
            getContext().startService(i);
            getActivity().finish();
        }
    }

    /*
     * Begins to upload the file specified by the file path.
     */
    private void beginS3Upload(String filePath) {
        /** upload the file to the S3 server */
        if (filePath == null) {
            Toast.makeText(getContext(), "Could not find the filepath of the selected file",
                    Toast.LENGTH_LONG).show();
            return;
        }
        String[] split = filePath.split(":");
        File file = new File(split[1]);
        transferUtility.upload(getResources().getString(R.string.amazon_bucket), file.getName(),file);
        // TODO: 09/11/2016 add logic to completion or failure of upload image
        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginS3Upload -> onResume
         * -> set listeners to in progress transfers.
         */
        // observer.setTransferListener(new UploadListener());
    }
}
