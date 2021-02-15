package com.example.atlanticgrains;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.atlanticgrains.Adapter.CustomExpandableListAdapter;
import com.example.atlanticgrains.Helper.FragmentNavigationManager_OfflineItems;
import com.example.atlanticgrains.Interface.NavigationManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OfflineItems extends AppCompatActivity {
    prefs_class pc = new prefs_class();
    ui_class uic = new ui_class();
    navigation_class navc = new navigation_class();
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listTitle;
    private Map<String, List<String>> listChild;
    private NavigationManager navigationManager;

    DatabaseHelper myDb;
    DatabaseHelper7 myDb7;

    DecimalFormat df = new DecimalFormat("#,###");

    private OkHttpClient client;
    Menu menu;
    TableLayout tableLayout;
    Button btnBack,btnCancel;

    String title, hidden_title;
    TextView txtReference,txtHeader;
    String typeTrans;
    long mLastClickTime;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_items);

        tableLayout = findViewById(R.id.table_main);
        btnBack = findViewById(R.id.btnBack);
        txtReference = findViewById(R.id.txtReference);
        txtHeader = findViewById(R.id.txtHeader);
        btnCancel = findViewById(R.id.btnCancel);

        myDb = new DatabaseHelper(this);
        myDb7 = new DatabaseHelper7(this);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        expandableListView = (ExpandableListView)findViewById(R.id.navList);
        navigationManager = FragmentNavigationManager_OfflineItems.getmInstance(this);

        client = new OkHttpClient();


        title = getIntent().getStringExtra("title");
        hidden_title = getIntent().getStringExtra("hiddenTitle");

        txtReference = findViewById(R.id.txtReference);
        String reference = getIntent().getStringExtra("reference");
        txtReference.setText(reference);

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

        typeTrans = getIntent().getStringExtra("type");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        String id = getIntent().getStringExtra("id");
        String type = getIntent().getStringExtra("type");
        Cursor cursor = myDb7.getAllDataByModule(id, "id");
        String body = "";
        if(cursor != null){
            while (cursor.moveToNext()){
                body = cursor.getString(3);
            }
            loadData(body,type);
        }
        cursor.close();
        btnCancel.setVisibility(View.VISIBLE);
        String finalBody = body;
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OfflineItems.this);
                builder.setMessage("Are you sure want to cancel?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String id = getIntent().getStringExtra("id");
                                String reference = getIntent().getStringExtra("reference");
                                boolean isSuccess = myDb7.deleteData(id);
                                if(isSuccess){
                                    loadData(finalBody,type);
                                }else{
                                    Toast.makeText(getBaseContext(), reference + " failed to cancel", Toast.LENGTH_SHORT).show();
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
            navigationManager.showFragment(getString(R.string.app_name));
            getSupportActionBar().setTitle(getString(R.string.app_name));
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

    public void changePassword(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(OfflineItems.this);
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(OfflineItems.this);
                    builder.setMessage("Are you sure want to submit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    OfflineItems.myChangePassword myChangePassword = new OfflineItems.myChangePassword(txtPassword.getText().toString().trim());
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
        LoadingDialog loadingDialog = new LoadingDialog(OfflineItems.this);
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(OfflineItems.this);
                        builder.setMessage("We redirect you to Login Page")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pc.loggedOut(OfflineItems.this);
                                        pc.removeToken(OfflineItems.this);
                                        startActivity(uic.goTo(OfflineItems.this, MainActivity.class));
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



    public void loadData(String result,String type){
        System.out.println("this is a type: " + type);
        try{
            tableLayout.removeAllViews();
            TableRow tableColumn = new TableRow(OfflineItems.this);
            String[] columns;

            if(!typeTrans.equals("Sales")) {
                columns = new String[]{"Item", "Qty."};
            }else{
                columns = new String[]{"Item", "Qty.", "Price", "Disc. %"};
            }
            for (String s : columns) {
                TextView lblColumn1 = new TextView(OfflineItems.this);
                lblColumn1.setGravity(View.TEXT_ALIGNMENT_CENTER);
                lblColumn1.setText(s);
                lblColumn1.setPadding(10, 0, 10, 0);
                lblColumn1.setTextSize(20);
                lblColumn1.setTextColor(Color.BLACK);
                tableColumn.addView(lblColumn1);
            }
            tableLayout.addView(tableColumn);

            JSONObject jsonObject = new JSONObject(result);
            System.out.println("Printt: " + jsonObject);
            JSONObject jsonObjectHeader = jsonObject.getJSONObject("header");
            String sHeader = "";
            if(type.equals("Sales") || type.equals("Transfer Item")) {
                sHeader = "Transdate: " + jsonObjectHeader.getString("transdate") + "\n" +
//                        "Remarks: " + jsonObjectHeader.getString("remarks") + "\n" +
                        (type.equals("Transfer Item") ? "" : "Trans. Type: " + jsonObjectHeader.getString("transtype") + "\n") +
                        "Discount %: " + (jsonObjectHeader.isNull("discprcnt") ? 0.00 : jsonObjectHeader.getDouble("discprcnt")) + "\n" +
                        "Tender Amount: " + (jsonObjectHeader.isNull("tenderamt") ? 0.00 : jsonObjectHeader.getDouble("tenderamt")) + "\n" +
                        "SAP #: " + (jsonObjectHeader.isNull("sap_number") ? "N/A" : jsonObjectHeader.getInt("sap_number")) + "\n";
            }else if(type.equals("Received Item")){
                sHeader = "Transdate: " + jsonObjectHeader.getString("transdate") + "\n" +
                        "Remarks: " + jsonObjectHeader.getString("remarks") + "\n" +
                        "Supplier: " + (jsonObjectHeader.isNull("supplier") ? "N/A" : jsonObjectHeader.getString("supplier")) + "\n" +
                        (type.equals("Transfer Item") ? "" : "Type: " + (jsonObjectHeader.isNull("type2") ? "N/A" : jsonObjectHeader.getString("type2")) + "\n" +
                                "SAP #: " + (jsonObjectHeader.isNull("sap_number") ? "N/A" : jsonObjectHeader.getInt("sap_number")) + "\n");

            }else if(type.equals("Item Request")){
                sHeader = "Transdate: " + jsonObjectHeader.getString("transdate") + "\n" +
                        "Due Date: " + jsonObjectHeader.getString("duedate") + "\n" +
                        "Remarks: " + jsonObjectHeader.getString("remarks") + "\n";
            }
            txtHeader.setText(sHeader);

            String arrayName = "";

            if(type.equals("Sales") || type.equals("Item Request")){
                arrayName = "rows";
            }else if(type.equals("Received Item") || type.equals("Transfer Item")){
                arrayName = "details";
            }
            JSONArray jsonArrayRecRow = jsonObject.getJSONArray(arrayName);
            for (int i = 0; i < jsonArrayRecRow.length(); i++) {
                JSONObject jsonObjectRecRow = jsonArrayRecRow.getJSONObject(i);

                final TableRow tableRow = new TableRow(getBaseContext());
                tableRow.setBackgroundColor(Color.WHITE);
                LinearLayout linearLayoutItem = new LinearLayout(this);
                linearLayoutItem.setPadding(10, 10, 10, 10);
                linearLayoutItem.setOrientation(LinearLayout.VERTICAL);
                linearLayoutItem.setBackgroundColor(Color.WHITE);
                linearLayoutItem.setGravity(View.TEXT_ALIGNMENT_CENTER);
                tableRow.addView(linearLayoutItem);

                LinearLayout.LayoutParams layoutParamsItem = new LinearLayout.LayoutParams(400, LinearLayout.LayoutParams.WRAP_CONTENT);
                TextView lblColumn1 = new TextView(this);
                lblColumn1.setGravity(View.TEXT_ALIGNMENT_CENTER);
                lblColumn1.setLayoutParams(layoutParamsItem);
//                       String v = cutWord(item);
                lblColumn1.setText(jsonObjectRecRow.getString("item_code"));
                lblColumn1.setTextSize(15);
                lblColumn1.setTextColor(Color.BLACK);
                lblColumn1.setBackgroundColor(Color.WHITE);
                linearLayoutItem.addView(lblColumn1);

                TextView lblColumn2 = new TextView(getBaseContext());
                lblColumn2.setGravity(View.TEXT_ALIGNMENT_CENTER);
                lblColumn2.setText(df.format(jsonObjectRecRow.getDouble("quantity")));
                lblColumn2  .setTextSize(15);
                lblColumn2.setTextColor(Color.BLACK);
                lblColumn2.setBackgroundColor(Color.WHITE);
                lblColumn2.setPadding(10, 10, 10, 10);
                tableRow.addView(lblColumn2);

                TextView lblColumn3 = new TextView(getBaseContext());
                lblColumn3.setGravity(View.TEXT_ALIGNMENT_CENTER);

                double price = jsonObjectRecRow.isNull("unit_price") ? 0.00 : jsonObjectRecRow.getDouble("unit_price");
                double discprcnt = jsonObjectRecRow.isNull("discprcnt") ? 0.00 : jsonObjectRecRow.getDouble("discprcnt");

                if(typeTrans.equals("Sales")){
                    lblColumn3.setText(df.format(price));
                    lblColumn3.setTextSize(15);
                    lblColumn3.setTextColor(Color.BLACK);
                    lblColumn3.setBackgroundColor(Color.WHITE);
                    lblColumn3.setPadding(10, 10, 10, 10);
                    tableRow.addView(lblColumn3);

                    TextView lblColumn4 = new TextView(getBaseContext());
                    lblColumn4.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    lblColumn4.setText(df.format(discprcnt));
                    lblColumn4.setTextSize(15);
                    lblColumn4.setTextColor(Color.BLACK);
                    lblColumn4.setBackgroundColor(Color.WHITE);
                    lblColumn4.setPadding(10, 10, 10, 10);

                    tableRow.addView(lblColumn4);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tableLayout.addView(tableRow);
                        View viewLine = new View(getBaseContext());
                        LinearLayout.LayoutParams layoutParamsLine = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        viewLine.setLayoutParams(layoutParamsLine);
                        viewLine.setBackgroundColor(Color.GRAY);
                        tableLayout.addView(viewLine);
                    }
                });

            }
        }catch (Exception ex){
            ex.printStackTrace();
            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
                        pc.loggedOut(OfflineItems.this);
                        pc.removeToken(OfflineItems.this);
                        startActivity(uic.goTo(OfflineItems.this, MainActivity.class));
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
