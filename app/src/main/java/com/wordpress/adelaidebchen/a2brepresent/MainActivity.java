package com.wordpress.adelaidebchen.a2brepresent;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.wordpress.adelaidebchen.a2brepresent.adaptor.CongressAdaptor;
import com.wordpress.adelaidebchen.a2brepresent.model.Person;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity {
    final int REQUEST_CODE = 34;
    final Map<String, Person> dict = new HashMap<>();

    private String api_key = "954645444994489799477277bb47b4bb65e4b4b";
    private String api_key2 = "t2B8gkA4dz89DCGqHmjFpSpJfr1L7ySg9Gpgqj6P";
    FusedLocationProviderClient mFusedLocationClient;
    Task<Location> pinpoint;
    String zipcode;
    double lat;
    double lng;
    long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestPermission();

        goToMain();
    }

    private void goToMain() {
        setContentView(R.layout.activity_main);
        final TextView button = findViewById(R.id.next);
        final TextView number = findViewById(R.id.input_zip);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = number.getText().toString();
                findAddress(input);
            }
        });
        getLocation();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    public void getLocation() {
        final TextView text = findViewById(R.id.textView);

        final Button rand = findViewById(R.id.rand_location);
        rand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("generating...");
                rand.setEnabled(false);
                startTime = new Date().getTime();
                randomZip();
            }
        });

        final Button curr = findViewById(R.id.curr_location);
        curr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    text.setText("Locating...");
                    curr.setEnabled(false);
                    startTime = new Date().getTime();
                    pinpoint = mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations, this can be null.
                        if (location != null) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            text.setText(String.format("%f, %f", lat, lng) );
                            findAddress(lat, lng);
                        } else {
                            text.setText("empty");
                        }
                        }
                    });
                    System.out.print(pinpoint);
                } catch (SecurityException e) {
                    System.out.println("Location permission denied :c");
                    text.setText("null");
                }
            }
        });
    }

    private void randomZip() {
        Random ran = new Random();
        int zipInt = ran.nextInt(100000);
        zipcode = String.format("%1$05d", zipInt);
        findAddress(zipcode);
    }

    private void findAddress(String zip) {
        final String temp = "https://api.geocod.io/v1.3/geocode?q=%1$s&fields=cd&api_key=%2$s";
        String urlStr = String.format(temp, zip, api_key);
        findingAddress(urlStr);
    }
    private void findAddress(double lat0, double lng0) {
        final String temp = "https://api.geocod.io/v1.3/reverse?q=%1$f%%2C%2$f&fields=cd&api_key=%3$s";
        String urlStr = String.format(temp, lat0, lng0, api_key);
        findingAddress(urlStr);
    }

    private void findingAddress(String urlStr) {
        final TextView text = findViewById(R.id.textView);
        text.setText("Fetching...");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            URL url = new URL(urlStr);

            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String inputLine;
            StringBuffer result = new StringBuffer();

            while ((inputLine = br.readLine()) != null) {
                result.append(inputLine);
            }

            br.close();
            format(result.toString());

        } catch (Exception e) {
        }
    }

    private void format(String jsonData) throws JSONException {
        JSONObject root = new JSONObject(jsonData);
        JSONArray results = root.getJSONArray("results");
        if (results.length() == 0) {
            randomZip();
            return;
        }

        String zipcode = results.getJSONObject(0)
                .getJSONObject("address_components")
                .getString("zip");


        for (int i = 0; i < results.length(); i++) {
            JSONObject address = results.getJSONObject(i);
            JSONObject fields = address.getJSONObject("fields");
            JSONArray cds = fields.getJSONArray("congressional_districts");
            for (int j = 0; j < cds.length(); j++) {
                JSONObject cd = cds.getJSONObject(j);
                JSONArray curr_legs = cd.getJSONArray("current_legislators");
                for (int k = 0; k < curr_legs.length(); k++) {

                    JSONObject person = curr_legs.getJSONObject(k);
                    JSONObject bio = person.getJSONObject("bio");
                    String name = bio.getString("first_name") + ' ' + bio.getString("last_name");
                    String party = bio.getString("party");
                    JSONObject contact = person.getJSONObject("contact");
                    String link = contact.getString("url");
                    String email = contact.getString("contact_form");
                    JSONObject references = person.getJSONObject("references");
                    String bioguide_id = references.getString("bioguide_id");

                    Person card = new Person(name, party, link, email, name, bioguide_id);
                    dict.put(name, card);
                }
            }
        }
        districtView(zipcode, dict);
    }

    private void districtView(String zipcode, final Map dict) {
        setContentView(R.layout.district);

        TextView mainZip = findViewById(R.id.mainZIP);
        TextView back = findViewById(R.id.backButton);
        mainZip.setText(zipcode);

        RecyclerView view = findViewById(R.id.listOfCards);
        List<Person> list = new ArrayList(dict.values());
        CongressAdaptor adaptor = new CongressAdaptor(list, this);

        view.setLayoutManager(new LinearLayoutManager(this));
        view.setAdapter(adaptor);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dict.clear();
                goToMain();
            }
        });

        Button rand = findViewById(R.id.rand_location);
        Button curr = findViewById(R.id.curr_location);
        if (rand != null && !rand.isEnabled()) {
            rand.setEnabled(true);
        }
        if (curr != null && !curr.isEnabled()) {
            curr.setEnabled(true);
        }
    }

    public void setPartyIcon(Person person, ImageView view) {
        if (person.getParty().equals("Democrat")) {
            view.setImageResource(R.drawable.donkey);
        } else if (person.getParty().equals("Republican")) {
            view.setImageResource(R.drawable.elephant);
        } else {
            view.setImageResource(R.drawable.independent);
        }
    }

    private String portraitURL(String bioguide_id) {
        String template = "http://bioguide.congress.gov/bioguide/photo/%1$s/%2$s.jpg";
        return String.format(template, bioguide_id.charAt(0), bioguide_id);
    }

    private void findCommittees(String api_key, String bioguide_id) {
        final TextView test = findViewById(R.id.committees);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            URL url = new URL(String.format("https://api.propublica.org/congress/v1/members/%1$s.json", bioguide_id));

            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestProperty("X-API-Key:", api_key);
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String inputLine;
            StringBuffer result = new StringBuffer();

            while ((inputLine = br.readLine()) != null) {
                result.append(inputLine);
            }

            br.close();

            test.setText(result.toString());
            parseCommittes(result.toString());

        } catch (Exception e) {
            e.getMessage();
        }

    }

    private void parseCommittes(String jsonData) throws JSONException {
        TextView comm = findViewById(R.id.committees);

        JSONObject root = new JSONObject(jsonData);
        JSONArray results = root.getJSONArray("results");
        JSONObject result = results.getJSONObject(0);
        JSONArray roles = result.getJSONArray("roles");
        for (int i=0; i<roles.length(); i++) {
        //    JSONObject role = roles.get(i);
        }

    }

    public void detailedView (final Person person) {

        setContentView(R.layout.detailedview);

        TextView name = findViewById(R.id.name1A);
        ImageView party = findViewById(R.id.party1A);
        ImageView link = findViewById(R.id.link1A);
        ImageView email = findViewById(R.id.email1A);
        ImageView picture = findViewById(R.id.portrait);
        TextView back = findViewById(R.id.backButton);

        findCommittees(api_key2, person.getBioguide_id());

        name.setText(person.getPersonName());
        setPartyIcon(person, party);

        //comm.setText(committees(api_key, person.getBioguide_id()));

        new DownloadImageFromInternet(picture)
                .execute(portraitURL(person.getBioguide_id()));

        link.setImageResource(R.drawable.outlink);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(person.getLink()));
                startActivity(intent);
            }
        });

        email.setImageResource(R.drawable.email);

        if (person.getEmail().equals("null")) {
            email.setVisibility(View.INVISIBLE);
        } else {
            email.setVisibility(View.VISIBLE);
        }

        if (email != null ) {
            email.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse(person.getEmail()));
                    startActivity(intent);
                }
            });
        }

        if (back != null) {
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    districtView(zipcode, dict);
                }
            });
        }

    }

    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap pixels = null;
            try {
                InputStream input = new java.net.URL(imageURL).openStream();
                pixels = BitmapFactory.decodeStream(input);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return pixels;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}