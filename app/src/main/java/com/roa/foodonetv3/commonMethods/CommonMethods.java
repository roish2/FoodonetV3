package com.roa.foodonetv3.commonMethods;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.activities.MapActivity;
import com.roa.foodonetv3.activities.SignInActivity;
import com.roa.foodonetv3.activities.WelcomeUserActivity;
import com.roa.foodonetv3.model.User;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonMethods {
    private static final String TAG = "CommonMethods";

    /** We only need one instance of the clients and credentials provider */
    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;
    private static TransferUtility sTransferUtility;


    public static void navigationItemSelectedAction(Context context, int id){
        /** handle the navigation actions from the drawer*/
        Intent intent;
        switch (id){
            case R.id.nav_my_shares:

                break;
            case R.id.nav_all_events:

                break;
            case R.id.nav_map_view:
                intent = new Intent(context, MapActivity.class);
                context.startActivity(intent);
                break;
            case R.id.nav_notifications:

                break;
            case R.id.nav_groups:

                break;
            case R.id.nav_settings:
                intent = new Intent(context,SignInActivity.class);
                context.startActivity(intent);
                break;
            case R.id.nav_contact_us:

                break;
            case R.id.nav_about:

                break;
        }
    }

    public static String getDeviceUUID(Context context){
        /** returns a UUID */
        return PreferenceManager.getDefaultSharedPreferences(context).getString(User.ACTIVE_DEVICE_DEV_UUID,null);
    }

    public static int getMyUserID(Context context){
        /** returns the userID from shared preferences */
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(User.IDENTITY_PROVIDER_USER_ID,-1);
    }

    public static void setMyUserID(Context context,int userID){
        /** saves the userID to shared preferences */
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(User.IDENTITY_PROVIDER_USER_ID,userID).apply();
    }

    public static long getNewLocalPublicationID() {
        /** should increment negatively for a unique id until the server gives us a server unique publication id to replace it */
        //todo add a check for available negative id, currently hard coded
        return -1;
    }

    public static String getRoundedStringFromNumber(float num){
        DecimalFormat df = new DecimalFormat("####0.00");
        return df.format(num);
    }
    public static String getRoundedStringFromNumber(double num){
        DecimalFormat df = new DecimalFormat("####0.00");
        return df.format(num);
    }

    public static File createImageFile(Context context) throws IOException {
        /** Creates a local image file name for taking the picture with the camera */
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */);
    }
    public static File createImageFile(Context context, long publicationID) throws IOException {
        /** Creates a local image file name for downloaded images from s3 server of a specific publication */
        String imageFileName = "PublicationID." + publicationID;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        return File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */);

        File newFile = new File(storageDir.getPath()+"/"+imageFileName+".jpg");
        Log.d(TAG,"newFile = "+newFile.getPath());
        return newFile;
    }


    public static String getFileNameFromPath(String path){
        /** returns the file name without the path */
        String [] segments = path.split("/");
        return segments[segments.length-1];
    }

    public static String getPublicationIDFromFile(String path){
        /** returns the file name without the path */
        String [] segments = path.split(".");
        if (segments.length > 1) {
            return segments[segments.length - 2];
        }else {
            return "n";
        }
    }

    public static String getPhotoPathByID(Context context,long publicationID){
        /** Creates a local image file name for downloaded images from s3 server of a specific publication */
        String imageFileName = "PublicationID." + publicationID;
        String storageDir = (context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath());
//        return File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */);
        String newFile = storageDir+"/"+imageFileName+".jpg";
        Log.d(TAG,"newFile = "+newFile);
        return newFile;
    }

    public static boolean editOverwriteImage(Context context,String mCurrentPhotoPath){
        /** after capturing an image, we'll crop, downsize and compress it to be sent to the s3 server,
         * then, it will overwrite the local original one.
         * returns true if successful*/

        /** ratio - 16:9 */
        final float ratio = 16/9f;
        final int WANTED_HEIGHT = 720;
        final int WANTED_WIDTH = (int)(WANTED_HEIGHT*ratio);
        Bitmap sourceBitmap;
        try {
            sourceBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse("file:"+mCurrentPhotoPath));
            Bitmap cutBitmap;

            /** cut the image to display as a 16:9 image */
            if (sourceBitmap.getHeight()*ratio < sourceBitmap.getWidth()){
                /** full height of the image, cut the width*/
                cutBitmap = Bitmap.createBitmap(
                        sourceBitmap,
                        (int)((sourceBitmap.getWidth() - (sourceBitmap.getHeight()*ratio))/2),
                        0,
                        (int)(sourceBitmap.getHeight()*ratio),
                        sourceBitmap.getHeight()
                );
            }else{
                /** full width of the image, cut the height*/
                cutBitmap = Bitmap.createBitmap(
                        sourceBitmap,
                        0,
                        (int)((sourceBitmap.getHeight() - (sourceBitmap.getWidth()/ratio))/2),
                        sourceBitmap.getWidth(),
                        (int)(sourceBitmap.getWidth()/ratio)
                );
            }
            /** scale the image down*/
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(cutBitmap,WANTED_WIDTH,WANTED_HEIGHT,false);

            /** compress the image and overwrite the original one*/
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(mCurrentPhotoPath);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                return true;
            } catch (Exception e) {
                Log.e(TAG,e.getMessage());
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG,e.getMessage());
                }
            }
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        }
        return false;

    }


    /**
     * Gets an instance of CognitoCachingCredentialsProvider which is
     * constructed using the given Context.
     *
     * @param context An Context instance.
     * @return A default credential provider.
     */
    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    context.getResources().getString(R.string.amazon_pool_id),
                    Regions.EU_WEST_1);
        }
        return sCredProvider;
    }

    /**
     * Gets an instance of a S3 client which is constructed using the given
     * Context.
     *
     * @param context An Context instance.
     * @return A default S3 client.
     */
    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()));
        }
        return sS3Client;
    }

    /**
     * Gets an instance of the TransferUtility which is constructed using the
     * given Context
     *
     * @param context An Context instance.
     * @return a TransferUtility instance
     */
    public static TransferUtility getTransferUtility(Context context) {
        if (sTransferUtility == null) {
            sTransferUtility = new TransferUtility(getS3Client(context.getApplicationContext()),
                    context.getApplicationContext());
        }

        return sTransferUtility;
    }
}
