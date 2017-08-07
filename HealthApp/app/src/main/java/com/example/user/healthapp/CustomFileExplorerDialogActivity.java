package com.example.user.healthapp;

/**
 * Created by user on 23/07/2017.
 */
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class CustomFileExplorerDialogActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 198;
    private static final int PERMISSIONS_READ_EXTERNAL_STORAGE = 199;
    private static final int SELECT_PHOTO = 100;
    ImageView image;
    TextView txt;
    Button upload, floder_save,pickImageBtn;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;
    String uploaded_file;
    private Bitmap bitmap;
    String upLoadServerUri = null;
    String filename;
    EditText usr_floder;

    String usr_name="Theo M J";
    InputStream is = null;
    String result;
    String line;
    int code;
    String org_file;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        image = (ImageView) findViewById(R.id.display_image);
        txt = (TextView) findViewById(R.id.textView);
        usr_floder = (EditText) findViewById(R.id.usr_floder);
        upload = (Button) findViewById(R.id.upload);
        floder_save = (Button) findViewById(R.id.button);
        pickImageBtn = (Button) findViewById(R.id.pick);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog = ProgressDialog.show(CustomFileExplorerDialogActivity.this, "", "Uploading file...", true);

                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                //messageText.setText("uploading started.....");
                            }
                        });
                        usr_name = usr_floder.getText().toString();

                        uploadFile(uploaded_file);
                        doc_save();


                    }
                }).start();
            }
        });

        floder_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usr_name = usr_floder.getText().toString();


                create_folder();

            }
        });

        pickImageBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pickAImage();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    public void pickAImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        } else {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_READ_EXTERNAL_STORAGE);
                } else {
                    if (resultCode == RESULT_OK) {
                        Uri selectedImage = imageReturnedIntent.getData();


                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                            bitmap = ImageResizer.decodeSampledBitmapFromFile(getRealPathFromURI(selectedImage), 2000, 1800);
                            uploaded_file = getRealPathFromURI(selectedImage);


                            filename = uploaded_file.substring(uploaded_file.lastIndexOf("/") + 1);

                            org_file = filename;
                            txt.setText(uploaded_file);
Log.e("path",""+uploaded_file);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        InputStream imageStream = null;
                        try {
                            imageStream = getContentResolver().openInputStream(selectedImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                        image.setImageBitmap(bitmap);// To display selected image in image view
                    }
                }


        }
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        if (requestCode == PERMISSIONS_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                pickAImage();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PERMISSIONS_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                pickAImage();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }


    public int uploadFile(String sourceFileUri) {
       String UserNAme="Theo M J";
        create_folder();
        upLoadServerUri = "http://acehealthcare.co.in/create_folder.php?usr_name="+UserNAme;
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {
            Log.e("uploadFile", "Source File Does not exist");
            return 0;
        }
        try { // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);
            conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);
            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
            if (serverResponseCode == 200) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        //tv.setText("File Upload Completed.");
                        Toast.makeText(CustomFileExplorerDialogActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {
            dialog.dismiss();
            ex.printStackTrace();
            Toast.makeText(CustomFileExplorerDialogActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {
            dialog.dismiss();
            e.printStackTrace();
            Toast.makeText(CustomFileExplorerDialogActivity.this, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            //Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e);
        }
        dialog.dismiss();
        return serverResponseCode;
    }


    @Override
    public void onBackPressed() {
        Intent i = new Intent(CustomFileExplorerDialogActivity.this, IconActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            // Handle the camera action
            Intent i = new Intent(CustomFileExplorerDialogActivity.this, IconActivity.class);
            startActivity(i);
            finish();
        } else if (id == R.id.doc) {
            Intent i = new Intent(CustomFileExplorerDialogActivity.this, MainActivity.class);
            startActivity(i);
            finish();

        } else if (id == R.id.user) {
            Intent i = new Intent(CustomFileExplorerDialogActivity.this, Documents.class);
            startActivity(i);
            finish();

        } else if (id == R.id.plus) {
            Intent i = new Intent(CustomFileExplorerDialogActivity.this, CustomFileExplorerDialogActivity.class);
            startActivity(i);
            finish();

        } else if (id == R.id.rep) {
            Intent i = new Intent(CustomFileExplorerDialogActivity.this, ViewReports.class);
            startActivity(i);
            finish();

        } else if (id == R.id.query) {
            Intent i = new Intent(CustomFileExplorerDialogActivity.this, Reports.class);
            startActivity(i);
            finish();

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void doc_save() {

        runOnUiThread(new Runnable() {


            @Override
            public void run() {


                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("doc_name", org_file));
                nameValuePairs.add(new BasicNameValuePair("patient_id", String.valueOf(Login.person_id)));
                Log.i("namevaluepairs", nameValuePairs.toString());


                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost("http://acehealthcare.co.in/doc_save.php");

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
                    //Log.i("nameValuePairs", String.valueOf(nameValuePairs));
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                    Log.e("pass 1", "connection success");

                } catch (Exception e) {

                    Log.e("fail 1", e.toString());

                    //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");

                    }
                    is.close();
                    result = sb.toString();
                    Log.e("pass 2", "connection success");
                } catch (Exception e) {

                    Log.e("fail 2", e.toString());
                }
                try {
                    JSONObject json_data = new JSONObject(result);
                    code = (json_data.getInt("code"));

                    if (code == 1) {
                        Toast.makeText(getBaseContext(), "Saved", Toast.LENGTH_SHORT).show();
                        // finish actvity
                        Intent i = new Intent(CustomFileExplorerDialogActivity.this, IconActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(getBaseContext(), "Sorry,Try again", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {


                    Log.e("fall 3", e.toString());
                    Log.i("tagconvertstr", "[" + result + "]");
                }

            }

        });
    }



    public void create_folder() {

        runOnUiThread(new Runnable() {


            @Override
            public void run() {


                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("usr_name", usr_name));


                try {
                    HttpClient httpClient = new DefaultHttpClient();

                    HttpPost httpPost = new HttpPost("http://acehealthcare.co.in/create_folder.php?usr_name=usr_name");

//                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
//                    Log.i("nameValuePairs",""+ String.valueOf(nameValuePairs));
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                    Log.e("pass 1", "connection success");

                } catch (Exception e) {

                    Log.e("fail 1", e.toString());
                    //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");

                    }
                    is.close();
                    result = sb.toString();
                    Log.e("pass 2", "connection success");
                } catch (Exception e) {

                    Log.e("fail 2", e.toString());
                }

                Log.e("response",""+result);
                try {
                    JSONObject json_data = new JSONObject(result);
                    Log.e("response",""+result);
                    code = (json_data.getInt("code"));

                    if (code == 1) {
                        Toast.makeText(getBaseContext(), "Saved", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getBaseContext(), "Sorry,Try again", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {

                    Log.e("fall 3", e.toString());
                    Log.i("tagconvertstr", "[" + result + "]");
                }


            }

        });
    }

}
