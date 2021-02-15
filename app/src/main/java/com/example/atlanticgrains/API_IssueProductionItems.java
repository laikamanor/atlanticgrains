package com.example.atlanticgrains;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.atlanticgrains.Adapter.CustomExpandableListAdapter;
import com.example.atlanticgrains.Helper.FragmentNavigationManager_API_IssueProductionItems;
import com.example.atlanticgrains.Interface.NavigationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class API_IssueProductionItems extends AppCompatActivity {
    ProgressBar progressBar;
    private RequestQueue mQueue;
    TextView lblReference;
    Button btnDone;

    String title, hidden_title;

    DatabaseHelper4 myDb4;
    DatabaseHelper3 myDb3;
    DatabaseHelper myDb;
    DatabaseHelper8 myDb8;
    DatabaseHelper7 myDb7;
    DatabaseHelper9 myDb9;

    DecimalFormat df = new DecimalFormat("#,###");

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

    long mLastClickTime = 0;
    private OkHttpClient client;

    Menu menu;

    JSONObject globalJsonObject;
    Button btnBack;
    private long backPressedTime;
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_p_i__issue_production_items);
        progressBar = findViewById(R.id.progWait);
        mQueue = Volley.newRequestQueue(this);
        btnDone = findViewById(R.id.btnDone);
        lblReference = findViewById(R.id.lblReference);
        lblReference.setText(getIntent().getStringExtra("reference"));
        myDb4 = new DatabaseHelper4(this);
        myDb3 = new DatabaseHelper3(this);
        myDb = new DatabaseHelper(this);
        myDb8 = new DatabaseHelper8(this);
        myDb7 = new DatabaseHelper7(this);
        myDb9 = new DatabaseHelper9(this);
        btnBack = findViewById(R.id.btnBack);

        client = new OkHttpClient();

        globalJsonObject = new JSONObject();

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        expandableListView = (ExpandableListView)findViewById(R.id.navList);
        navigationManager = FragmentNavigationManager_API_IssueProductionItems.getmInstance(this);

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

        title = getIntent().getStringExtra("title");
        hidden_title = getIntent().getStringExtra("hiddenTitle");
        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>" + title + " </font>"));
        btnDone.setText((hidden_title.equals("API Issue For Production") ? "Create Issue" : "Confirm Issue"));

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDb9.truncateTable();
                Intent intent;
                intent = new Intent(getBaseContext(), APIReceived.class);
                intent.putExtra("title", title);
                intent.putExtra("hiddenTitle", hidden_title);
                startActivity(intent);
                finish();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if(hidden_title.equals("API Issue For Production")){
                    createIssue();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(API_IssueProductionItems.this);
                    builder.setMessage("Are you sure want to confirm issue?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    confirmIssue();
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
        loadData();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Intent a = new Intent(Intent.ACTION_MAIN);
//        a.addCategory(Intent.CATEGORY_HOME);
//        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(a);
//    }

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
                    finish();
                }
                else if(selectedItem.equals("Received from System Transfer Item")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Received from System Transfer Item");
                    intent.putExtra("hiddenTitle", "API System Transfer Item");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Manual Received Item")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Received Item");
                    intent.putExtra("hiddenTitle", "API Received Item");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Manual Transfer Item")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Transfer Item");
                    intent.putExtra("hiddenTitle", "API Transfer Item");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Sales")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Sales");
                    intent.putExtra("hiddenTitle", "API Menu Items");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Issue For Production")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Issue For Production");
                    intent.putExtra("hiddenTitle", "API Issue For Production");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Confirm Issue For Production")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Confirm Issue For Production");
                    intent.putExtra("hiddenTitle", "API Confirm Issue For Production");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Received from Production")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Received from Production");
                    intent.putExtra("hiddenTitle", "API Received from Production");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Item Request")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Item Request");
                    intent.putExtra("hiddenTitle", "API Item Request");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Item Request For Transfer")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Item Request For Transfer");
                    intent.putExtra("hiddenTitle", "API Item Request For Transfer");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Production Order List")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Production Order List");
                    intent.putExtra("hiddenTitle", "API Production Order List");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Inventory Count")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Inventory Count");
                    intent.putExtra("hiddenTitle", "API Inventory Count");
                    startActivity(intent);
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Pull out Request")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Pull Out Request");
                    intent.putExtra("hiddenTitle", "API Pull Out Count");
                    startActivity(intent);
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Logout")){
                    onBtnLogout();
                }
                else if(selectedItem.equals("Logs")){
                    intent = new Intent(getBaseContext(), API_SalesLogs.class);
                    intent.putExtra("title", "Inventory Logs");
                    intent.putExtra("hiddenTitle", "API Inventory Logs");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Cut Off")){
                    intent = new Intent(getBaseContext(), CutOff.class);
                    intent.putExtra("title", "Cut Off");
                    intent.putExtra("hiddenTitle", "API Cut Off");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Inventory Confirmation")){
                    intent = new Intent(getBaseContext(), API_InventoryConfirmation.class);
                    intent.putExtra("title", "Inv. and P.O Count Confirmation");
                    intent.putExtra("hiddenTitle", "API Inventory Count Confirmation");
                    startActivity(intent);
                    finish();
                }
                else if(selectedItem.equals("Change Password")){
                    changePassword();
                }
                else if(selectedItem.equals("Offline Pending Transactions")){
                    intent = new Intent(getBaseContext(), OfflineList.class);
                    intent.putExtra("title", "Offline Pending Transactions");
                    intent.putExtra("hiddenTitle", "API Offline List");
                    startActivity(intent);
                    finish();
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
        listChild = new TreeMap<>();
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

    public void confirmIssue(){
        int id = getIntent().getIntExtra("id",0);
        myConfirmIssue myConfirmIssue = new myConfirmIssue(id);
        myConfirmIssue.execute("");
    }

    public void createIssue(){
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(API_IssueProductionItems.this);
        myDialog.setCancelable(false);
        LinearLayout layout = new LinearLayout(getBaseContext());
        layout.setPadding(40, 40, 40, 40);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParamsBranch = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView lblSAPNumber = new TextView(getBaseContext());
        lblSAPNumber.setText("SAP #:");
        lblSAPNumber.setTextColor(Color.rgb(0,0,0));
        lblSAPNumber.setTextSize(15);
        lblSAPNumber.setGravity(View.TEXT_ALIGNMENT_CENTER);
        lblSAPNumber.setLayoutParams(layoutParamsBranch);
        layout.addView(lblSAPNumber);

        EditText txtSAPNumber = new EditText(getBaseContext());
        txtSAPNumber.setTextSize(15);
        txtSAPNumber.setGravity(View.TEXT_ALIGNMENT_CENTER);
        txtSAPNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtSAPNumber.setLayoutParams(layoutParamsBranch);
        layout.addView(txtSAPNumber);

        TextView lblRemarks = new TextView(getBaseContext());
        lblRemarks.setTextColor(Color.rgb(0,0,0));
        lblRemarks.setText("*Remarks:");
        lblRemarks.setTextSize(15);
        lblRemarks.setGravity(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(lblRemarks);

        EditText txtRemarks = new EditText(getBaseContext());
        txtRemarks.setTextColor(Color.rgb(0,0,0));
        txtRemarks.setTextSize(15);
        txtRemarks.setGravity(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(txtRemarks);

        myDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder builder = new AlertDialog.Builder(API_IssueProductionItems.this);
                builder.setMessage("Are you sure want to submit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(txtRemarks.getText().toString().trim().isEmpty()){
                                    Toast.makeText(getBaseContext(), "Remarks field is required", Toast.LENGTH_SHORT).show();
                                }else{
                                    int sapNumber = txtSAPNumber.getText().toString().isEmpty() ? 0 : Integer.parseInt(txtSAPNumber.getText().toString().trim());
                                    int id = getIntent().getIntExtra("id",0);
                                    String remarkss = txtRemarks.getText().toString().trim();
                                    String response = "";
                                    Cursor cursor = myDb9.getAllData();
                                    if(cursor != null){
                                        if(cursor.moveToNext()){
                                            if(cursor.getString(4).equals("Issue For Production")){
                                                response = cursor.getString(3);
                                            }
                                        }
                                    }
                                    myInsert my = new myInsert(id,sapNumber, remarkss,response);
                                    my.execute("");
                                }
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
        });

        myDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        myDialog.setView(layout);
        myDialog.show();
    }

    private class myConfirmIssue extends AsyncTask<String, Void, String> {
        int id = 0;
        LoadingDialog loadingDialog = new LoadingDialog(API_IssueProductionItems.this);

        @Override
        protected void onPreExecute() {
            loadingDialog.startLoadingDialog();
        }

        public myConfirmIssue(int sId) {
            id = sId;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPAddress = sharedPreferences2.getString("IPAddress", "");

                SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObjectBody = new JSONObject();
                RequestBody body = RequestBody.create(JSON, jsonObjectBody.toString());
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPAddress + "/api/production/issue_for_prod/confirm/" + id)
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
                        ex.printStackTrace();
                        Toast.makeText(getBaseContext(), "Error \n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismissDialog();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                System.out.println("SSSSS: " + s);
                if (s != null) {
                    JSONObject jsonObjectResponse = new JSONObject(s);
                    loadingDialog.dismissDialog();
                    Toast.makeText(getBaseContext(), jsonObjectResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    if (jsonObjectResponse.getBoolean("success")) {
                        Toast.makeText(getBaseContext(), "Message \n" + jsonObjectResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        myDb9.truncateTable();
                        Intent intent = new Intent(getBaseContext(), APIReceived.class);
                        intent.putExtra("title", title);
                        intent.putExtra("hiddenTitle", hidden_title);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(getBaseContext(), "Error \n" + jsonObjectResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getBaseContext(), "Error \n" + s, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ex.printStackTrace();
                        Toast.makeText(getBaseContext(), "Error \n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismissDialog();
                    }
                });
            }
        }
    }

    private class myInsert extends AsyncTask<String, Void, String> {
        int id = 0, sapNumber = 0;
        String remarks = "",response = "";
        LoadingDialog loadingDialog = new LoadingDialog(API_IssueProductionItems.this);

        @Override
        protected void onPreExecute() {
            loadingDialog.startLoadingDialog();
        }

        public myInsert(int sId, int sSapNumber, String sRemarks, String sResponse) {
            id = sId;
            sapNumber = sSapNumber;
            remarks = sRemarks;
            response = sResponse;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPAddress = sharedPreferences2.getString("IPAddress", "");

                SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String currentDate = sdf.format(new Date());

                JSONObject jsonObjectBody = new JSONObject();
                JSONObject jsonObjectHeader = new JSONObject();
                jsonObjectHeader.put("prod_order_id", getIntent().getIntExtra("id", 0));
                jsonObjectHeader.put("transdate", currentDate);
                jsonObjectHeader.put("sap_number", (sapNumber <= 0 ? JSONObject.NULL : sapNumber));
                jsonObjectHeader.put("remarks", (remarks.isEmpty() ? JSONObject.NULL : remarks));

                JSONObject jsonObjectResponse = new JSONObject(response);
                JSONObject jsonObjectData = jsonObjectResponse.getJSONObject("data");
                JSONArray jsonArrayBase = jsonObjectData.getJSONArray("base");
                JSONArray jsonArrayForIssue = jsonObjectData.getJSONArray("for_issue");
                String whseCode = "";
//                base
                for (int i = 0; i < jsonArrayBase.length(); i++) {
                    JSONObject jsonObjectBase = jsonArrayBase.getJSONObject(i);
                    whseCode = jsonObjectBase.getString("whsecode");
                }
                jsonObjectHeader.put("whsecode", whseCode);
                jsonObjectBody.put("header", jsonObjectHeader);
//              for issue
                JSONArray jsonArrayRows = new JSONArray();
                for (int i = 0; i < jsonArrayForIssue.length(); i++) {
                    JSONObject jsonObjectRows = new JSONObject();
                    JSONObject jsonObjectForIssue = jsonArrayForIssue.getJSONObject(i);
                    String itemCode = jsonObjectForIssue.getString("item_code"),
                            uom = jsonObjectForIssue.getString("uom");
                    double quantity = jsonObjectForIssue.getDouble("quantity");
                    jsonObjectRows.put("item_code", itemCode);
                    jsonObjectRows.put("quantity", quantity);
                    jsonObjectRows.put("uom", uom);
                    jsonArrayRows.put(jsonObjectRows);
                }
                jsonObjectBody.put("rows", jsonArrayRows);
                System.out.println(jsonObjectBody);
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, jsonObjectBody.toString());

                client = new OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPAddress + "/api/production/issue_for_prod/new")
                        .method("POST", body)
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
                        ex.printStackTrace();
                        Toast.makeText(getBaseContext(), "Error \n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismissDialog();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                System.out.println("SSSSS: " + s);
                if (s != null) {
                    JSONObject jsonObjectResponse = new JSONObject(s);
                    loadingDialog.dismissDialog();
                    Toast.makeText(getBaseContext(), jsonObjectResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    if (jsonObjectResponse.getBoolean("success")) {
                        Toast.makeText(getBaseContext(), "Message \n" + jsonObjectResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        myDb9.truncateTable();
                        Intent intent = new Intent(getBaseContext(), APIReceived.class);
                        intent.putExtra("title", title);
                        intent.putExtra("hiddenTitle", hidden_title);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(getBaseContext(), "Error \n" + jsonObjectResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getBaseContext(), "Error \n" + s, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ex.printStackTrace();
                        Toast.makeText(getBaseContext(), "Error \n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismissDialog();
                    }
                });
            }
        }
    }



    public void changePassword(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(API_IssueProductionItems.this);
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(API_IssueProductionItems.this);
                    builder.setMessage("Are you sure want to submit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    API_IssueProductionItems.myChangePassword myChangePassword = new API_IssueProductionItems.myChangePassword(txtPassword.getText().toString().trim());
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
        LoadingDialog loadingDialog = new LoadingDialog(API_IssueProductionItems.this);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(API_IssueProductionItems.this);
                        builder.setMessage("We redirect you to Login Page")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pc.loggedOut(API_IssueProductionItems.this);
                                        pc.removeToken(API_IssueProductionItems.this);
                                        startActivity(uic.goTo(API_IssueProductionItems.this, MainActivity.class));
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


    public void onBtnLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    pc.loggedOut(API_IssueProductionItems.this);
                    pc.removeToken(API_IssueProductionItems.this);
                    startActivity(uic.goTo(API_IssueProductionItems.this, MainActivity.class));
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void loadData(){
        Cursor cursor = myDb9.getAllData();
        if(cursor != null){
            if(cursor.moveToNext()){
                if(cursor.getString(4).equals("Issue For Production")){
                    MyAppendData myAppendData = new MyAppendData(cursor.getString(3));
                    myAppendData.execute("");
                }
            }
        }

    }

    private class MyAppendData extends AsyncTask<String, Void, String> {
        String sResult = "";
        public MyAppendData(String result){
            sResult = result;
        }

        @Override
        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                @SuppressLint({"ResourceType", "SetTextI18n"})
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        protected String doInBackground(String... strings) {
            return sResult;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObjectResponse = new JSONObject();
                if (!globalJsonObject.toString().equals("{}") && !API_ItemInfo.isSubmit) {
                    jsonObjectResponse = globalJsonObject;
                } else {
                    globalJsonObject = new JSONObject(s);
                    jsonObjectResponse = new JSONObject(s);
                }

                if (jsonObjectResponse.getBoolean("success")) {
                    JSONObject jsonObjectData = jsonObjectResponse.getJSONObject("data");
                    JSONArray jsonArrayBase = jsonObjectData.getJSONArray("base");
                    JSONArray jsonArrayForIssue = jsonObjectData.getJSONArray("for_issue");
                    runOnUiThread(new Runnable() {
                        @SuppressLint({"ResourceType", "SetTextI18n"})
                        @Override
                        public void run() {
                            try {
                                ArrayList<String> myReference = new ArrayList<String>();
                                ArrayList<String> myID = new ArrayList<String>();
                                ArrayList<String> myAmount = new ArrayList<String>();

                                ArrayList<String> myReference2 = new ArrayList<String>();
                                ArrayList<String> myID2 = new ArrayList<String>();
                                ArrayList<String> myAmount2 = new ArrayList<String>();
                                for (int i = 0; i < jsonArrayBase.length(); i++) {
                                    JSONObject jsonObjectBase = jsonArrayBase.getJSONObject(i);
                                    int id = jsonObjectBase.getInt("id");
                                    String itemCode = jsonObjectBase.getString("item_code"),uom = jsonObjectBase.getString("uom");
                                    double plannedQty = jsonObjectBase.getDouble("planned_qty");
//                                    base += "id: " + id + "\n Item Code: " + itemCode + "\n Planned Qty: " + plannedQty;
                                    myReference.add(itemCode);
                                    myID.add("0");
                                    myAmount.add(plannedQty + uom);
                                }
                                for (int i = 0; i < jsonArrayForIssue.length(); i++) {
                                    JSONObject jsonObjectForIssue = jsonArrayForIssue.getJSONObject(i);
                                    String itemCode = jsonObjectForIssue.getString("item_code"),uom = jsonObjectForIssue.getString("uom");
                                    double plannedQty = jsonObjectForIssue.getDouble("quantity");
                                    myReference2.add(itemCode);
                                    myID2.add("0");
                                    myAmount2.add(plannedQty + " " + uom);
                                }
                                MyAdapter adapter = new MyAdapter(getBaseContext(), myReference, myID,myAmount);
                                ListView listView = findViewById(R.id.listView1);
                                listView.setAdapter(adapter);
                                MyAdapter adapter2 = new MyAdapter(getBaseContext(), myReference2, myID2,myAmount2);
                                ListView listView2 = findViewById(R.id.listView2);
                                listView2.setAdapter(adapter2);
                            } catch (Exception ex) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.GONE);
                                        ex.printStackTrace();
                                        Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    String msg = jsonObjectResponse.getString("message");
                    if (msg.equals("Token is invalid")) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_IssueProductionItems.this);
                        builder.setCancelable(false);
                        builder.setMessage("Your session is expired. Please login again.");
                        builder.setPositiveButton("OK", (dialog, which) -> {
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            pc.loggedOut(API_IssueProductionItems.this);
                            pc.removeToken(API_IssueProductionItems.this);
                            startActivity(uic.goTo(API_IssueProductionItems.this, MainActivity.class));
                            finish();
                            dialog.dismiss();
                        });
                        builder.show();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (JSONException ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getBaseContext(), "Front-end Error: \n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        ex.printStackTrace();
                    }
                });
            }

            runOnUiThread(new Runnable() {
                @SuppressLint({"ResourceType", "SetTextI18n"})
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    class MyAdapter extends ArrayAdapter<String> {
        Context rContext;
        ArrayList<String> myReference;
        ArrayList<String> myIds;
        ArrayList<String> amounts;

        MyAdapter(Context c, ArrayList<String> reference, ArrayList<String> id,ArrayList<String> amount) {
            super(c, R.layout.custom_list_view_sales_logs, R.id.txtReference, reference);
            this.rContext = c;
            this.myReference = reference;
            this.myIds = id;
            this.amounts = amount;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.custom_list_view_sales_logs, parent, false);
            TextView textView1 = row.findViewById(R.id.txtReference);
            TextView textView2 = row.findViewById(R.id.txtIDs);
            TextView textView3 = row.findViewById(R.id.txtAmount);
            textView1.setText(myReference.get(position));
            textView2.setText(myIds.get(position));
            textView3.setText(amounts.get(position));
            textView2.setVisibility(View.GONE);
            textView3.setGravity(Gravity.LEFT);
            textView3.setVisibility((amounts.get(position).equals("") ? View.GONE : View.VISIBLE));

            return row;
        }
    }

    public String cutWord(String value, int limit){
        String result;
        int limitTo = limit - 3;
        result = (value.length() > limit ? value.substring(0, limitTo) + "..." : value);
        return result;
    }

    public ArrayAdapter<String> fillItems(List<String> items){
        return new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, items);
    }
}