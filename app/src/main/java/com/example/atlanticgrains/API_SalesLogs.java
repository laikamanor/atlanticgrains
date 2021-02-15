package com.example.atlanticgrains;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.atlanticgrains.Adapter.CustomExpandableListAdapter;
import com.example.atlanticgrains.Helper.FragmentNavigationManager_API_SalesLogs;
import com.example.atlanticgrains.Interface.NavigationManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class API_SalesLogs extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    prefs_class pc = new prefs_class();
    ui_class uic = new ui_class();
    navigation_class navc = new navigation_class();
    DatabaseHelper myDb;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
//    private String[] items;

    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listTitle;
    private Map<String, List<String>> listChild;
    private NavigationManager navigationManager;

    private OkHttpClient client;
    Menu menu;
    ListView listView;
    String title,hidden_title;
    TextView txtTotal,txtUser;
    Spinner cmbType,cmbStatus,cmbUser;

    Button btnPickDate;
    TextView lblDate;
    CheckBox checkSAP;
    ProgressBar progressBar;
    JSONArray jsonArrayUsers = new JSONArray();
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_p_i_sales_logs);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        cmbType = findViewById(R.id.cmbType);
        lblDate= findViewById(R.id.txtDate);
        txtTotal = findViewById(R.id.txtTotal);
        txtUser = findViewById(R.id.lblUser);
        checkSAP = findViewById(R.id.checkSAP);
        cmbStatus = findViewById(R.id.cmbStatus);
        cmbUser = findViewById(R.id.cmbUser);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        lblDate.setText(currentDateandTime);

        checkSAP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyData myData = new MyData(cmbType.getSelectedItem().toString());
                myData.execute();
            }
        });

        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDate();
            }
        });
        myDb = new DatabaseHelper(this);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        expandableListView = (ExpandableListView)findViewById(R.id.navList);
        navigationManager = FragmentNavigationManager_API_SalesLogs.getmInstance(this);

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
        listView = findViewById(R.id.listView);

        ArrayList<String> types = new ArrayList<>();
        types.add("Received Transactions");
        types.add("Transfer Transactions");
        types.add("Sales Transactions");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbType.setAdapter(adapter);
        cmbType.setSelection(0);
        cmbType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0){
                    txtTotal.setVisibility(View.GONE);
                    cmbUser.setVisibility(View.GONE);
                    txtUser.setVisibility(View.GONE);
                    MyData myData = new MyData("Received Transactions");
                    myData.execute();
                }else if(i == 1){
                    cmbUser.setVisibility(View.GONE);
                    txtUser.setVisibility(View.GONE);
                    txtTotal.setVisibility(View.GONE);
                    MyData myData = new MyData("Transfer Transactions");
                    myData.execute();
                }else if(i == 2){
                    txtTotal.setVisibility(View.VISIBLE);
                    cmbUser.setVisibility(View.VISIBLE);
                    txtUser.setVisibility(View.VISIBLE);
                    loadUsers();
                    MyData myData = new MyData("Sales Transactions");
                    myData.execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayList<String> status = new ArrayList<>();
        status.add("All");
        status.add("Open");
        status.add("Closed");
        status.add("Cancelled");
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, status);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbStatus.setAdapter(adapter1);
        cmbStatus.setSelection(0);

        cmbStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                MyData myData = new MyData(cmbType.getSelectedItem().toString());
                myData.execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cmbUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MyData myData = new MyData(cmbType.getSelectedItem().toString());
                myData.execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
                    startActivity(intent);
                }
                else if(selectedItem.equals("Pull out Request")) {
                    intent = new Intent(getBaseContext(), APIReceived.class);
                    intent.putExtra("title", "Pull Out Request");
                    intent.putExtra("hiddenTitle", "API Pull Out Count");
                    startActivity(intent);
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
                else if(selectedItem.equals("Inventory Confirmation")){
                    intent = new Intent(getBaseContext(), API_InventoryConfirmation.class);
                    intent.putExtra("title", "Inv. and P.O Count Confirmation");
                    intent.putExtra("hiddenTitle", "API Inventory Count Confirmation");
                    startActivity(intent);
                }
                else if(selectedItem.equals("Change Password")){
                    changePassword();
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


    public void loadUsers(){
        List<String> tenderTypes = new ArrayList<>();

        SharedPreferences sharedPreferences0 = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = sharedPreferences0.getString("token", "");

        SharedPreferences sharedPreferences3 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPAddress = sharedPreferences3.getString("IPAddress", "");

        SharedPreferences sharedPreferences4 = getSharedPreferences("LOGIN", MODE_PRIVATE);
        String userID = sharedPreferences4.getString("userid", "");
        String isManager = sharedPreferences4.getString("isManager", "");
        String isAdmin = sharedPreferences4.getString("isAdmin", "");
        String branchCode = sharedPreferences4.getString("branch", "");
        String sURL = "";
        if(Integer.parseInt(isManager.trim()) > 0 || Integer.parseInt(isAdmin.trim()) > 0){
            sURL = "?isSales=1&branch=" + branchCode;
        }else {
            sURL = "?id=" + userID;
        }
        System.out.println("RESS: " + IPAddress + "/api/auth/user/get_all" + sURL);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(IPAddress + "/api/auth/user/get_all" + sURL)
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
                API_SalesLogs.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Handler handler = new Handler();
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
                                                    System.out.println("FINAL: " + isManager);
                                                    if(Integer.parseInt(isManager.trim()) > 0 || Integer.parseInt(isAdmin.trim()) > 0){
                                                        tenderTypes.add("All");
                                                    }
                                                    for (int ii = 0; ii < jsonArray.length(); ii++) {
                                                        JSONObject jsonObject = jsonArray.getJSONObject(ii);
                                                        tenderTypes.add(jsonObject.getString("username"));
                                                        JSONObject jsonObjectUsers = new JSONObject();
                                                        jsonObjectUsers.put("id", jsonObject.getInt("id"));
                                                        jsonObjectUsers.put("username", jsonObject.getString("username"));
                                                        jsonArrayUsers.put(jsonObjectUsers);
                                                    }
                                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(API_SalesLogs.this, android.R.layout.simple_spinner_item, tenderTypes);
                                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                    cmbUser.setAdapter(adapter);
                                                }else {
                                                    String msg = jsonObject1.getString("message");
                                                    Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                                                }

                                            } else {
                                                System.out.println(jsonObject1.getString("message"));
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    });
                                }
                            }
                        };
                        Thread thread = new Thread(runnable);
                        thread.start();
                    }
                });
            }
        });
    }

    public void changePassword(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(API_SalesLogs.this);
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(API_SalesLogs.this);
                    builder.setMessage("Are you sure want to submit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    API_SalesLogs.myChangePassword myChangePassword = new API_SalesLogs.myChangePassword(txtPassword.getText().toString().trim());
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
        LoadingDialog loadingDialog = new LoadingDialog(API_SalesLogs.this);
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(API_SalesLogs.this);
                        builder.setMessage("We redirect you to Login Page")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pc.loggedOut(API_SalesLogs.this);
                                        pc.removeToken(API_SalesLogs.this);
                                        startActivity(uic.goTo(API_SalesLogs.this, MainActivity.class));
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


    public void showDate(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePickerDialog.OnDateSetListener) this, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        lblDate.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
        MyData myData = new MyData(cmbType.getSelectedItem().toString());
        myData.execute();
    }

    private class MyData extends AsyncTask<String, Void, String> {
        String type = "";
        public MyData(String pType){
            type = pType;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                client = new OkHttpClient();
                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPAddress = sharedPreferences2.getString("IPAddress", "");

                SharedPreferences sharedPreferences0 = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = sharedPreferences0.getString("token", "");

                SharedPreferences sharedPreferences3 = getSharedPreferences("LOGIN", MODE_PRIVATE);
                String branchCode = sharedPreferences3.getString("branch", "");
                String userID = sharedPreferences3.getString("userid", "");
                String sUserID = userID.isEmpty() ? "" : "&created_by=" + userID;
                String statusDecode = cmbStatus.getSelectedItem().toString().equals("All") ? "" : cmbStatus.getSelectedItem().toString().equals("Open") ? "O" : cmbStatus.getSelectedItem().equals("Closed") ? "C" : cmbStatus.getSelectedItem().toString().equals("Cancelled") ? "N" : "";
                String sStatus = "&docstatus=" + statusDecode;
                String sSAPNumber = checkSAP.isChecked() ? "&sap_number=1" : "&sap_number=";
                String sBranch = "&branch=" + branchCode;
                int id = 0;
                if(cmbType.getSelectedItem().toString().equals("Sales Transactions")){
                    for (int ii = 0; ii < jsonArrayUsers.length(); ii++) {
                        JSONObject jsonObject = jsonArrayUsers.getJSONObject(ii);
                        if(cmbUser.getSelectedItem().toString().equals(jsonObject.getString("username"))){
                            id = jsonObject.getInt("id");
                            break;
                        }
                    }
                }
                String sUser = "&created_by=" + (id > 0 ? id : "");
                String recURL = "/api/inv/recv/get_all?transdate=" + lblDate.getText().toString() + sSAPNumber + sUserID + sStatus,
                        transURL = "/api/inv/trfr/getall?transdate=" + lblDate.getText().toString() + sSAPNumber + sUserID + sStatus,
                        salesURL = "/api/sales/get_all?transdate=" + lblDate.getText().toString() + sSAPNumber + sStatus + sUser + sBranch ,
                        resultURL = "";
                if(type.equals("Received Transactions")){
                    resultURL = recURL;
                }else if(type.equals("Transfer Transactions")){
                    resultURL = transURL;
                }else{
                    System.out.println("SALES URL: " + salesURL);
                    resultURL = salesURL;
                }
                System.out.println("result: " + resultURL);
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPAddress +  resultURL)
                        .method("GET", null)
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
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if(s != null) {
                    progressBar.setVisibility(View.GONE);
                    String answer = s;
                    formatResponse(answer);
                }
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
    }


    public void formatResponse(String temp){
        if(!temp.isEmpty() && temp.substring(0,1).equals("{")){
            try{
                JSONObject jsonObject1 = new JSONObject(temp);
                if(jsonObject1.getBoolean("success")){
                    JSONArray jsonArrayData = jsonObject1.getJSONArray("data");
                    System.out.println("ey: " + jsonObject1);
                    ArrayList<String> myReference = new ArrayList<String>();
                    ArrayList<String> myID = new ArrayList<String>();
                    ArrayList<String> myAmount = new ArrayList<String>();
                    DecimalFormat df = new DecimalFormat("#,###.00");
                    double total = 0.00;
                    for (int i = 0; i < jsonArrayData.length(); i++) {
                        JSONObject jsonObjectData = jsonArrayData.getJSONObject(i);
                        myReference.add(jsonObjectData.getString("reference") + "\n SAP #: " + (jsonObjectData.isNull("sap_number") ? "" : jsonObjectData.getString("sap_number")));
                        myID.add(String.valueOf(jsonObjectData.getInt("id")));
                        String amount = (cmbType.getSelectedItem().toString().equals("Sales Transactions") ? df.format(jsonObjectData.getDouble("doctotal")) : "");
                        myAmount.add(amount);
                        total += cmbType.getSelectedItem().toString().equals("Sales Transactions") ? jsonObjectData.getDouble("doctotal") : 0.00;
                    }
                    txtTotal.setText("Total: " + df.format(total));
                    MyAdapter adapter = new MyAdapter(this, myReference, myID,myAmount);;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            TextView textView = view.findViewById(R.id.txtIDs);
                                            TextView textView1 = view.findViewById(R.id.txtReference);
                                            Intent intent;
                                            intent = new Intent(getBaseContext(), API_SalesLogsItems.class);
                                            intent.putExtra("reference", "Reference: " + textView1.getText().toString());

                                            String URL;
                                            if (cmbType.getSelectedItem().toString() == "Received Transactions") {
                                                URL = "/api/inv/recv/details/" + textView.getText().toString();
                                            } else if (cmbType.getSelectedItem().toString() == "Transfer Transactions") {
                                                URL = "/api/inv/trfr/getdetails/" + textView.getText().toString();
                                            } else {
                                                URL = "/api/sales/details/" + textView.getText().toString();
                                            }

                                            intent.putExtra("URL", URL);
                                            intent.putExtra("title", "Inventory Logs");
                                            intent.putExtra("hiddenTitle", "API Inventory Logs");
                                            intent.putExtra("type", cmbType.getSelectedItem().toString());
                                            startActivity(intent);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }else{
                    String msg = jsonObject1.getString("message");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), "Error: \n" + msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }catch (Exception ex){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ex.printStackTrace();
                        Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }else{
            Runnable r = () -> {
                Toast.makeText(getBaseContext(), temp, Toast.LENGTH_SHORT).show();
            };
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
            textView3.setVisibility((amounts.get(position).equals("") ? View.GONE : View.VISIBLE));

            return row;
        }
    }

//    @Override
//    public void onBackPressed() {
//        Intent a = new Intent(Intent.ACTION_MAIN);
//        a.addCategory(Intent.CATEGORY_HOME);
//        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(a);
//    }

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
                        pc.loggedOut(API_SalesLogs.this);
                        pc.removeToken(API_SalesLogs.this);
                        startActivity(uic.goTo(API_SalesLogs.this, MainActivity.class));
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
}
