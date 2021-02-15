package com.example.atlanticgrains;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Response;

import static java.lang.Double.parseDouble;

public class MainActivity extends AppCompatActivity {
    Button btnSignIn;
    api_class apic;
    urlList_class urlc = new urlList_class();
    utility_class utilityc = new utility_class();
    TextInputLayout txtUsername,txtPassword;

    DatabaseHelper myDb = new DatabaseHelper(this);
    DatabaseHelper2 myDb2;
    DatabaseHelper3 myDb3;
    DatabaseHelper4 myDb4;
    DatabaseHelper5 myDb5;
    DatabaseHelper8 myDb8;

    String gText = "";
    long mLastClickTime = 0;
    long queueid;
    DownloadManager manager;

    private OkHttpClient client;
    int userid,isManager = 0,isSales = 0, isProduction = 0,isAdmin = 0;
    boolean isManagerB = false,isSalesB=false,isProductionB = false,isAdminB = false;
    String fullName="",whse = "",resultToken = "",branch = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();

        btnSignIn = findViewById(R.id.btnSignIn);
        txtUsername = findViewById(R.id.username);
        txtPassword = findViewById(R.id.password);

        myDb2 = new DatabaseHelper2(this);
        myDb3 = new DatabaseHelper3(this);
        myDb4 = new DatabaseHelper4(this);
        myDb5 = new DatabaseHelper5(this);
        myDb8 = new DatabaseHelper8(this);

        client = new OkHttpClient();

        if(isNetworkAvailable()){
            BackTask backTask = new BackTask();
            backTask.execute("https://raw.githubusercontent.com/laikamanor/files/master/agi_file.txt");

            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)){
                        DownloadManager.Query req_query = new DownloadManager.Query();
                        req_query.setFilterById(queueid);
                        Cursor c = manager.query(req_query);
                        if(c.moveToFirst()){
                            int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                            if(DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)){
                                openDownloads(MainActivity.this);
                            }
                        }
                    }
                }
            };
