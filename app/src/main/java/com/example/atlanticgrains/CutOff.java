package com.example.atlanticgrains;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.atlanticgrains.Adapter.CustomExpandableListAdapter;
import com.example.atlanticgrains.Helper.FragmentNavigationManager_CutOff;
import com.example.atlanticgrains.Interface.NavigationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CutOff extends AppCompatActivity {
    prefs_class pc = new prefs_class();
    ui_class uic = new ui_class();
    navigation_class navc = new navigation_class();

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
//    private String[] items;

    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listTitle;
    private Map<String, List<String>> listChild;
    private NavigationManager navigationManager;

    DatabaseHelper myDb;
    Menu menu;

    private OkHttpClient client;
    TextView lblTitle;
    Button btnCutOff;

    String title,hidden_title;
    private long backPressedTime;
    long mLastClickTime;
    DatabaseHelper7 myDb7;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cutoff);
        client = new OkHttpClient();

        myDb = new DatabaseHelper(this);
        myDb7 = new DatabaseHelper7(this);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        expandableListView = (ExpandableListView)findViewById(R.id.navList);
        navigationManager = FragmentNavigationManager_CutOff.getmInstance(this);

        title = getIntent().getStringExtra("title");
        hidden_title = getIntent().getStringExtra("hiddenTitle");
        SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
        String fullName = Objects.requireNonNull(sharedPreferences.getString("fullname", ""));

        View listReaderView = getLayoutInflater().inflate(R.layout.nav_header, null,false);
        TextView txtName = listReaderView.findViewById(R.id.txtName);
        txtName.setText(fullName + " - v" + BuildConfig.VERSION_NAME);
        expandableListView.addHeaderView(listReaderView);

        genData();
        addDrawersItem();
        setupDrawer();

        if(savedInstanceState == null){
            selectFirstItemDefault();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>" + title + " </font>"));


        lblTitle = findViewById(R.id.lblTitle);
        btnCutOff = findViewById(R.id.btnCutOff);
        btnCutOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cutOffFunction();
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);

    }

    public void selectFirstItemDefault(){
        if(navigationManager != null){
            String firstItem = listTitle.get(0);
            navigationManager.showFragment(firstItem);
            getSupportActionBar().setTitle(firstItem);
        }
    }

    public void addDrawersItem(){
        adapter = new CustomExpandableListAdapter(this, listTitle, listChild);
        expandableListView.setAdapter(adapter);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String selectedItem = ((List)listChild.get(listTitle.get(groupPosition)))
                        .get(childPosition).toString();
                getSupportActionBar().setTitle(selectedItem);
                Intent intent;
                if(selectedItem.equals("Received from SAP")){
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Received from SAP");
                    intent.putExtra("hiddenTitle", "API Received from SAP");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Received from System Transfer Item")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Received from System Transfer Item");
                    intent.putExtra("hiddenTitle", "API System Transfer Item");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Manual Received Item")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Received Item");
                    intent.putExtra("hiddenTitle", "API Received Item");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Manual Transfer Item")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Transfer Item");
                    intent.putExtra("hiddenTitle", "API Transfer Item");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Sales")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Sales");
                    intent.putExtra("hiddenTitle", "API Menu Items");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Issue For Production")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Issue For Production");
                    intent.putExtra("hiddenTitle", "API Issue For Production");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Confirm Issue For Production")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Confirm Issue For Production");
                    intent.putExtra("hiddenTitle", "API Confirm Issue For Production");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Received from Production")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Received from Production");
                    intent.putExtra("hiddenTitle", "API Received from Production");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Item Request")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Item Request");
                    intent.putExtra("hiddenTitle", "API Item Request");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Item Request For Transfer")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Item Request For Transfer");
                    intent.putExtra("hiddenTitle", "API Item Request For Transfer");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Production Order List")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Production Order List");
                    intent.putExtra("hiddenTitle", "API Production Order List");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Inventory Count")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Inventory Count");
                    intent.putExtra("hiddenTitle", "API Inventory Count");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Pull out Request")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Pull Out Request");
                    intent.putExtra("hiddenTitle", "API Pull Out Count");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Logout")){
                    onBtnLogout();
                }
                else if(selectedItem.equals("Logs")){
                    intent = new Intent(getBaseContext(), API_SalesLogs.class);
                    intent.putExtra("title", "Inventory Logs");
                    intent.putExtra("hiddenTitle", "API Inventory Logs");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Cut Off")){
                    intent = new Intent(getBaseContext(), CutOff.class);
                    intent.putExtra("title", "Cut Off");
                    intent.putExtra("hiddenTitle", "API Cut Off");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Offline Pending Transactions")){
                    intent = new Intent(getBaseContext(), OfflineList.class);
                    intent.putExtra("title", "Offline Pending Transactions");
                    intent.putExtra("hiddenTitle", "API Offline List");
                    startActivity(intent);
                }
                return true;
            }
        });
    }

    public void setupDrawer(){
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,R.string.open,R.string.close){
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void genData(){
        List<String>title = navc.getTitles(getString(R.string.app_name));
        int iterate = 3;
        int titleIndex = 0;
        while (iterate >= 0){
            listChild.put(title.get(titleIndex),navc.getItem(title.get(titleIndex)));
            titleIndex += 1;
            iterate -= 1;
        }
        listTitle = new ArrayList<>(listChild.keySet());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu item) {
        getMenuInflater().inflate(R.menu.main_menu,item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(mDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    public void changePassword(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(CutOff.this);
        myDialog.setCancelable(false);
        myDialog.setMessage("*Enter Your New Password");
        LinearLayout layout = new LinearLayout(getBaseContext());
        layout.setPadding(40, 0, 40, 0);
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0,0,0,20);
        EditText txtPassword = new EditText(getBaseContext());
        txtPassword.setTextSize(15);
        txtPassword.setGravity(View.TEXT_ALIGNMENT_CENTER);
        txtPassword.setTransformationMethod(new PasswordTransformationMethod());
        txtPassword.setLayoutParams(layoutParams);
        layout.addView(txtPassword);

        CheckBox checkPassword = new CheckBox(getBaseContext());
        checkPassword.setText("Show Password");
        checkPassword.setTextSize(15);
        checkPassword.setGravity(View.TEXT_ALIGNMENT_CENTER);
        checkPassword.setLayoutParams(layoutParams);

        checkPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    txtPassword.setTransformationMethod(null);
                }else{
                    txtPassword.setTransformationMethod(new PasswordTransformationMethod());
                }
                txtPassword.setSelection(txtPassword.length());
            }
        });

        layout.addView(checkPassword);

        myDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(txtPassword.getText().toString().trim().isEmpty()){
                    Toast.makeText(getBaseContext(), "Password field is required", Toast.LENGTH_SHORT).show();
                }else{

                    AlertDialog.Builder builder = new AlertDialog.Builder(CutOff.this);
                    builder.setMessage("Are you sure want to submit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    CutOff.myChangePassword myChangePassword = new CutOff.myChangePassword(txtPassword.getText().toString().trim());
                                    myChangePassword.execute();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
            }
        });

        myDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        myDialog.setView(layout);
        myDialog.show();
    }

    private class myChangePassword extends AsyncTask<String, Void, String> {
        String password = "";
        LoadingDialog loadingDialog = new LoadingDialog(CutOff.this);
        @Override
        protected void onPreExecute() {
            loadingDialog.startLoadingDialog();
        }

        public myChangePassword(String sPassword) {
            password = sPassword;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPAddress = sharedPreferences2.getString("IPAddress", "");

                SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("password", password);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                client = new OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPAddress + "/api/user/change_pass")
                        .method("PUT", body)
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = null;

                response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismissDialog();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if(s != null) {
                    JSONObject jsonObjectResponse = new JSONObject(s);
                    loadingDialog.dismissDialog();
                    Toast.makeText(getBaseContext(), jsonObjectResponse.getString("message"), Toast.LENGTH_SHORT).show();

                    if(jsonObjectResponse.getBoolean("success")){

                        AlertDialog.Builder builder = new AlertDialog.Builder(CutOff.this);
                        builder.setMessage("We redirect you to Login Page")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pc.loggedOut(CutOff.this);
                                        pc.removeToken(CutOff.this);
                                        startActivity(uic.goTo(CutOff.this, MainActivity.class));
                                        finish();
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                }
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismissDialog();
                    }
                });
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        uiCutOff();
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    public  void onBtnLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pc.loggedOut(CutOff.this);
                        pc.removeToken(CutOff.this);
                        startActivity(uic.goTo(CutOff.this, MainActivity.class));
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    public void uiCutOff() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        wait(10);
                    } catch (InterruptedException ex) {
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // create your json here
//                                JSONObject jsonObject = new JSONObject();
//                                try {
//                                    jsonObject.put("username", us);
//                                    jsonObject.put("password", ps);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//
//                                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                                SharedPreferences sharedPreferences0 = getSharedPreferences("TOKEN", MODE_PRIVATE);
                                String token = sharedPreferences0.getString("token", "");

                                SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
                                String whseCode = sharedPreferences2.getString("whse", "");

                                SharedPreferences sharedPreferences3 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                                String IPaddress = sharedPreferences3.getString("IPAddress", "");
//                                System.out.println(IPaddress + "/api/whse/get_all?whsecode=" + whseCode);
                                okhttp3.Request request = new okhttp3.Request.Builder()
                                        .url(IPaddress + "/api/whse/get_all?whsecode=" + whseCode)
                                        .addHeader("Authorization", "Bearer " + token)
                                        .addHeader("Content-Type", "application/json")
                                        .method("GET", null)
                                        .build();
                                client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
                                        CutOff.this.runOnUiThread(new Runnable() {
                                            public void run() {
                                                Handler handler = new Handler();
                                                LoadingDialog loadingDialog = new LoadingDialog(CutOff.this);
                                                loadingDialog.startLoadingDialog();
                                                Runnable runnable = new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        synchronized (this) {
                                                            try {
                                                                wait(10);
                                                            } catch (InterruptedException ignored) {

                                                            }
                                                            handler.post(() -> {
                                                                try {
                                                                    assert response.body() != null;
                                                                    String result = response.body().string();
//                                                                                    System.out.println(result);
                                                                    JSONObject jsonObject1 = new JSONObject(result);
                                                                    if (response.isSuccessful()) {


                                                                        if (jsonObject1.getBoolean("success")) {
                                                                            JSONArray jsonArray = jsonObject1.getJSONArray("data");
                                                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                                                JSONObject jsonObjectData = jsonArray.getJSONObject(i);
                                                                                boolean isCutOff = jsonObjectData.getBoolean("cutoff");
                                                                                String cutOffText = isCutOff ? "Enabled" : "Disabled";
                                                                                String cutOffTextReverse = !isCutOff ? "Enabled" : "Disabled";
                                                                                lblTitle.setText("Cut Off Status: " + cutOffText);
                                                                                btnCutOff.setText( cutOffTextReverse+ " Cut Off");
                                                                            }
                                                                        } else {
                                                                            System.out.println(jsonObject1.getString("message"));
                                                                        }

                                                                    } else {
                                                                        System.out.println(jsonObject1.getString("message"));
                                                                    }
                                                                } catch (Exception ex) {
                                                                    ex.printStackTrace();
                                                                }
                                                            });
                                                        }
                                                        runOnUiThread(loadingDialog::dismissDialog);

                                                    }
                                                };
                                                Thread thread = new Thread(runnable);
                                                thread.start();
                                            }
                                        });
                                    }
                                });
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }

            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
