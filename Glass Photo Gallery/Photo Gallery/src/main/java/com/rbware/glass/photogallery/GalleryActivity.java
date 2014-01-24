package com.rbware.glass.photogallery;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class GalleryActivity extends Activity {

    private ArrayList<File> mFileList = new ArrayList<File>();

    private int mSelectedListing;
    private UIListingCardScrollAdapter mAdapter;
    private CardScrollView mCardScrollView;
    private TextView mNoMediaFound;
    private View infoView;
    private RelativeLayout mainLayoutContainer;
    private boolean showPlayButtonInMenu;

    private boolean isShowingDeleteOverlay = false;
    private boolean isShowingInfoOverlay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        setupPhotoListView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (showPlayButtonInMenu)
            menu.findItem(R.id.action_play).setVisible(true);
        else
            menu.findItem(R.id.action_play).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if(isShowingInfoOverlay){
            if (mainLayoutContainer != null && infoView != null)
                mainLayoutContainer.removeView(infoView);

            isShowingInfoOverlay = false;
            AudioManager audio = (AudioManager) GalleryActivity.this.getSystemService(Context.AUDIO_SERVICE);
            audio.playSoundEffect(Sounds.DISMISSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.action_play:

                playMovieFile(mFileList.get(mSelectedListing).getAbsolutePath());
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mFileList.get(mSelectedListing).getAbsolutePath()));
//                intent.setDataAndType(Uri.parse(mFileList.get(mSelectedListing).getAbsolutePath()), "video/*");
//                startActivity(intent);
                return true;
            case R.id.action_delete:
                new DeletePhoto().execute(mFileList.get(mSelectedListing).getName());
                return true;
            case R.id.action_details:
                showFileDetails(mFileList.get(mSelectedListing));
                return true;
            case R.id.action_share:
                Intent sharingIntent;
                if (showPlayButtonInMenu){
                    sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("video/*");
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mFileList.get(mSelectedListing).getAbsolutePath()));
                    startActivity(Intent.createChooser(sharingIntent, ""));
                } else {
                    sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("image/*");
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mFileList.get(mSelectedListing).getAbsolutePath()));
                    startActivity(Intent.createChooser(sharingIntent, ""));
                }
                return true;
            default:
                // Nothing
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupPhotoListView(){

        File sdcard = Environment.getExternalStorageDirectory();
        File photoDirectory = new File(sdcard, "/DCIM/Camera");
        if (photoDirectory != null){
            mFileList.addAll(Arrays.asList(photoDirectory.listFiles()));
        }


        mCardScrollView = (CardScrollView)findViewById(R.id.card_scroll_view);

        if(!mFileList.isEmpty()){
            mAdapter = new UIListingCardScrollAdapter();
            mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mSelectedListing = position;

                    if (isShowingDeleteOverlay)
                        return;

                    if (isShowingInfoOverlay) {
                        if (mainLayoutContainer != null && infoView != null)
                            mainLayoutContainer.removeView(infoView);

                        isShowingInfoOverlay = false;
                        return;
                    }

                    if (mFileList.get(mSelectedListing).getName().toLowerCase().endsWith(".mp4"))
                        showPlayButtonInMenu = true;
                    else
                        showPlayButtonInMenu = false;

                    GalleryActivity.super.openOptionsMenu();

                }
            });
            mCardScrollView.setVisibility(View.VISIBLE);
            mCardScrollView.setAdapter(mAdapter);
            mCardScrollView.activate();
        } else {
            mCardScrollView.setVisibility(View.GONE);
            mNoMediaFound = (TextView)findViewById(R.id.noMediaFound);
            mNoMediaFound.setVisibility(View.VISIBLE);
        }
    }

    private void showFileDetails(File currentFile){
        isShowingInfoOverlay = true;

        mainLayoutContainer = (RelativeLayout)findViewById(R.id.container);
        infoView = getLayoutInflater().inflate(R.layout.info_overlay, null);

        TextView photoName = (TextView)infoView.findViewById(R.id.info_overlay_textview_photo_name);
        TextView photoStorageLocation = (TextView)infoView.findViewById(R.id.info_overlay_textview_photo_storage_location);
        TextView photoSize = (TextView)infoView.findViewById(R.id.info_overlay_textview_photo_size);

        photoName.setText(currentFile.getName());
        photoStorageLocation.setText(currentFile.getAbsolutePath().substring(0,
                currentFile.getAbsoluteFile().toString().lastIndexOf("/")));

        String fileSize = "";
        long size = (currentFile.length() / 1024) / 1024;
        fileSize = size + " MB (" + (currentFile.length() / 1024) + " KB)";
        photoSize.setText(fileSize);

        mainLayoutContainer.addView(infoView);
    }

    private void playMovieFile(String fileLocation){
        Intent videoPlayerIntent = new Intent(this, VideoPlayerActivity.class);
        videoPlayerIntent.putExtra("videoUrl", fileLocation);
        startActivity(videoPlayerIntent);
    }

    private class UIListingCardScrollAdapter extends CardScrollAdapter {

        @Override
        public int findIdPosition(Object id) {
            return -1;
        }

        @Override
        public int findItemPosition(Object item) {
            return mFileList.indexOf(item);
        }

        @Override
        public int getCount() {
            return mFileList.size();
        }

        @Override
        public Object getItem(int position) {
            return mFileList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            LayoutInflater layoutInflater;

            if (isShowingInfoOverlay){
                if (mainLayoutContainer != null && infoView != null)
                    mainLayoutContainer.removeView(infoView);

                isShowingInfoOverlay = false;
            }

            if (v == null) {
                layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = layoutInflater.inflate(R.layout.photo_detail, null);
            }

            ImageView imageView = (ImageView)v.findViewById(R.id.imageview_photo);
            ImageView imagePlayButton = (ImageView)v.findViewById(R.id.imageview_play_button);
            new LoadPhoto(imageView, position).execute();

            if(mFileList.get(position).getName().endsWith(".mp4")){
                // Show play button
                imagePlayButton.setVisibility(View.VISIBLE);
            } else {
                imagePlayButton.setVisibility(View.INVISIBLE);
            }
            return v;
        }

    }

    public class LoadPhoto extends AsyncTask<Object, Object, Object>{

        private ImageView imageView;
        private Bitmap bitmap;
        private int imageIndex;

        public LoadPhoto(ImageView view, int index){
            imageView = view;
            imageIndex = index;
        }

        @Override
        protected Object doInBackground(Object... params) {

            if (mFileList.get(imageIndex).getName().endsWith(".mp4")){
                bitmap = ThumbnailUtils.createVideoThumbnail(
                        mFileList.get(imageIndex).getAbsolutePath(),
                        MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
            } else {

                bitmap = BitmapFactory.decodeFile(mFileList.get(imageIndex).getAbsolutePath());
                bitmap = Bitmap.createScaledBitmap(bitmap,
                        640, 360, false);

                try{
                    ByteArrayOutputStream bytearroutstream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytearroutstream);
                    bytearroutstream.close();
                } catch(IOException e){
                    Log.e("IOException", e.toString());
                }
            }
            return "";

        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            imageView.setImageBitmap(bitmap);
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
        }
    }

    public class DeletePhoto extends AsyncTask<String, Integer, Boolean>{

        private RelativeLayout mainLayout;
        private View deleteView;
        private ImageView statusImage;
        private TextView statusText;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            isShowingDeleteOverlay = true;
            mainLayout = (RelativeLayout)findViewById(R.id.container);
            deleteView = getLayoutInflater().inflate(R.layout.delete_overlay, null);
            statusImage = (ImageView)deleteView.findViewById(R.id.delete_overlay_imageview_status);
            statusText = (TextView)deleteView.findViewById(R.id.delete_overlay_textview_status);
            mainLayout.addView(deleteView);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            try{
                File sdcard = Environment.getExternalStorageDirectory();

                boolean status = false;
                Thread.sleep(1500);

                File fileToRemove = new File(sdcard, "/DCIM/Camera/" + params[0]);
                status = fileToRemove.delete();

                // Update the File List
                mFileList.clear();

                File photoDirectory = new File(sdcard, "/DCIM/Camera");
                if (photoDirectory != null){
                    mFileList.addAll(Arrays.asList(photoDirectory.listFiles()));
                }
                publishProgress(1);

                return status;
            } catch(InterruptedException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            AudioManager audio = (AudioManager) GalleryActivity.this.getSystemService(Context.AUDIO_SERVICE);
            if (result){

                audio.playSoundEffect(Sounds.SUCCESS);

                // Had to introduce lag to make this function like other areas of Glass that delete doesn't happen instantaneously
                try{
                    Thread.sleep(1500);

                } catch(InterruptedException e){ }
                setupPhotoListView();

            } else {
                audio.playSoundEffect(Sounds.ERROR);
            }

            mainLayout.removeView(deleteView);
            isShowingDeleteOverlay = false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            statusImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_50));
            statusText.setText("Success");

        }
    }
}
