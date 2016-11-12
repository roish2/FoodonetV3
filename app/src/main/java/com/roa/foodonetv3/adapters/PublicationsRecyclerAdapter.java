package com.roa.foodonetv3.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.model.Publication;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PublicationsRecyclerAdapter extends RecyclerView.Adapter<PublicationsRecyclerAdapter.PublicationHolder> {
    /** recycler adapter for publication */
    private static final String TAG = "PubsRecyclerAdapter";

    private Context context;
    private ArrayList<Publication> publications = new ArrayList<>();
    private TransferUtility transferUtility;

    public PublicationsRecyclerAdapter(Context context) {
        this.context = context;
        transferUtility = CommonMethods.getTransferUtility(context);
//        setHasStableIds(true);

    }

    public void updatePublications(ArrayList<Publication> publications){
        this.publications = publications;
        notifyDataSetChanged();
    }

    @Override
    public PublicationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.publication_list_item,parent,false);
        return new PublicationHolder(v);
    }

    @Override
    public void onBindViewHolder(PublicationHolder holder, int position) {
        holder.bindPublication(publications.get(position));
    }

    @Override
    public int getItemCount() {
        return publications.size();
    }

    class PublicationHolder extends RecyclerView.ViewHolder implements TransferListener {
        private Publication publication;
        private ImageView imagePublication,imagePublicationGroup;
        private TextView textPublicationTitle, textPublicationAddressDistance;
        private File mCurrentPhotoFile;
        private int observerId;


        PublicationHolder(View itemView) {
            super(itemView);
            imagePublication = (ImageView) itemView.findViewById(R.id.imagePublication);
            imagePublicationGroup = (ImageView) itemView.findViewById(R.id.imagePublicationGroup);
            textPublicationTitle = (TextView) itemView.findViewById(R.id.textPublicationTitle);
            textPublicationAddressDistance = (TextView) itemView.findViewById(R.id.textPublicationAddressDistance);
        }

        private void bindPublication(Publication publication) {
            this.publication = publication;
            // TODO: add image logic, add distance logic, number of users who joined, currently hard coded
            textPublicationTitle.setText(publication.getTitle());
            String addressDistance = CommonMethods.getRoundedStringFromNumber(15.7f);
            textPublicationAddressDistance.setText(addressDistance);
            //add photo here
            if(publication.getPhotoURL().equals("")){
                imagePublication.setImageResource(R.drawable.camera_xxh);
                /** no image saved, display default image */
                // TODO: 10/11/2016 display default image 

            }else{
                /** there is an image available to download or is currently on the device */
                // TODO: 10/11/2016 check version of the publication as well
                /** check if the image is already saved on the device */
                mCurrentPhotoFile = new File(CommonMethods.getPhotoPathByID(context,publication.getId()));
                if (mCurrentPhotoFile.exists()) {
                    /** image was found and is the same as the publication id */

                    Picasso.with(context)
                            .load(mCurrentPhotoFile)
                            .resize(1000, 1000)
                            .centerCrop()
                            .into(imagePublication);
                } else {
                    /** image ready to download, not on the device */
                        TransferObserver observer = transferUtility.download(context.getResources().getString(R.string.amazon_bucket),
                                publication.getPhotoURL(), mCurrentPhotoFile
                        );
                        observer.setTransferListener(this);
                        observerId = observer.getId();

                }
            }

        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            /** listener for the s3 server download, needs to be adapter wide since it's currently keeps using the same image in different layout */
            // TODO: 09/11/2016 check picasso adapter for the images and using the s3 observer on an adapter scale
            Log.d(TAG,"amazon onStateChanged " + id + " "  + state.toString());
            if(state == TransferState.COMPLETED){
                if(observerId==id){
                Picasso.with(context)
                        .load(mCurrentPhotoFile)
                        .resize(imagePublication.getWidth(),imagePublication.getHeight())
                        .centerCrop()
                        .into(imagePublication);
                }

            }
        }
        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//            Log.d(TAG,"amazon onProgressChanged" + id + " " + bytesCurrent+ "/" + bytesTotal);
        }
        @Override
        public void onError(int id, Exception ex) {
            Log.d(TAG,"amazon onError" + id + " " + ex.toString());
        }
    }
}