//                here
            registerReceiver(broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }else {

        }


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);
                String sUsername = txtUsername.getEditText().getText().toString().trim();
                String sPassword = txtPassword.getEditText().getText().toString().trim();
                apic = new api_class(urlc.UserLogin + "?username=" + sUsername + "&password=" + sPassword ,"GET","", utilityc.getIPAddress(MainActivity.this));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.startLoadingDialog();
                            }
                        });

                        String response = apic.getResponse();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    loadingDialog.dismissDialog();
                                    String msg = "";
                                    JSONObject jsonObject = new JSONObject();
                                    if(response.substring(0,1).equals("{")){
                                        jsonObject = new JSONObject(response);
                                        msg = jsonObject.getString("message");

                                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
                                        builder.setTitle("Message");
                                        builder.setMessage(msg);
                                        builder.setCancelable(false);
                                        JSONObject finalJsonObject = jsonObject;
                                        String finalMsg = msg;
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    if (finalJsonObject.getBoolean("success")) {
                                                        JSONObject jsonObjectData = finalJsonObject.getJSONObject("data");
                                                        userid = jsonObjectData.getInt("id");
                                                        resultToken = finalJsonObject.getString("token");
                                                        fullName = jsonObjectData.getString("fullname");
                                                        whse = jsonObjectData.getString("whse");
                                                        branch = jsonObjectData.getString("branch");
                                                        isManagerB = (!jsonObjectData.isNull("isManager") && jsonObjectData.getBoolean("isManager"));
                                                        isSalesB = (!jsonObjectData.isNull("isSales") && jsonObjectData.getBoolean("isSales"));
                                                        isProductionB = (!jsonObjectData.isNull("isProduction") && jsonObjectData.getBoolean("isProduction"));
                                                        isAdminB = (!jsonObjectData.isNull("isAdmin") && jsonObjectData.getBoolean("isAdmin"));
                                                        isAdmin = (isAdminB ? 1 : 0);
                                                        isManager = (isManagerB ? 1 : 0);
                                                        isSales = (isSalesB ? 1 : 0);
                                                        isProduction = (isProductionB ? 1 : 0);
                                                        saveToken(resultToken);
                                                        saveLoggedIn();

                                                        myDb8.truncateTable();

                                                        downloadsJSONS(finalJsonObject.getString("token"));
//                                                    finish();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                dialog.dismiss();
                                            }
                                        });
                                        builder.show();
                                    }else {
                                        msg = response;
                                        String finalMsg1 = msg;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                btnSignIn.setEnabled(true);
                                                if (finalMsg1.contains("Failed to connect to") || finalMsg1.contains("timeout") || finalMsg1.contains("Unable to resolve host")) {
                                                    isNoInternet();
                                                }else{
                                                    Toast.makeText(getBaseContext(), finalMsg1, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                    Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).start();
            }
        });
    }

    public void generateShit(){
        String AlphaNumericString = "abcdefghijklmnopqrstuvxyz"+ "0123456789";
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        for(int i = 0; i <= 10; i ++){
            System.out.println("HPH1004" + sb.toString());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void openDownloads(@NonNull Activity activity) {
        if (isSamsung()) {
            Intent intent = activity.getPackageManager()
                    .getLaunchIntentForPackage("com.sec.android.app.myfiles");
            intent.setAction("samsung.myfiles.intent.action.LAUNCH_MY_FILES");
            intent.putExtra("samsung.myfiles.intent.extra.START_PATH",
                    getDownloadsFile().getPath());
            activity.startActivity(intent);
        }
        else activity.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
    }

    public static boolean isSamsung() {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer != null) return manufacturer.toLowerCase().equals("samsung");
        return false;
    }

    public static File getDownloadsFile() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public void saveToken(String token){
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token",token).apply();
    }

    public  void saveLoggedIn(){
        String susername = txtUsername.getEditText().getText().toString();
        String spassword = txtPassword.getEditText().getText().toString();
        SharedPreferences sharedPreferences = getSharedPreferences("LOGIN",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username",susername).apply();
        editor.putString("password",spassword).apply();
        editor.putString("fullname",fullName).apply();
        editor.putString("branch",branch).apply();
        editor.putString("userid",Integer.toString(userid)).apply();
        editor.putString("whse",whse).apply();
        editor.putString("isManager",Integer.toString(isManager)).apply();
        editor.putString("isSales",Integer.toString(isSales)).apply();
        editor.putString("isProduction",Integer.toString(isProduction)).apply();
        editor.putString("isAdmin",Integer.toString(isAdmin)).apply();
//        uc.insetLoginLogs(MainActivity.this, susername);
//        uc.checkCutOff(MainActivity.this, susername);
    }

    //background process to download the file from internet
    private class BackTask extends AsyncTask<String,Integer,Void> {
        LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);
        String text="";
        protected void onPreExecute(){
            super.onPreExecute();
            //display progress dialog
            loadingDialog.startLoadingDialog();
        }
        protected Void doInBackground(String...params){
            URL url;
            try {
                //create url object to point to the file location on internet
                url = new URL(params[0]);
                //make a request to server
                HttpURLConnection con=(HttpURLConnection)url.openConnection();
                //get InputStream instance
                InputStream is=con.getInputStream();
                //create BufferedReader object
                BufferedReader br=new BufferedReader(new InputStreamReader(is));
                String line;
                //read content of the file line by line
                while((line=br.readLine())!=null){
                    text+=line;

                }

                br.close();

            }catch (Exception e) {
                e.printStackTrace();
                loadingDialog.dismissDialog();
            }

            return null;

        }


        //        https://github.com/laikamanor/mobile-pos-v2/releases/download/v1.17/AtlanticBakery.apk
        protected void onPostExecute(Void result){
            loadingDialog.dismissDialog();
            gText = text;
            double currentVersion = 0.00;
            try{
                currentVersion = Double.parseDouble(text);
            }catch (NumberFormatException ex){
                currentVersion = 0.00;
            }
            if(currentVersion > Double.parseDouble(BuildConfig.VERSION_NAME)){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                        String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission,1000);
                    }else {
                        startDownload(text);
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1000 : {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startDownload(gText);
                }
            }
        }
    }

    public void startDownload(String text){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setMessage("There is a version (v" + text + ") available do you want to update now?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
//            https://github.com/laikamanor/mobile-pos-v2/releases/download/v1.17/Atlantic.Bakery.apk
//            https://github.com/laikamanor/mobile-pos-v2/releases/download/v1.17/Atlantic.Bakery.apk
            public void onClick(DialogInterface dialog, int which) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://github.com/laikamanor/mobile-pos-v2/releases/download/v1.17/Atlantic.Grains.apk"));
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                request.setTitle("Download Atlantic Bakery");
                request.setDescription("Downloading File...");
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Atlantic Bakery_v" + text + ".apk");
                manager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
                queueid = manager.enqueue(request);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    public void  isNoInternet(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setTitle("Validation");
        builder.setMessage("You can't connect to the server, You want to Try Again or Go to Offline Mode?");

        builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                btnSignIn.performClick();
            }
        });

        builder.setNegativeButton("Go to Offline Mode", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userid = 0;
                resultToken = "N/A";
                fullName = "Offline Mode";
                whse = "N/A";
                isManagerB = false;
                isManager = (isManagerB ? 1 : 0);
                isSalesB = false;
                isSales = (isSalesB ? 1 : 0);
                isProductionB = false;
                isAdminB = false;
                isProduction = (isProductionB ? 1 : 0);;
                isAdmin = (isAdminB ? 1 : 0);
                saveToken(resultToken);

                myDb.truncateTable();
                myDb2.truncateTable();
                myDb3.truncateTable();
                myDb4.truncateTable();
                myDb5.truncateTable();

                saveLoggedIn();
                Intent intent = new Intent(MainActivity.this, API_Nav2.class);
                startActivity(intent);
                finish();

                dialog.dismiss();
            }
        });

        builder.show();
    }

    public void downloadsJSONS(String token){
        JSONObject jsonObjectData2 = new JSONObject();
        JSONArray jsonArrays = new JSONArray();
//                        DATE
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        try{
            JSONObject jsonObjectItems = new JSONObject();
            jsonObjectItems.put("sURL", "/api/item/getall");
            jsonObjectItems.put("method", "GET");
            jsonObjectItems.put("from_module", "Item");
            jsonObjectItems.put("date_created", currentDate);
            jsonArrays.put(jsonObjectItems);

            JSONObject jsonObjectCustomers = new JSONObject();
            jsonObjectCustomers.put("sURL", "/api/customer/get_all?transtype=sales");
            jsonObjectCustomers.put("method", "GET");
            jsonObjectCustomers.put("from_module", "Customer");
            jsonObjectCustomers.put("date_created", currentDate);
            jsonArrays.put(jsonObjectCustomers);

            JSONObject jsonObjectSalesType= new JSONObject();
            jsonObjectSalesType.put("sURL", "/api/sales/type/get_all");
            jsonObjectSalesType.put("method", "GET");
            jsonObjectSalesType.put("from_module", "Sales Type");
            jsonObjectSalesType.put("date_created", currentDate);
            jsonArrays.put(jsonObjectSalesType);

            JSONObject jsonObjectWarehouse= new JSONObject();
            jsonObjectWarehouse.put("sURL", "/api/whse/get_all");
            jsonObjectWarehouse.put("method", "GET");
            jsonObjectWarehouse.put("from_module", "Warehouse");
            jsonObjectWarehouse.put("date_created", currentDate);
            jsonArrays.put(jsonObjectWarehouse);

            JSONObject jsonObjectBranch = new JSONObject();
            jsonObjectBranch.put("sURL", "/api/branch/get_all");
            jsonObjectBranch.put("method", "GET");
            jsonObjectBranch.put("from_module", "Branch");
            jsonObjectBranch.put("date_created", currentDate);
            jsonArrays.put(jsonObjectBranch);

            JSONObject jsonObjectStock = new JSONObject();
            jsonObjectStock.put("sURL", "/api/inv/whseinv/getall");
            jsonObjectStock.put("method", "GET");
            jsonObjectStock.put("from_module", "Stock");
            jsonObjectStock.put("date_created", currentDate);
            jsonArrays.put(jsonObjectStock);

            JSONObject jsonObjectItemGroup = new JSONObject();
            jsonObjectItemGroup.put("sURL", "/api/item/item_grp/getall");
            jsonObjectItemGroup.put("method", "GET");
            jsonObjectItemGroup.put("from_module", "Item Group");
            jsonObjectItemGroup.put("date_created", currentDate);
            jsonArrays.put(jsonObjectItemGroup);

            jsonObjectData2.put("data", jsonArrays);
        }catch (Exception ex){
            ex.printStackTrace();
            Toast.makeText(getBaseContext(),ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        MyDownloads myDownloads = new MyDownloads(jsonObjectData2, token);
        myDownloads.execute("");
    }

    private class MyDownloads extends AsyncTask<String, Void, String> {
        JSONObject gJData;
        String gToken = "";
        @Override
        protected void onPreExecute() {
            btnSignIn.setText("Downloading Resources...");
            btnSignIn.setEnabled(false);
        }

        public MyDownloads(JSONObject jsonObjectData, String token){
            gJData = jsonObjectData;
            gToken = token;
        }

        @Override
        protected String doInBackground(String... strings) {
//            int counter = 0;

//            HashMap<String, String> modules = new HashMap<String, String>();
//            modules.put("items", )


            try{
                JSONArray jsonArray = gJData.getJSONArray("data");
                String appendJsons = "";
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObjectData = jsonArray.getJSONObject(i);
                    client = new OkHttpClient();
                    SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                    String IPAddress = sharedPreferences2.getString("IPAddress", "");

//                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//                    System.out.println("BODY: " + jsonObjectData.getString("body"));

                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(IPAddress + jsonObjectData.getString("sURL"))
                            .method("GET", null)
                            .addHeader("Authorization", "Bearer " + gToken)
                            .build();
                    Response response = null;
                    try {
                        response = client.newCall(request).execute();
                        String s = response.body().string();
                        if(s.substring(0,1).equals("{")){
                            JSONObject jsonObjectResponse = new JSONObject(s);
                            boolean apiSuccess = jsonObjectResponse.getBoolean("success");
                            if(apiSuccess){
                                String re = jsonObjectResponse.toString();
                                boolean isSuccess = myDb8.insertData(jsonObjectData.getString("sURL"), jsonObjectData.getString("method"), re, jsonObjectData.getString("from_module"), jsonObjectData.getString("date_created"));
                                if(isSuccess){
                                    appendJsons += jsonObjectData.getString("from_module") + " Resources downloaded \n";
                                }else {
                                    appendJsons += jsonObjectData.getString("from_module") + " Resources failed to download \n";
                                }
                            }
                        }else{
                            appendJsons += jsonObjectData.getString("from_module") + " Resources failed to download \n";
                        }

                    } catch (Exception ex) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnSignIn.setText("Sign In");
                                btnSignIn.setEnabled(true);
                                ex.printStackTrace();
                                Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                return appendJsons;
            }catch (Exception ex) {
                ex.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnSignIn.setEnabled(true);
                        ex.printStackTrace();
                        Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (s != null) {
                    Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
                    btnSignIn.setEnabled(true);
                    myDb.truncateTable();
                    myDb2.truncateTable();
                    myDb3.truncateTable();
                    myDb4.truncateTable();
                    myDb5.truncateTable();

                    Intent intent = new Intent(MainActivity.this, API_Nav2.class);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnSignIn.setEnabled(true);
                        ex.printStackTrace();
                        Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}