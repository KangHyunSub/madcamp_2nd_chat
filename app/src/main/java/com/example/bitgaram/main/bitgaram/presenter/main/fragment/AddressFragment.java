package com.example.bitgaram.main.bitgaram.presenter.main.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitgaram.R;
import com.example.bitgaram.main.bitgaram.presenter.main.EnvironmentData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.example.bitgaram.main.bitgaram.presenter.main.fragment.NetworkManager.SERVER_ADDRESS;
import static com.example.bitgaram.main.bitgaram.presenter.main.fragment.SignUpActivity.mynumber;

public class AddressFragment extends Fragment {
    NetworkManager networkManager = NetworkManager.newInstance(EnvironmentData.phoneNumber);
    private ArrayList<AddressData> addresses;
    private AddressAdapter addressAdapter;
    private RecyclerView recyclerView;

    public static AddressFragment newInstance(){
        AddressFragment addressFragment = new AddressFragment();
        return addressFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //?????? ?????? ??????
        addresses = new ArrayList<AddressData>();
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.addressfragment, container, false);

        //Recycler view ????????????
        addressAdapter = new AddressAdapter(addresses);
        recyclerView = rootView.findViewById(R.id.addressView);

        //Address Button ????????? ??????
        FloatingActionButton addressSync = rootView.findViewById(R.id.addressSync);
        addressSync.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //?????? ???????????? ????????? ?????? ?????????
                addresses.clear();
                SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
                String number = pref.getString("mynumber", "");

                //????????? ???????????? ??????
                new JSONTask().execute(SERVER_ADDRESS + "user/find/" + number);
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

                    Log.d("???", buffer.toString());
                    //??? ???????????? String ???????????? ????????????. ????????? protected String doInBackground(String... urls) ??????
                    return buffer.toString();

                    //????????? ???????????? ????????????.
                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //????????? ?????? disconnect???????????? ????????????.
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
                    JSONArray addressObject = jsonArray.getJSONObject(0).getJSONArray("address");

                    for(int i=0; i<addressObject.length(); i++){
                        AddressData data = new AddressData(addressObject.getJSONObject(i).getString("name"), addressObject.getJSONObject(i).getString("phonenum"));
                        addresses.add(data);
                    }

                    recyclerView.setAdapter(addressAdapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    addressAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }

    }
}
