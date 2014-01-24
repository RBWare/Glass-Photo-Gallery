package com.rbware.glass.photogallery;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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
    private boolean showPlayButtonInMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootLayout = getLayoutInflater().inflate(R.layout.activity_gallery, null);
        setupPhotoListView(rootLayout);
        setContentView(rootLayout);
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
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.action_play:

                return true;
            case R.id.action_delete:

                return true;
            case R.id.action_details:

                return true;
            case R.id.action_share:

                return true;
            default:
                // Nothing
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupPhotoListView(View rootLayout){

        File sdcard = Environment.getExternalStorageDirectory();
        File photoDirectory = new File(sdcard, "/DCIM/Camera");
        if (photoDirectory != null){
            mFileList.addAll(Arrays.asList(photoDirectory.listFiles()));
        }


        if(!mFileList.isEmpty()){
            mAdapter = new UIListingCardScrollAdapter();
            if (mCardScrollView == null){
                mCardScrollView = (CardScrollView)rootLayout.findViewById(R.id.card_scroll_view);
                mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSelectedListing = position;

                        if (mFileList.get(mSelectedListing).getName().toLowerCase().endsWith(".mp4"))
                            showPlayButtonInMenu = true;
                        else
                            showPlayButtonInMenu = false;

                        Log.e("Filename", "Name: " + mFileList.get(mSelectedListing).getName());
                        Log.e("Show play", "Value: " + showPlayButtonInMenu);

                        GalleryActivity.super.openOptionsMenu();
                    }
                });
            }
            mCardScrollView.setVisibility(View.VISIBLE);

            mCardScrollView.setAdapter(mAdapter);
            mCardScrollView.activate();
        } else {


        }
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

            if (v == null) {
                layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = layoutInflater.inflate(R.layout.photo_detail, null);
            }

            ImageView imageView = (ImageView)v.findViewById(R.id.imageview_photo);
            new LoadPhoto(imageView, position).execute();
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
}
