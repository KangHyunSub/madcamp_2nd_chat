package com.example.bitgaram.main.bitgaram.presenter.main.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitgaram.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.bitgaram.main.bitgaram.presenter.main.fragment.NetworkManager.SERVER_ADDRESS;

public class GalleryFragment extends Fragment {

    ArrayList<Integer> images = new ArrayList<>(Arrays.asList(R.drawable.cat1, R.drawable.cat2, R.drawable.cat3, R.drawable.cat4, R.drawable.cat5,
            R.drawable.cat6, R.drawable.dog1, R.drawable.dog2, R.drawable.dog3, R.drawable.dog4,
            R.drawable.dog5, R.drawable.dog6));

    private GridView gallery;
    private GalleryGridAdapter gridAdapter;
    private Button signUpBtn;
    private ArrayList<String> gallerys;
    private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();

    public static GalleryFragment newInstance(){
        GalleryFragment galleryFragment = new GalleryFragment();
        return galleryFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.galleryframent, container, false);

        gallery = (GridView)rootView.findViewById(R.id.gridView);
        gridAdapter = new GalleryGridAdapter(getContext(), R.layout.imagecell, bitmaps);
        gallery.setAdapter(gridAdapter);
        gallerys = new ArrayList<>();

        //Gallery Button ????????? ??????
        FloatingActionButton gallerySync = rootView.findViewById(R.id.gallerySync);
        gallerySync.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //?????? ???????????? ????????? ?????? ?????????
                bitmaps.clear();
                SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
                String number = pref.getString("mynumber", "");
                //????????? ???????????? ??????
                new GalleryFragment.JSONTask().execute(SERVER_ADDRESS + "gallery/find/"+number);
                Toast.makeText(getContext(), "????????? ????????? ??? ?????????", Toast.LENGTH_LONG).show();

            }
        });

        return rootView;
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    URL url = new URL(urls[0]);//url??? ????????????.
                    con = (HttpURLConnection) url.openConnection();
                    con.connect();//?????? ??????

                    //?????? ????????? ??????
                    InputStream stream = con.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));

                    //?????? ???????????? ?????????
                    StringBuffer buffer = new StringBuffer();

                    //line??? ???????????? ?????? ?????? temp ??????
                    String line = "";

                    //??????????????? ?????? reader?????? ???????????? ???????????? ????????????. ??? node.js??????????????? ???????????? ????????????.
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    return buffer.toString();

                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        //????????? ????????????.
                        if(reader != null){
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }//finally ??????
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        //doInBackground???????????? ????????? ????????? ?????? ??????????????? ?????? ????????????.
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null){
                Toast.makeText(getContext(), "No Input", Toast.LENGTH_SHORT).show();
            }
            else {
                //????????? ????????? JSON?????? ??????
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONArray addressObject = jsonArray.getJSONObject(0).getJSONArray("gallery");

                    for(int i=0; i<addressObject.length(); i++){
                        JSONObject image = addressObject.getJSONObject(i);
                        String data = image.getString("gallery");
                        Log.d("??????1", data);
                        //gallerys.add(data);
                        bitmaps.add(StringToBitmap(data));
                    }

                    //recyclerView.setAdapter(addressAdapter);
                    //recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    //addressAdapter.notifyDataSetChanged();
                    gridAdapter = new GalleryGridAdapter(getContext(), R.layout.imagecell, bitmaps);
                    gallery.setAdapter(gridAdapter);
                    gallerys = new ArrayList<>();

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }

    }

    public static Bitmap StringToBitmap(String bitmapString) {
        byte[] decodedString = Base64.decode(bitmapString.getBytes(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;
    }
}