//                        finish();
    }

    public void cutOffFunction(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String cutOffTitle = btnCutOff.getText().toString();
        cutOffTitle = cutOffTitle.replace(" Cut Off","");
        builder.setMessage("Are you sure want to " + cutOffTitle + " Cut Off?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                synchronized (this) {
                                    try {
                                        wait(10);
                                    } catch (InterruptedException ex) {
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                // create your json here
//                                JSONObject jsonObject = new JSONObject();
//                                try {
//                                    jsonObject.put("username", us);
//                                    jsonObject.put("password", ps);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//
//                                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                                                SharedPreferences sharedPreferences0 = getSharedPreferences("TOKEN", MODE_PRIVATE);
                                                String token = sharedPreferences0.getString("token", "");

                                                SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
                                                String whseCode = sharedPreferences2.getString("whse", "");

                                                SharedPreferences sharedPreferences3 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                                                String IPaddress = sharedPreferences3.getString("IPAddress", "");
                                                System.out.println(IPaddress + "/api/whse/get_all?whsecode=" + whseCode);
                                                Request request = new Request.Builder()
                                                        .url(IPaddress + "/api/whse/get_all?whsecode=" + whseCode)
                                                        .addHeader("Authorization", "Bearer " + token)
                                                        .addHeader("Content-Type", "application/json")
                                                        .method("GET", null)
                                                        .build();
                                                client.newCall(request).enqueue(new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {
                                                        CutOff.this.runOnUiThread(new Runnable() {
                                                            public void run() {
                                                                Handler handler = new Handler();
                                                                LoadingDialog loadingDialog = new LoadingDialog(CutOff.this);
                                                                loadingDialog.startLoadingDialog();
                                                                Runnable runnable = new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        synchronized (this) {
                                                                            try {
                                                                                wait(10);
                                                                            } catch (InterruptedException ignored) {

                                                                            }
                                                                            handler.post(() -> {
                                                                                try {
                                                                                    assert response.body() != null;
                                                                                    String result = response.body().string();
//                                                                                    System.out.println(result);
                                                                                    JSONObject jsonObject1 = new JSONObject(result);
                                                                                    if (response.isSuccessful()) {
                                                                                        if (jsonObject1.getBoolean("success")) {
                                                                                            JSONArray jsonArray = jsonObject1.getJSONArray("data");
                                                                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                                                                JSONObject jsonObjectData = jsonArray.getJSONObject(i);
//                                                                                                int whseID = jsonObjectData.getInt("id");
                                                                                                boolean isCutOff = jsonObjectData.getBoolean("cutoff");

                                                                                                String URL2 = IPaddress + "/api/whse/cutoff";
                                                                                                JSONObject jsonObjectHeader = new JSONObject();
                                                                                                jsonObjectHeader.put("cutoff", !isCutOff);
                                                                                                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                                                                                                RequestBody body = RequestBody.create(JSON, jsonObjectHeader.toString());
                                                                                                System.out.println(jsonObjectHeader);
                                                                                                Request request = new Request.Builder()
                                                                                                        .url(URL2)
                                                                                                        .method("PUT", body)
                                                                                                        .addHeader("Authorization", "Bearer " + token)
                                                                                                        .addHeader("Content-Type", "application/json")
                                                                                                        .build();
                                                                                                client.newCall(request).enqueue(new Callback() {
                                                                                                    @Override
                                                                                                    public void onFailure(Call call, IOException e) {
                                                                                                        runOnUiThread(new Runnable() {
                                                                                                            @Override
                                                                                                            public void run() {
                                                                                                                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        });
                                                                                                    }

                                                                                                    @Override
                                                                                                    public void onResponse(Call call, Response response) {
                                                                                                        try {
                                                                                                            String sResult = response.body().string();
                                                                                                            if(response.isSuccessful()){
                                                                                                                runOnUiThread(new Runnable() {
                                                                                                                    @Override
                                                                                                                    public void run() {
                                                                                                                        try {
                                                                                                                            JSONObject jsonObjectResponse = new JSONObject(sResult);
                                                                                                                            Toast.makeText(getBaseContext(), jsonObjectResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                                                                                                            uiCutOff();
                                                                                                                        } catch (JSONException e) {
                                                                                                                            e.printStackTrace();
                                                                                                                            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                                                                                                                        }
                                                                                                                    }
                                                                                                                });

                                                                                                            }else{
                                                                                                                runOnUiThread(new Runnable() {
                                                                                                                    @Override
                                                                                                                    public void run() {
                                                                                                                        try {
                                                                                                                            if(sResult.substring(0,1).equals("{")){
                                                                                                                                JSONObject jsonObjectResponse = new JSONObject(sResult);
                                                                                                                                Toast.makeText(getBaseContext(), "Error \n" + jsonObjectResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                                                                                                            }else{
                                                                                                                                Toast.makeText(getBaseContext(),sResult, Toast.LENGTH_SHORT).show();
                                                                                                                            }
//                                                                                                                            JSONObject jsonObjectResponse = new JSONObject(sResult);

                                                                                                                        } catch (Exception e) {
                                                                                                                            e.printStackTrace();
                                                                                                                            Toast.makeText(getBaseContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                        }
                                                                                                                    }
                                                                                                                });
                                                                                                            }

                                                                                                        } catch (Exception ex) {
                                                                                                            ex.printStackTrace();
                                                                                                            runOnUiThread(new Runnable() {
                                                                                                                @Override
                                                                                                                public void run() {
                                                                                                                    Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                }
                                                                                                            });
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        }
                                                                                        else{
                                                                                            System.out.println(jsonObject1.getString("message"));
                                                                                        }

                                                                                    }else{
                                                                                        System.out.println(jsonObject1.getString("message"));
                                                                                    }
                                                                                } catch (Exception ex) {
                                                                                    ex.printStackTrace();
                                                                                    Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        }
                                                                        runOnUiThread(loadingDialog::dismissDialog);

                                                                    }
                                                                };
                                                                Thread thread = new Thread(runnable);
                                                                thread.start();
                                                            }
                                                        });
                                                    }
                                                });
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                                Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }

                            }
                        };
                        Thread thread = new Thread(runnable);
                        thread.start();
//                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
