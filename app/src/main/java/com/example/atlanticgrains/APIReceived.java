package com.example.atlanticgrains;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.GridLayout;
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
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.atlanticgrains.Adapter.CustomExpandableListAdapter;
import com.example.atlanticgrains.Helper.FragmentNavigationManager_APIReceived;
import com.example.atlanticgrains.Interface.NavigationManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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

public class APIReceived extends AppCompatActivity {
    private RequestQueue mQueue;
    ProgressBar progressBar;
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
    //    private String[] items;

    private ExpandableListView expandableListView;
    private List<String> listTitle;
    private Map<String, List<String>> listChild;
    private NavigationManager navigationManager;


    Button btnSearch;

    long mLastClickTime = 0;
    private OkHttpClient client;
    JSONObject globalJsonObject;
    Button btnRefresh;
    String appName = "";
    String gSelectedBranch = "",gBranch = "";
    AutoCompleteTextView cmbItemGroup,txtSearch;
    @SuppressLint({"WrongConstant", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_p_i_received);
        mQueue = Volley.newRequestQueue(this);
        progressBar = findViewById(R.id.progWait);
        btnDone = findViewById(R.id.btnDone);
        myDb4 = new DatabaseHelper4(this);
        myDb3 = new DatabaseHelper3(this);
        myDb = new DatabaseHelper(this);
        myDb8 = new DatabaseHelper8(this);
        myDb7 = new DatabaseHelper7(this);
        myDb9 = new DatabaseHelper9(this);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnSearch = findViewById(R.id.btnSearch);
        appName = getString(R.string.app_name);
        cmbItemGroup = findViewById(R.id.cmbItemGroup);
        txtSearch = findViewById(R.id.txtSearch);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        GridLayout gridLayout = findViewById(R.id.grid);
        if(width <= 720){
            gridLayout.setColumnCount(2);
            gridLayout.setRowCount(2);
        }else{
            gridLayout.setColumnCount(4);
            gridLayout.setRowCount(3);
        }


        client = new OkHttpClient();

        globalJsonObject = new JSONObject();


        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        expandableListView = (ExpandableListView)findViewById(R.id.navList);
        navigationManager = FragmentNavigationManager_APIReceived.getmInstance(this);

        SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
        String fullName = Objects.requireNonNull(sharedPreferences.getString("fullname", ""));

        @SuppressLint("InflateParams") View listReaderView = getLayoutInflater().inflate(R.layout.nav_header, null,false);
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

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hidden_title.equals("API Inventory Count")) {
                    try {
                        while (gBranch.isEmpty()){
                            gBranch = loadWarehouse();
                        }
                        if(gBranch.substring(0,1).equals("{")){
                            showWarehouses(gBranch);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    globalJsonObject = new JSONObject();
                    loadData();
                }
            }
        });

        if(!hidden_title.equals("API Inventory Count")){
            myItemGroups myItemGroups = new myItemGroups();
            myItemGroups.execute();
        }

        if(hidden_title.equals("API Inventory Count")) {
            try {
                while (gBranch.isEmpty()) {
                    gBranch = loadWarehouse();
                }
                if (gBranch.substring(0, 1).equals("{")) {
                    showWarehouses(gBranch);
                }

                TextView lblInformation = findViewById(R.id.lblInformation);
                lblInformation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnRefresh.performClick();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });

        cmbItemGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                loadData();
            }
        });
        btnDone.setVisibility(hidden_title.equals("API Production Order List") ? View.GONE : View.VISIBLE);
        if(!hidden_title.equals("API Inventory Count")){
            loadData();
        }
    }

    public String getFromSharedPref(String name, String key){
        SharedPreferences sharedPreferences2 = getSharedPreferences(name, MODE_PRIVATE);
        return sharedPreferences2.getString(key, "");
    }

    public String loadWarehouse() throws IOException {
        try{
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            String IPAddress = getFromSharedPref("CONFIG", "IPAddress");
            String token = getFromSharedPref("TOKEN", "token");
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(IPAddress + "/api/whse/get_all")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = null;
            response = client.newCall(request).execute();
            return response.body().string();
        }catch (Exception ex){
            ex.printStackTrace();
            Cursor cursor = myDb8.getAllData();
            String res = "";
            while (cursor.moveToNext()) {
                String module = cursor.getString(3);
                if (module.contains("Warehouse")) {
                    res = cursor.getString(4);
                }
            }
            return res;
        }
    }


    public String findWarehouseCode(String value){
        try{
            JSONObject jsonObjectResponse = new JSONObject(gBranch);
            JSONArray jsonArray = jsonObjectResponse.getJSONArray("data");
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                            String branch = jsonObject.getString("whsecode") + "," + jsonObject.getString("whsename");
                if(value.contains(jsonObject.getString("whsename"))){
                    return jsonObject.getString("whsecode");
                }
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
        return "";
    }

    public List<String> returnBranches(String value) {
        List<String> result = new ArrayList<>();
        result.add("Select Warehouse");
//        System.out.println(gBranch);
        try {
            JSONObject jsonObjectResponse = new JSONObject(value);
            JSONArray jsonArray = jsonObjectResponse.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String branch = jsonObject.getString("whsename");
                result.add(branch);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getBaseContext(),"dito po ba? " +  ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    public void showWarehouses(String value){
        AlertDialog _dialog = null;
        AlertDialog.Builder dialogSelectWarehouse = new AlertDialog.Builder(APIReceived.this);
        dialogSelectWarehouse.setTitle("Select Warehouse");
        dialogSelectWarehouse.setCancelable(false);
        LinearLayout layout = new LinearLayout(getBaseContext());
        layout.setPadding(40, 40, 40, 40);
        layout.setOrientation(LinearLayout.VERTICAL);

        AutoCompleteTextView txtSearchBranch = new AutoCompleteTextView(getBaseContext());
        txtSearchBranch.setTextSize(13);
        layout.addView(txtSearchBranch);

        final List<String>[] warehouses = new List[]{returnBranches(value)};
        final ArrayList<String>[] myReference = new ArrayList[]{getReference(warehouses[0], txtSearchBranch.getText().toString().trim())};
        final ArrayList<String>[] myID = new ArrayList[]{getID(warehouses[0], txtSearchBranch.getText().toString().trim())};
        final List<String>[] listItems = new List[]{getListItems(warehouses[0])};

        TextView btnSearchBranch = new TextView(getBaseContext());
        btnSearchBranch.setBackgroundColor(Color.parseColor("#0b8a0f"));
        btnSearchBranch.setPadding(20,20,20,20);
        btnSearchBranch.setTextColor(Color.WHITE);
        btnSearchBranch.setTextSize(13);
        btnSearchBranch.setText("Search");
        ListView listView = new ListView(getBaseContext());
        btnSearchBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myReference[0] = getReference(warehouses[0], txtSearchBranch.getText().toString().trim());
                myID[0] = getID(warehouses[0], txtSearchBranch.getText().toString().trim());
                listItems[0] = getListItems(warehouses[0]);

                APIReceived.MyAdapter adapter = new APIReceived.MyAdapter(APIReceived.this, myReference[0], myID[0]);

                listView.setAdapter(adapter);
            }
        });

        layout.addView(btnSearchBranch);


        LinearLayout.LayoutParams layoutParamsWarehouses = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,300);
        layoutParamsWarehouses.setMargins(10,10,10,10);
        listView.setLayoutParams(layoutParamsWarehouses);

        txtSearchBranch.setAdapter(fillItems(listItems[0]));
        APIReceived.MyAdapter adapter = new APIReceived.MyAdapter(APIReceived.this, myReference[0], myID[0]);
        dialogSelectWarehouse.setView(layout);

        dialogSelectWarehouse.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        _dialog = dialogSelectWarehouse.show();
        listView.setAdapter(adapter);

        AlertDialog final_dialog = _dialog;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = view.findViewById(R.id.txtIDs);
                        TextView textView1 = view.findViewById(R.id.txtReference);
                        gSelectedBranch = textView1.getText().toString();
                        TextView lblInfo = findViewById(R.id.lblInformation);
                        lblInfo.setText("Branch: " +  gSelectedBranch);
                        globalJsonObject = new JSONObject();
                        loadData();
                        final_dialog.dismiss();
                    }
                });
            }
        });
        layout.addView(listView);
    }

    public List<String> getListItems(List<String> warehouses){
        List<String> result = new ArrayList<String>();
        for(String temp : warehouses){
            if(!temp.contains("Select Warehouse")){
                result.add(temp);
            }
        }
        return result;
    }

    public ArrayList<String> getReference(List<String> warehouses,String value){
        ArrayList<String> result = new ArrayList<String>();
        for(String temp : warehouses){
            if(!temp.contains("Select Warehouse")){
                if (!value.isEmpty()) {
                    if (value.trim().toLowerCase().equals(temp.toLowerCase())) {
                        result.add(temp);
                    }
                }else{
                    result.add(temp);
//                    myID.add("0");
                }
            }
        }
        return result;
    }

    public ArrayList<String> getID(List<String> warehouses,String value){
        ArrayList<String> result = new ArrayList<String>();
        for(String temp : warehouses){
            if(!temp.contains("Select Warehouse")){
                if (!value.isEmpty()) {
                    if (value.trim().contains(temp)) {
                        result.add("0");
//                        myID.add("0");
                    }
                }else{
                    result.add("0");
//                    myID.add("0");
                }
            }
        }
        return result;
    }

    class MyAdapter extends ArrayAdapter<String> {
        Context rContext;
        ArrayList<String> myReference;
        ArrayList<String> myIds;

        MyAdapter(Context c, ArrayList<String> reference, ArrayList<String> id) {
            super(c, R.layout.custom_list_view_sales_logs, R.id.txtReference, reference);
            this.rContext = c;
            this.myReference = reference;
            this.myIds = id;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.custom_list_view_sales_logs, parent, false);
            TextView textView1 = row.findViewById(R.id.txtReference);
            TextView textView2 = row.findViewById(R.id.txtIDs);

            textView1.setText(myReference.get(position));
            textView2.setText(myIds.get(position));
            textView2.setVisibility(View.INVISIBLE);

            return row;
        }
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

    public void loadData(){
        TextView lblInformation = findViewById(R.id.lblInformation);
        if(hidden_title.equals("API Item Request For Transfer")){
            if(myDb3.countItems(hidden_title) <= 0){
                getItems(0);
            }else {
                lblInformation.setVisibility(View.VISIBLE);
                Button btnBack = findViewById(R.id.btnBack);
                btnBack.setVisibility(View.VISIBLE);
                loadSelectedSAPNumberItems();

                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myDb3.truncateTable();
                        lblInformation.setVisibility(View.GONE);
                        btnBack.setVisibility(View.GONE);
                        loadData();
                    }
                });
            }
        }else if(hidden_title.equals("API Inventory Count")){
            LinearLayout linearLayoutItemGroup = findViewById(R.id.layoutItemGroup);
            linearLayoutItemGroup.setVisibility(View.GONE);
            lblInformation.setVisibility(View.VISIBLE);
            getItems(0);
        }
        else{
            getItems(0);
        }
    }

    @SuppressLint("SetTextI18n")
    public void loadSelectedSAPNumberItems() {
        Handler handler = new Handler();
        progressBar.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> {
            GridLayout gridLayout = findViewById(R.id.grid);
            gridLayout.removeAllViews();
            Cursor cursor = myDb3.getAllData(hidden_title);
            int iterate = 1;
            if(cursor != null) {
                List<String> listItems = new ArrayList<String>();
                while (cursor.moveToNext()) {
                    final int id = cursor.getInt(0);
                    final String sapNumber = cursor.getString(1);
                    final String fromBranch = (hidden_title.equals("API System Transfer Item") ? cursor.getString(8) : cursor.getString(2));
                    final String itemName = cursor.getString(3);
                    final double quantity = cursor.getDouble(4);
                    final boolean isSelected = (cursor.getInt(6) > 0);
                    TextView lblInformation = findViewById(R.id.lblInformation);
                    lblInformation.setText("#: " + sapNumber + "\n Branch: " + fromBranch);
                    listItems.add(itemName);
                    String uom = cursor.getString(11);
                    int received_quantity = cursor.getInt(12);
                    int itemID = cursor.getInt(13);
                    boolean isClosed = cursor.getInt(14) > 0;
                    if (!txtSearch.getText().toString().trim().isEmpty()) {
                        if (txtSearch.getText().toString().trim().toLowerCase().contains(itemName.toLowerCase())) {
                            uiItems2(id,itemName,sapNumber,quantity,fromBranch,isSelected,received_quantity,uom,itemID,isClosed,iterate);
                            iterate+= 1;
                        }
                    }else{
                        uiItems2(id,itemName,sapNumber,quantity,fromBranch,isSelected,received_quantity,uom,itemID,isClosed,iterate);
                        iterate+= 1;
                    }
                }
                txtSearch.setAdapter(fillItems(listItems));
            }
            cursor.close();
            progressBar.setVisibility(View.GONE);
        },500);
        btnDone.setOnClickListener(view -> navigateDone());
    }

    public void addDrawersItem(){
        ExpandableListAdapter adapter = new CustomExpandableListAdapter(this, listTitle, listChild);
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
        int iterate = 4;
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
        if(mDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }



    @SuppressLint("SetTextI18n")
    public void changePassword(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(APIReceived.this);
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                    builder.setMessage("Are you sure want to submit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    APIReceived.myChangePassword myChangePassword = new APIReceived.myChangePassword(txtPassword.getText().toString().trim());
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
        String password;
        LoadingDialog loadingDialog = new LoadingDialog(APIReceived.this);
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                        builder.setMessage("We redirect you to Login Page")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pc.loggedOut(APIReceived.this);
                                        pc.removeToken(APIReceived.this);
                                        startActivity(uic.goTo(APIReceived.this, MainActivity.class));
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

    private class myItemGroups extends AsyncTask<String, Void, String> {
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

                SharedPreferences sharedPreferences1 = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = sharedPreferences1.getString("token", "");

//                System.out.println("IP Address: " + IPAddress);
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPAddress + "/api/item/item_grp/getall")
                        .addHeader("Authorization", "Bearer " + token)
                        .method("GET", null)
                        .build();
                Response response = null;

                response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception ex) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if(s != null) {
                    if(s.substring(0,1).equals("{")){
                        JSONObject jsonObject1 = new JSONObject(s);
                        String msg = jsonObject1.getString("message");
                        if (jsonObject1.getBoolean("success")) {
                            List<String> tenderTypes = new ArrayList<>();
                            JSONArray jsonArray = jsonObject1.getJSONArray("data");
                            tenderTypes.add("All");
                            SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
                            for (int ii = 0; ii < jsonArray.length(); ii++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(ii);
                                tenderTypes.add(jsonObject.getString("code"));
                            }
//                            ArrayAdapter<String> adapter = new ArrayAdapter<>(APIReceived.this, android.R.layout.simple_spinner_item, );
//                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            cmbItemGroup.setAdapter(fillItems(tenderTypes));
//                            cmbItemGroup.setText("All");
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    globalJsonObject = new JSONObject();
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getBaseContext(), "Validation \n" + msg, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    else{
                        globalJsonObject = new JSONObject();
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getBaseContext(), "Validation \n" + s, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Cursor cursor = myDb8.getAllData();
                            while (cursor.moveToNext()){
                                String module = cursor.getString(3);
//                                System.out.println("Moduleee: " + module);
                                if(module.contains("Item Group")){
                                    try {
                                        JSONObject jsonObject1 = new JSONObject(cursor.getString(4));
                                        String msg = jsonObject1.getString("message");
                                        if (jsonObject1.getBoolean("success")) {
                                            List<String> tenderTypes = new ArrayList<>();
                                            tenderTypes.add("All");
                                            JSONArray jsonArray = jsonObject1.getJSONArray("data");
                                            SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
                                            String isSales = Objects.requireNonNull(sharedPreferences.getString("isSales", ""));
                                            String isProduction = Objects.requireNonNull(sharedPreferences.getString("isProduction", ""));
                                            for (int ii = 0; ii < jsonArray.length(); ii++) {
                                                JSONObject jsonObject = jsonArray.getJSONObject(ii);
                                                tenderTypes.add(jsonObject.getString("code"));
                                            }
                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(APIReceived.this, android.R.layout.simple_spinner_item, tenderTypes);
                                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                                            spinnerItemGroup.setAdapter(adapter);
                                            globalJsonObject = new JSONObject();
                                            loadData();
                                        }else{
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    globalJsonObject = new JSONObject();
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(getBaseContext(), "Validation \n" + msg, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } catch (JSONException ex) {
                                        globalJsonObject = new JSONObject();
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                }
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        globalJsonObject = new JSONObject();
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getBaseContext(), "Validation \n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(hidden_title.equals("API Transfer Item")){
//            globalJsonObject = new JSONObject();
//            getItems(0);
//        }else if(hidden_title.equals("API Received Item")){
//            globalJsonObject = new JSONObject();
//            getItems(0);;
//        }
//        else {
//            globalJsonObject = new JSONObject();
//            getItems(0);
//        }
//        if(API_ItemInfo.isSubmit){
//            API_ItemInfo.isSubmit = false;
//            loadData();
//        }
        if(hidden_title.equals("API Inventory Count") && !gSelectedBranch.isEmpty()) {
            getItems(0);
        }else{
            getItems(0);
        }
    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    public void onBtnLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    pc.loggedOut(APIReceived.this);
                    pc.removeToken(APIReceived.this);
                    startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public ArrayAdapter<String> fillItems(List<String> items){
        return new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, items);
    }

    @SuppressLint("SetTextI18n")
    public void uiItems2(int id, String itemName, String sapNumber, double quantity, String fromBranch, boolean isSelected, int receivedQuantity, String uom, int itemID, boolean isClosed, int iterate){
        GridLayout gridLayout = findViewById(R.id.grid);
        MaterialCardView cardView = new MaterialCardView(APIReceived.this);
        cardView.setRadius(15);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int boxWidth,boxHeight;
        if(width <= 720){
            boxHeight = 250;
            boxWidth = 250;
        }else{
            boxHeight = 350;
            boxWidth = 350;
        }

        int cardViewMarginLeft = 0;
        if(width <= 720){
            if(iterate % 2 == 0){
                cardViewMarginLeft = 20;
            }
        }else{
            cardViewMarginLeft = 20;
        }

        LinearLayout.LayoutParams layoutParamsCv = new LinearLayout.LayoutParams(boxWidth, boxHeight);
        layoutParamsCv.setMargins(cardViewMarginLeft, 10, 10, 10);
        cardView.setLayoutParams(layoutParamsCv);
        cardView.setRadius(12);
        cardView.setCardElevation(5);

        System.out.println("item: " + itemName);

        cardView.setVisibility(View.VISIBLE);
        gridLayout.addView(cardView);
        final LinearLayout linearLayout = new LinearLayout(APIReceived.this);
        LinearLayout.LayoutParams layoutParamsLinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 5f);
        linearLayout.setLayoutParams(layoutParamsLinear);
        linearLayout.setTag(id);

        linearLayout.setOnClickListener(view -> {
            if(hidden_title.equals("API Production Order List")){
                if(isSelected || isClosed){
                    Toast.makeText(getBaseContext(), "'" + itemName + "' is already closed!", Toast.LENGTH_SHORT).show();
                }else {
                    AlertDialog.Builder myDialog = new AlertDialog.Builder(APIReceived.this);
                    myDialog.setCancelable(false);
                    myDialog.setTitle(itemName);
//            System.out.println("ID: " + finalDocEntry);
                    myDialog.setMessage("Are you sure you want to close?");
                    myDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            JSONObject jsonObject = new JSONObject();
                            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                            RequestBody body = RequestBody.create(JSON, jsonObject.toString());
                            SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                            String IPaddress = sharedPreferences2.getString("IPAddress", "");
                            SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                            String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                            okhttp3.Request request = new okhttp3.Request.Builder()
                                    .url(IPaddress + "/api/production/order/details/close/" + itemID)
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
                                public void onResponse(Call call, okhttp3.Response response) {
                                    String result = "";
                                    try {
                                        result = response.body().string();
//                            System.out.println(result);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    String finalResult = result;
                                    APIReceived.this.runOnUiThread(() -> {
                                        try {
                                            JSONObject jj = new JSONObject(finalResult);
                                            boolean isSuccess = jj.getBoolean("success");
                                            if (isSuccess) {
                                                Toast.makeText(getBaseContext(),  jj.getString("message"), Toast.LENGTH_SHORT).show();
                                                boolean isInserted = myDb3.updateSelected(Integer.toString(id),1, quantity);
                                                if(isInserted){
                                                    loadData();
                                                }else {
                                                    Toast.makeText(getBaseContext(),  "Failed to Close", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                String msg = jj.getString("message");
                                                if (msg.equals("Token is invalid")) {
                                                    final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                                    builder.setCancelable(false);
                                                    builder.setMessage("Your session is expired. Please login again.");
                                                    builder.setPositiveButton("OK", (dialog, which) -> {
                                                        pc.loggedOut(APIReceived.this);
                                                        pc.removeToken(APIReceived.this);
                                                        startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                                        finish();
                                                        dialog.dismiss();
                                                    });
                                                    builder.show();
                                                } else {
                                                    Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }
                            });
                        }
                    });
                    myDialog.setNegativeButton("No", (dialogInterface, i1) -> dialogInterface.dismiss());
                    myDialog.show();
                }
            }else {
                if (isSelected) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
//                    Intent intent;
//                    intent = new Intent(getBaseContext(), API_ItemInfo.class);
//                    intent.putExtra("title", title);
//                    intent.putExtra("hiddenTitle", hidden_title);
//                    intent.putExtra("item", itemName);
//                    intent.putExtra("sapNumber", sapNumber);
//                    intent.putExtra("quantity", Double.toString(quantity));
//                    intent.putExtra("fromBranch", fromBranch);
//                    intent.putExtra("deliveredQuantity", quantity);
//                    intent.putExtra("receivedQuantity", receivedQuantity);
//                    intent.putExtra("id", id);
//                    startActivity(intent);
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getBaseContext());
                    builder.setTitle(itemName);
                    builder.setCancelable(false);
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.show();
                }
            }
        });
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(View.TEXT_ALIGNMENT_CENTER);
        linearLayout.setVisibility(View.VISIBLE);


        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(20, 0, 20, 0);
        LinearLayout.LayoutParams layoutParamsItemLeft = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsItemLeft.setMargins(20, (hidden_title.equals("API Received from Production") || hidden_title.equals("API Item Request For Transfer") ? - 100 : -50), 0, 10);

        TextView txtItemName = new TextView(APIReceived.this);
        String cutWord = cutWord(itemName, 25);
        txtItemName.setText(cutWord);
        txtItemName.setLayoutParams(layoutParams);
        txtItemName.setTextSize(15);
        txtItemName.setVisibility(View.VISIBLE);

        TextView txtItemLeft = new TextView(APIReceived.this);
        txtItemLeft.setLayoutParams(layoutParamsItemLeft);
        txtItemLeft.setTextSize(13);
        txtItemLeft.setVisibility(View.VISIBLE);
        txtItemLeft.setText("Delivered Quantity: \n" +quantity);
        txtItemLeft.setTextColor(Color.parseColor("#34A853"));
            if (isSelected || isClosed) {
                linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                txtItemName.setTextColor(Color.rgb(250, 250, 250));
                txtItemLeft.setTextColor(Color.rgb(250, 250, 250));
            } else if(hidden_title.equals("API Received from Production") && receivedQuantity > 0) {
                linearLayout.setBackgroundColor(Color.rgb(250, 208, 17));
                txtItemName.setTextColor(Color.BLACK);
                txtItemLeft.setTextColor(Color.BLACK);
            }else {
                linearLayout.setBackgroundColor(Color.rgb(250, 250, 250));
                txtItemName.setTextColor(Color.rgb(28, 28, 28));
                txtItemLeft.setTextColor(Color.parseColor("#34A853"));
            }
        cardView.addView(linearLayout);
        linearLayout.addView(txtItemName);
        linearLayout.addView(txtItemLeft);
    }
    public void getItems(int docEntry) {
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
        String currentBranch = Objects.requireNonNull(sharedPreferences2.getString("branch", ""));
        String currentWhse = Objects.requireNonNull(sharedPreferences2.getString("whse", ""));
        progressBar.setVisibility(View.VISIBLE);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        wait(10);
                    } catch (InterruptedException ex) {
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String currentDate = sdf.format(new Date());
                    String appendURL = "";
                    if (docEntry > 0) {
                        appendURL = "/api/sapb1/itdetails/" + docEntry;
                    } else if (hidden_title.equals("API Received Item") || hidden_title.equals("API Item Request")) {
                        appendURL = "/api/item/getall";
                    } else if (hidden_title.equals("API Transfer Item")) {
                        appendURL = "/api/inv/whseinv/getall";
                    } else if (hidden_title.equals("API Item Request For Transfer")) {
                        appendURL = "/api/inv/item_request/get_all";
                    } else if (hidden_title.equals("API Issue For Production")) {
                        appendURL = "/api/inv/whseinv/getall";
                    } else if (hidden_title.equals("API Received from Production")) {
                        appendURL = "/api/inv/whseinv/getall";
                    } else if (hidden_title.equals("API Inventory Count")) {
                        System.out.println(gSelectedBranch);
                        appendURL = "/api/inv/count/create?whsecode=" + findWarehouseCode(gSelectedBranch);
                    }
                    SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                    String IPaddress = sharedPreferences2.getString("IPAddress", "");

                    String URL = IPaddress + appendURL;
                    System.out.println(URL);
//                    System.out.println(URL);
                    if (globalJsonObject.toString().equals("{}")) {
                        okhttp3.Request request = new okhttp3.Request.Builder()
                                .url(URL)
                                .method("GET", null)
                                .addHeader("Authorization", "Bearer " + token)
                                .addHeader("Content-Type", "application/json")
                                .build();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        e.printStackTrace();
                                        if (hidden_title.equals("API Received Item") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Menu Items")) {

                                        } else {
                                            Toast.makeText(getBaseContext(), "Error Connection \n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

//                                        Toast.makeText(getBaseContext(), "Error Connection" + (hidden_title.equals("API Received Item") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Menu Items") ? "\n" + e.getMessage() + "\n" + "The data is from Resources" : "\n" + e.getMessage()) , Toast.LENGTH_SHORT).show();

                                        if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item")) {
                                            loadOffline("Stock");
                                        } else if (hidden_title.equals("API Received Item") || hidden_title.equals("API Item Request")) {
                                            loadOffline("Item");
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, okhttp3.Response response) {
                                try {
//                                System.out.println(response.body().string());
                                    String sResult = response.body().string();
//                                    System.out.println(sResult);
                                    System.out.println("response: " + sResult);
                                    MyAppendData myAppendData = new MyAppendData(sResult, false);
                                    myAppendData.execute("");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ex.printStackTrace();
//                                            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        MyAppendData myAppendData = new MyAppendData(globalJsonObject.toString(), false);
                        myAppendData.execute("");
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                navigateDone();
            }
        });
    }

    public void loadOffline(String fromModule){
        Cursor cursor = myDb8.getAllData();
        while (cursor.moveToNext()){
            String module = cursor.getString(3);
            if(module.contains(fromModule)){
                try {
                    if(!module.equals("Item Group")) {
                        globalJsonObject = new JSONObject(cursor.getString(4));
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                if(!module.equals("Item Group")) {
                    MyAppendData myAppendData = new MyAppendData(cursor.getString(4),false);
                    myAppendData.execute("");
                }
            }
        }
    }

    private class MyAppendData extends AsyncTask<String, Void, String> {
        String sResult = "";
        boolean sItemGroup = false;
        public MyAppendData(String result,boolean isItemGroup){
            sResult = result;
            sItemGroup = isItemGroup;
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
                SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
                JSONObject jsonObjectResponse = new JSONObject();
                List<String> listItems = new ArrayList<String>();
                if(s.substring(0,1).equals("{")){
                    if (!globalJsonObject.toString().equals("{}")) {
                        jsonObjectResponse = globalJsonObject;
                    } else {
                        globalJsonObject = new JSONObject(s);
                        jsonObjectResponse = new JSONObject(s);
                    }
                    GridLayout gridLayout = findViewById(R.id.grid);
                    gridLayout.removeAllViews();
                    if (jsonObjectResponse.getBoolean("success")) {
                        JSONArray jsonArray = jsonObjectResponse.getJSONArray("data");
                        runOnUiThread(new Runnable() {
                            @SuppressLint({"ResourceType", "SetTextI18n"})
                            @Override
                            public void run() {
                                try {
                                    int iterate = 1;
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                        String item;
                                        String uom = "", uomGroup = "";
                                        double price = 0.00;
                                        double stockQuantity = 0.00;
                                        int docEntry1 = 0;
                                        int store_quantity = 0, auditor_quantity = 0, variance_quantity = 0;
                                        boolean isIssued = (hidden_title.equals("API Production Order List") && (!jsonObject1.isNull("issued")));
                                        String prodStatus = (hidden_title.equals("API Production Order List") ? jsonObject1.isNull("status") ? "" : jsonObject1.getString("status") : "");
                                        switch (hidden_title) {
                                            case "API Item Request":
                                                item = jsonObject1.getString("item_name");
                                                break;
                                            case "API Menu Items":
                                            case "API Transfer Item":
                                            case "API Inventory Count":
                                            case "API Pull Out Count":
                                            case "API Received Item":
                                            case "API Issue For Production":
                                            case "API Received from Production":
                                                SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
                                                String isManager = sharedPreferences2.getString("isManager", "");
                                                item = jsonObject1.has("item_code") ? jsonObject1.getString("item_code") : jsonObject1.getString("code");
//                                    JSONObject jsonObjectItem = jsonObject1.getJSONObject("item");
                                                if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item")) {
                                                    price = jsonObject1.getDouble("price");
                                                    uom = jsonObject1.getString("uom");
                                                }
                                                if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Inventory Count") || hidden_title.equals("API Issue For Production") || hidden_title.equals("API Received from Production")) {
                                                    stockQuantity = jsonObject1.isNull("quantity") ? 0.00 : jsonObject1.getDouble("quantity");
                                                    uom = jsonObject1.getString("uom");
                                                } else if (hidden_title.equals("API Pull Out Count") && Integer.parseInt(isManager) <= 0) {
                                                    stockQuantity = jsonObject1.getDouble("quantity");
                                                    uom = jsonObject1.getString("uom");
                                                }

                                                if (Integer.parseInt(isManager) > 0 && hidden_title.equals("API Pull Out Count")) {
                                                    store_quantity = jsonObject1.getInt("sales_count");
                                                    auditor_quantity = jsonObject1.getInt("auditor_count");
                                                    variance_quantity = jsonObject1.getInt("variance");
                                                    uom = jsonObject1.getString("uom");
                                                }

                                                break;
                                            case "API System Transfer Item":
                                            case "API Confirm Issue For Production":
                                            case "API Item Request For Transfer":
                                            case "API Production Order List":
                                                item = jsonObject1.getString("reference");
                                                docEntry1 = jsonObject1.getInt("id");
                                                break;
                                            default:
                                                item = jsonObject1.getString("docnum");
                                                docEntry1 = jsonObject1.getInt("docentry");
                                                break;
                                        }

                                        String supplier = "";
//                                    if (hidden_title.equals("API Received from SAP") && spinner.getSelectedItemPosition() == 1) {
//                                        supplier = jsonObject1.getString("cardcode");
//                                    }
                                        if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Received Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Issue For Production") || hidden_title.equals("API Received from Production")) {
                                            stockQuantity -= myDb7.getDecreaseQuantity(item);
                                            stockQuantity += myDb7.getIncreaseQuantity(item);
                                            uom = jsonObject1.has("uom") ? jsonObject1.getString("uom") : "";
                                            if (hidden_title.equals("API Received Item") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Issue For Production") || hidden_title.equals("API Received from Production")) {
                                                uomGroup = jsonObject1.has("uom_group") ? jsonObject1.getString("uom_group") : "0";
                                            }
                                        }
                                        String itemGroup = "";
                                        if(!hidden_title.equals("API Item Request For Transfer") && !hidden_title.equals("API Inventory Count")){
                                            uomGroup = jsonObject1.getString("item_group");
                                        }
                                        listItems.add(item);
                                        if(!txtSearch.getText().toString().trim().isEmpty()){
                                            if(txtSearch.getText().toString().trim().toLowerCase().contains(item.toLowerCase())){
                                                if(!cmbItemGroup.getText().toString().trim().isEmpty()){
                                                    if(cmbItemGroup.getText().toString().trim().equals("All")){
                                                        loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom, isIssued, prodStatus, uomGroup,iterate);
                                                        iterate+= 1;
                                                    }else if(cmbItemGroup.getText().toString().trim().toLowerCase().contains(itemGroup.toLowerCase())){
                                                        loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom, isIssued, prodStatus, uomGroup,iterate);
                                                        iterate+= 1;
                                                    }
                                                }else{
                                                    loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom, isIssued, prodStatus, uomGroup,iterate);
                                                    iterate+= 1;
                                                }
                                            }
                                        }else{
                                            if(!cmbItemGroup.getText().toString().trim().isEmpty()){
                                                if(cmbItemGroup.getText().toString().trim().equals("All")){
                                                    loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom, isIssued, prodStatus, uomGroup,iterate);
                                                    iterate+= 1;
                                                }else if(cmbItemGroup.getText().toString().trim().toLowerCase().contains(itemGroup.toLowerCase())){
                                                    loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom, isIssued, prodStatus, uomGroup,iterate);
                                                    iterate+= 1;
                                                }
                                            }
                                            else{
                                                loadUIItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity, uom, isIssued, prodStatus, uomGroup,iterate);
                                                iterate+= 1;
                                            }
                                        }
                                    }
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
                            final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                            builder.setCancelable(false);
                            builder.setMessage("Your session is expired. Please login again.");
                            builder.setPositiveButton("OK", (dialog, which) -> {
                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                    return;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();
                                pc.loggedOut(APIReceived.this);
                                pc.removeToken(APIReceived.this);
                                startActivity(uic.goTo(APIReceived.this, MainActivity.class));
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtSearch.setAdapter(fillItems(listItems));
                        }
                    });
                }else{
                    Toast.makeText(getBaseContext(), "DITO PO BAAAA? " + s,Toast.LENGTH_SHORT).show();
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

    public void loadUIItems(String item, double price, double stockQuantity, int docEntry1, String supplier, int store_quantity, int auditor_quantity, int variance_quantity, String uom, boolean isIssued, String prodStatus,String uomGroup, int iterate){
//        if (!txtSearch.getText().toString().trim().isEmpty()) {
//            if (txtSearch.getText().toString().trim().contains(item)) {
//                uiItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity,uom,isIssued, prodStatus,uomGroup);
//            }
//        }else{
//            uiItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity,uom,isIssued, prodStatus,uomGroup);
//        }
        uiItems(item, price, stockQuantity, docEntry1, supplier, store_quantity, auditor_quantity, variance_quantity,uom,isIssued, prodStatus,uomGroup,iterate);
    }

    private int getWidthResolution(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        return width;
    }

    @SuppressLint("SetTextI18n")
    public void uiItems(String item, Double price, Double stockQuantity, int docEntry1, String supplier, int store_quantity, int auditor_quantity, int variance_quantity,String uom,boolean isIssued, String prodStatus,String uomGroup, int iterate) {
        GridLayout gridLayout = findViewById(R.id.grid);
        MaterialCardView cardView = new MaterialCardView(APIReceived.this);
        cardView.setRadius(15);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int boxWidth = 0,boxHeight = 0;
        if(width <= 720){
           boxHeight = 250;
           boxWidth = 250;
        }else{
            boxHeight = 350;
            boxWidth = 350;
        }

        int cardViewMarginLeft = 0;
        if(iterate % 2 == 0){
            cardViewMarginLeft = 20;
        }
        LinearLayout.LayoutParams layoutParamsCv = new LinearLayout.LayoutParams(boxWidth, boxHeight);
        layoutParamsCv.setMargins(cardViewMarginLeft, 10, 10, 10);
        cardView.setLayoutParams(layoutParamsCv);
        cardView.setRadius(12);
        cardView.setCardElevation(5);

        cardView.setVisibility(View.VISIBLE);
        gridLayout.addView(cardView);
        final LinearLayout linearLayout = new LinearLayout(getBaseContext());
        linearLayout.setBackgroundColor(Color.rgb(255, 255, 255));
        LinearLayout.LayoutParams layoutParamsLinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 5f);
        linearLayout.setLayoutParams(layoutParamsLinear);
        linearLayout.setTag("Linear" + item);

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(View.TEXT_ALIGNMENT_CENTER);
        linearLayout.setVisibility(View.VISIBLE);

        String finalItem = item;
        int finalDocEntry = docEntry1;
        double finalPrice = price;
        double finalStockQuantity = stockQuantity;
        String finalSupplier = supplier;
        linearLayout.setOnClickListener(view -> {
            if (hidden_title.equals("API Menu Items")) {
                if (myDb.checkItem(item)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,linearLayout);
                }

            } else if (hidden_title.equals("API Received Item")) {
                if (myDb4.checkItem(item, title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,linearLayout);
                }
            } else if (hidden_title.equals("API Transfer Item")) {
                if (myDb4.checkItem(item, title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,linearLayout);
                }
            } else if (hidden_title.equals("API Item Request")) {
                if (myDb4.checkItem(item, title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,linearLayout);
                }
            } else if (hidden_title.equals("API Inventory Count")) {
                if (myDb3.checkItem(item, hidden_title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,linearLayout);
                }
            }
            else if (hidden_title.equals("API Received from Production")) {
                if (myDb4.checkItem(item, title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,linearLayout);
                }
            }
            else if (hidden_title.equals("API Issue For Production")) {
                if (myDb4.checkItem(item, title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,linearLayout);
                }
            }
            else if (hidden_title.equals("API Pull Out Count")) {
                if (myDb3.checkItem(item, hidden_title)) {
                    Toast.makeText(getBaseContext(), "This item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,linearLayout);
                }
            } else {
                anotherFunction(finalItem, finalPrice, finalDocEntry, finalSupplier, stockQuantity, store_quantity, auditor_quantity, variance_quantity, uom,uomGroup,linearLayout);
            }
        });

        cardView.addView(linearLayout);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(20, 0, 20, 0);
        LinearLayout.LayoutParams layoutParamsItemLeft = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsItemLeft.setMargins(20, -50, 0, 10);

        TextView txtItemName = new TextView(getBaseContext());
        txtItemName.setTag(item);
        txtItemName.setText(cutWord(item, 35));
        txtItemName.setTextColor(Color.rgb(0, 0, 0));
        txtItemName.setLayoutParams(layoutParams);
        txtItemName.setTextSize(15);
        txtItemName.setVisibility(View.VISIBLE);
        linearLayout.addView(txtItemName);

        if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Received Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Inventory Count") || hidden_title.equals("API Pull Out Count")  || hidden_title.equals("API Issue For Production") || hidden_title.equals("API Received from Production")) {
            TextView txtItemLeft = new TextView(getBaseContext());
            txtItemLeft.setLayoutParams(layoutParamsItemLeft);
            txtItemLeft.setTextColor(Color.rgb(0, 0, 0));
            txtItemLeft.setTextSize(13);
            txtItemLeft.setVisibility(View.VISIBLE);
            if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Issue For Production") || hidden_title.equals("API Received from Production")) {
                txtItemLeft.setText(df.format(stockQuantity) + " available");
                if (stockQuantity <= 0) {
                    txtItemLeft.setTextColor(Color.rgb(252, 28, 28));
                } else if (stockQuantity <= 10) {
                    txtItemLeft.setTextColor(Color.rgb(247, 154, 22));
                } else if (stockQuantity > 11) {
                    txtItemLeft.setTextColor(Color.rgb(30, 203, 6));
                }
            }

            SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
            String isManager = sharedPreferences2.getString("isManager", "");
            if (Integer.parseInt(isManager) > 0 && hidden_title.equals("API Inventory Count")) {
                txtItemLeft.setText(df.format(variance_quantity) + " variance");
                if (variance_quantity < 0) {
                    txtItemLeft.setTextColor(Color.rgb(252, 28, 28));
                } else {
                    txtItemLeft.setTextColor(Color.rgb(6, 188, 212));
                }
            }
            if (Integer.parseInt(isManager) > 0 && hidden_title.equals("API Pull Out Count")) {
                txtItemLeft.setText(df.format(variance_quantity) + " variance");
                if (variance_quantity < 0) {
                    txtItemLeft.setTextColor(Color.rgb(252, 28, 28));
                } else {
                    txtItemLeft.setTextColor(Color.rgb(6, 188, 212));
                }
            }

            if (stockQuantity <= 0 && hidden_title.equals("API Inventory Count")) {
                linearLayout.setBackgroundColor(Color.rgb(94, 94, 94));
                txtItemName.setTextColor(Color.rgb(255, 255, 255));
                txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
            }
            if (stockQuantity <= 0 && hidden_title.equals("API Pull Out Count") && Integer.parseInt(isManager) <= 0) {
                linearLayout.setBackgroundColor(Color.rgb(94, 94, 94));
                txtItemName.setTextColor(Color.rgb(255, 255, 255));
                txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
            }

            if (hidden_title.equals("API Received Item")) {
                if (myDb4.checkItem(item, title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            } else if (hidden_title.equals("API Transfer Item")) {
                if (myDb4.checkItem(item, title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            } else if (hidden_title.equals("API Item Request")) {
                if (myDb4.checkItem(item, title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(252, 28, 28));
                }
            } else if (hidden_title.equals("API Menu Items")) {
                if (myDb.checkItem(item)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            } else if (hidden_title.equals("API Inventory Count")) {
                if (myDb3.checkItem(item, hidden_title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            } else if (hidden_title.equals("API Pull Out Count")) {
                if (myDb3.checkItem(item, hidden_title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            }
            else if (hidden_title.equals("API Issue For Production")) {
                if (myDb4.checkItem(item, title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            }
            else if (hidden_title.equals("API Received from Production")) {
                if (myDb4.checkItem(item, title)) {
                    linearLayout.setBackgroundColor(Color.rgb(252, 28, 28));
                    txtItemName.setTextColor(Color.rgb(255, 255, 255));
                    txtItemLeft.setTextColor(Color.rgb(255, 255, 255));
                }
            }
            linearLayout.addView(txtItemLeft);
        }

        if(hidden_title.equals("API Production Order List")){
            System.out.println("is issued: " + isIssued + "\n status: " + prodStatus);
            LinearLayout.LayoutParams layoutParamsItemLeft2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParamsItemLeft2.setMargins(20,  -100, 0, 10);
            TextView txtItemLeft2 = new TextView(APIReceived.this);
            txtItemLeft2.setLayoutParams(layoutParamsItemLeft2);
            txtItemLeft2.setTextSize(13);
            txtItemLeft2.setVisibility(View.VISIBLE);
            txtItemLeft2.setText(isIssued ? "✓ \n" + prodStatus : prodStatus);
            txtItemLeft2.setTextColor(Color.parseColor("#34A853"));
            linearLayout.addView(txtItemLeft2);
        }
    }

    public void anotherFunction(String finalItem, double finalPrice, Integer finalDocEntry, String finalSupplier,double quantity,int store_quantity, int auditor_quantity, int variance_quantity,String uom, String uomGroup, LinearLayout linearLayout) {
//        if (hidden_title.equals("API Received Item") || hidden_title.equals("API Menu Items") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Inventory Count") || hidden_title.equals("API Pull Out Count") || hidden_title.equals("API Issue For Production") || hidden_title.equals("API Received from Production")) {
//            Intent intent;
//            intent = new Intent(getBaseContext(), API_ItemInfo.class);
//            intent.putExtra("title", title);
//            intent.putExtra("hiddenTitle", hidden_title);
//            intent.putExtra("item", finalItem);
//            intent.putExtra("quantity", quantity);
//            intent.putExtra("uom", uom);
//            intent.putExtra("uomGroup", uomGroup);
//            if (hidden_title.equals("API Menu Items")) {
//                intent.putExtra("price", finalPrice);
//            }
//
//            SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
//            String isManager = sharedPreferences2.getString("isManager", "");
//            if(Integer.parseInt(isManager) > 0 && hidden_title.equals("API Inventory Count")){
//                intent.putExtra("store_quantity", store_quantity);
//                intent.putExtra("auditor_quantity", auditor_quantity);
//                intent.putExtra("variance_quantity",variance_quantity);
//            }
//            if(Integer.parseInt(isManager) > 0 && hidden_title.equals("API Pull Out Count")){
//                intent.putExtra("store_quantity", store_quantity);
//                intent.putExtra("auditor_quantity", auditor_quantity);
//                intent.putExtra("variance_quantity",variance_quantity);
//            }
//            startActivity(intent);
//        }else if(hidden_title.equals("API Production Order List")) {
//            AlertDialog.Builder myDialog = new AlertDialog.Builder(APIReceived.this);
//            myDialog.setCancelable(true);
//            myDialog.setTitle(finalItem);
////            System.out.println("ID: " + finalDocEntry);
//            myDialog.setPositiveButton("View Item", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
//                        return;
//                    }
//                    mLastClickTime = SystemClock.elapsedRealtime();
//                    insertProductionOrderItems(finalDocEntry, finalItem);
//                }
//            });
//            myDialog.setNegativeButton("Close Transaction", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
//                        return;
//                    }
//                    mLastClickTime = SystemClock.elapsedRealtime();
//                    AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
//                    builder.setMessage("Are you sure want to close?")
//                            .setCancelable(false)
//                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    JSONObject jsonObject = new JSONObject();
//                                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//                                    RequestBody body = RequestBody.create(JSON, jsonObject.toString());
//                                    SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
//                                    String IPaddress = sharedPreferences2.getString("IPAddress", "");
//                                    SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
//                                    String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
//                                    okhttp3.Request request = new okhttp3.Request.Builder()
//                                            .url(IPaddress + "/api/production/order/close/" + finalDocEntry)
//                                            .method("PUT", body)
//                                            .addHeader("Authorization", "Bearer " + token)
//                                            .addHeader("Content-Type", "application/json")
//                                            .build();
//                                    client.newCall(request).enqueue(new Callback() {
//                                        @Override
//                                        public void onFailure(Call call, IOException e) {
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//                                                }
//                                            });
//                                        }
//
//                                        @Override
//                                        public void onResponse(Call call, okhttp3.Response response) {
//                                            String result = "";
//                                            try {
//                                                result = response.body().string();
////                            System.out.println(result);
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            }
//                                            String finalResult = result;
//                                            APIReceived.this.runOnUiThread(() -> {
//                                                try {
//                                                    JSONObject jj = new JSONObject(finalResult);
//                                                    boolean isSuccess = jj.getBoolean("success");
//                                                    if (isSuccess) {
//                                                        Toast.makeText(getBaseContext(),  jj.getString("message"), Toast.LENGTH_SHORT).show();
//                                                        Intent intent;
//                                                        intent = new Intent(getBaseContext(), APIReceived.class);
//                                                        intent.putExtra("title", title);
//                                                        intent.putExtra("hiddenTitle", hidden_title);
//                                                        startActivity(intent);
//                                                        finish();
//                                                    } else {
//                                                        String msg = jj.getString("message");
//                                                        if (msg.equals("Token is invalid")) {
//                                                            final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
//                                                            builder.setCancelable(false);
//                                                            builder.setMessage("Your session is expired. Please login again.");
//                                                            builder.setPositiveButton("OK", (dialog, which) -> {
//                                                                pc.loggedOut(APIReceived.this);
//                                                                pc.removeToken(APIReceived.this);
//                                                                startActivity(uic.goTo(APIReceived.this, MainActivity.class));
//                                                                finish();
//                                                                dialog.dismiss();
//                                                            });
//                                                            builder.show();
//                                                        } else {
//                                                            Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
//                                                        }
//                                                    }
//                                                } catch (JSONException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            });
//                                        }
//                                    });
//                                }
//                            })
//                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.cancel();
//                                }
//                            });
//                    AlertDialog alertDialog = builder.create();
//                    alertDialog.show();
//                }
//            });
//            myDialog.show();
//        }else if(hidden_title.equals("API Item Request For Transfer")){
//            AlertDialog.Builder myDialog = new AlertDialog.Builder(APIReceived.this);
//            myDialog.setCancelable(false);
//            myDialog.setTitle("Confirmation");
////            System.out.println("ID: " + finalDocEntry);
//            myDialog.setMessage("Are you sure you want to select '" + finalItem + "'?");
//            myDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
//                        return;
//                    }
//                    mLastClickTime = SystemClock.elapsedRealtime();
//                    insertReceivedProduction(finalDocEntry, finalItem);
//                    if (hidden_title.equals("API Item Request For Transfer")) {
//                        insertReceivedItemRequest(finalDocEntry, finalItem);
//                    }
//                }
//            });
//            myDialog.setNegativeButton("No", (dialogInterface, i1) -> dialogInterface.dismiss());
//            myDialog.show();
//        }
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(APIReceived.this);
        LinearLayout layout = new LinearLayout(getBaseContext());
        layout.setPadding(40, 40, 40, 40);
        layout.setOrientation(LinearLayout.VERTICAL);
        builder.setTitle(finalItem);
        builder.setCancelable(false);

        TextView lblInformation = new TextView(getBaseContext());
        if (hidden_title.equals("API Menu Items") || hidden_title.equals("API Inventory Count")) {
            lblInformation.setText("Delivered Quantity: " + 2);
            lblInformation.setTextColor(Color.rgb(0, 0, 0));
            lblInformation.setTextSize(15);
            lblInformation.setGravity(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(lblInformation);

            if(hidden_title.equals("API Menu Items")){
                TextView lblPrice = new TextView(getBaseContext());
                lblPrice.setText("₱" + finalPrice);
                lblPrice.setTextColor(Color.rgb(0, 0, 0));
                lblPrice.setTextSize(15);
                lblPrice.setGravity(View.TEXT_ALIGNMENT_CENTER);
                layout.addView(lblPrice);
            }
        }

        TextInputLayout lblQuantity = new TextInputLayout(APIReceived.this, null, R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        LinearLayout.LayoutParams layoutParamsLblQuantity = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsLblQuantity.setMargins(0, 5, 0, 5);
        lblQuantity.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        lblQuantity.setBoxCornerRadii(5, 5, 5, 5);
        lblQuantity.setLayoutParams(layoutParamsLblQuantity);

        TextInputEditText txtQuantity = new TextInputEditText(lblQuantity.getContext());
        LinearLayout.LayoutParams layoutParamsTxtQuantity = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        txtQuantity.setLayoutParams(layoutParamsTxtQuantity);
        txtQuantity.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        txtQuantity.setHint("*Enter Quantity");

        TextView lblVariance = new TextView(getBaseContext());
        txtQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double deliveredQty = lblInformation == null ? 0 : lblInformation.getText().toString().trim().isEmpty() ? 0.00 : Double.parseDouble(lblInformation.getText().toString().replace("Delivered Quantity: ", "").trim());
                    double inputQty = lblQuantity.getEditText().getText().toString().trim().isEmpty() || lblQuantity.getEditText().getText().toString().trim().equals(".") ? 0.00 : Double.parseDouble(lblQuantity.getEditText().getText().toString().trim());
                    double variance = inputQty - deliveredQty;
                    if (lblVariance != null) {
                        lblVariance.setText("Variance: " + variance);
                        if (variance == 0) {
                            lblVariance.setTextColor(Color.BLACK);
                        } else if (variance > 0) {
                            lblVariance.setTextColor(Color.BLUE);
                        } else if (variance < 0) {
                            lblVariance.setTextColor(Color.RED);
                        }
                    }
                } catch (Exception ex) {
                    Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        lblQuantity.addView(txtQuantity);
        lblQuantity.getEditText().setFocusable(true);

        layout.addView(lblQuantity);
        if (hidden_title.equals("API Inventory Count")) {
            lblVariance.setText("Variance: " + (0 - 2));
            lblVariance.setTextColor(Color.RED);
            lblVariance.setTextSize(15);
            lblVariance.setGravity(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(lblVariance);
        }


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Add to Cart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                double qty = 0.00;
                try{
                    qty = Double.parseDouble(lblQuantity.getEditText().getText().toString());
                }catch (NumberFormatException ex){
                    qty = 0.00;
                }
                if(qty <= 0 && (hidden_title.equals("API Received Item") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Issue For Production") || hidden_title.equals("Received from Production"))){
                    Toast.makeText(getBaseContext(), "Please input atleast 1", Toast.LENGTH_SHORT).show();
                }else{
                    boolean isInserted = false;
                    if(hidden_title.equals("API Received Item") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Issue For Production") || hidden_title.equals("Received from Production")){
                        isInserted = myDb4.insertData(finalItem, qty, title, 1,uom);
                    }else if(hidden_title.equals("API Inventory Count")){
                        isInserted = myDb3.insertData("", "", finalItem, qty, qty, 0, "", 0,hidden_title,1,uom,0,0,0,0);
                    }

                    if(isInserted){
                        Toast.makeText(getBaseContext(), "Item Added",Toast.LENGTH_SHORT).show();
                        linearLayout.setBackgroundColor(Color.RED);
                        int count = linearLayout.getChildCount();
                        View v = null;
                        for(int i=0; i<count; i++) {
                            TextView tv =  (TextView)linearLayout.getChildAt(i);
                            tv.setTextColor(Color.WHITE);
                        }
                    }else{
                        Toast.makeText(getBaseContext(), "Item not Added",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builder.setView(layout);
        builder.show();
    }

    public void insertProductionOrderItems(int id, String referenceNumber){
        String appendURL= "/api/production/order/details/" + id;
//        System.out.println("URL: " +  appendURL);
        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        String URL = IPaddress + appendURL;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            try {
                final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray jsonArray;
                            jsonArray = response.getJSONArray("data");
//                            System.out.println("array: " + jsonArray);
                            int countError = 0;
                            String selectedSapNumber = referenceNumber;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String fromBranch,
                                        itemName,
                                        toBranch,
                                        uom;
                                Double quantity;

                                fromBranch = jsonObject.getString("whsecode");
                                itemName = jsonObject.getString("item_code");
                                quantity = jsonObject.getDouble("planned_qty");
                                toBranch = jsonObject.getString("whsecode");
                                uom = jsonObject.getString("uom");
                                int itemID = jsonObject.getInt("id");
                                int objtype = jsonObject.getInt("objtype");
                                double int_quantity = jsonObject.getDouble("planned_qty");
                                int isClosed_int = jsonObject.isNull("close") ? 0 : jsonObject.getBoolean("close") ? 1 : 0;
                                int int_received_quantity = jsonObject.isNull("received_qty") ? 0 : jsonObject.getInt("received_qty");
                                boolean isSuccess = myDb3.insertData(referenceNumber, fromBranch, itemName, quantity, int_quantity, 0, toBranch, id,hidden_title,0,uom,int_received_quantity,itemID,objtype,isClosed_int);
                                if (!isSuccess) {
                                    countError += 1;
                                }
                            }

                            if (countError <= 0) {
                                Toast.makeText(APIReceived.this, "'" + selectedSapNumber + "' added", Toast.LENGTH_SHORT).show();
                                loadData();
                            } else {
                                Toast.makeText(APIReceived.this, "'" + id + "' not added", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String msg = response.getString("message");
                            if (msg.equals("Token is invalid")) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                builder.setCancelable(false);
                                builder.setMessage("Your session is expired. Please login again.");
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    pc.loggedOut(APIReceived.this);
                                    pc.removeToken(APIReceived.this);
                                    startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            } else {
                                Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getBaseContext(), "Connection Timeout", Toast.LENGTH_SHORT).show()) {
                    @Override
                    public Map<String, String> getHeaders() {
                        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "Bearer " + token);
                        return params;
                    }
                };
                mQueue.add(request);
            }catch (Exception ex){
                Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            }
        },500);
    }


    public void insertReceivedProduction(int id, String referenceNumber){
        String appendURL= "/api/production/order/details/" + id + "?mode=receive";
//        System.out.println("URL: " +  appendURL);
        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        String URL = IPaddress + appendURL;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            try {
                final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray jsonArray;
                            jsonArray = response.getJSONArray("data");
//                            System.out.println("array: " + jsonArray);
                            int countError = 0;
                            String selectedSapNumber = referenceNumber;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String fromBranch,
                                        itemName,
                                        toBranch,
                                        uom;
                                Double quantity;

                                fromBranch = jsonObject.getString("whsecode");
                                itemName = jsonObject.getString("item_code");
                                quantity = jsonObject.getDouble("planned_qty");
                                toBranch = jsonObject.getString("whsecode");
                                uom = jsonObject.getString("uom");
                                double int_quantity = jsonObject.getDouble("planned_qty");
                                int int_received_quantity = jsonObject.isNull("received_qty") ? 0 : jsonObject.getInt("received_qty");
                                boolean isSuccess = myDb3.insertData(referenceNumber, fromBranch, itemName, quantity, int_quantity, 0, toBranch, id,hidden_title,0,uom,int_received_quantity,0,0,0);
                                if (!isSuccess) {
                                    countError += 1;
                                }
                            }

                            if (countError <= 0) {
                                Toast.makeText(APIReceived.this, "'" + selectedSapNumber + "' added", Toast.LENGTH_SHORT).show();
                                loadData();
                            } else {
                                Toast.makeText(APIReceived.this, "'" + id + "' not added", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String msg = response.getString("message");
                            if (msg.equals("Token is invalid")) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                builder.setCancelable(false);
                                builder.setMessage("Your session is expired. Please login again.");
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    pc.loggedOut(APIReceived.this);
                                    pc.removeToken(APIReceived.this);
                                    startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            } else {
                                Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getBaseContext(), "Connection Timeout", Toast.LENGTH_SHORT).show()) {
                    @Override
                    public Map<String, String> getHeaders() {
                        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "Bearer " + token);
                        return params;
                    }
                };
                mQueue.add(request);
            }catch (Exception ex){
                Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            }
        },500);
    }

    public void insertReceivedItemRequest(int id, String referenceNumber){
        String appendURL= "/api/inv/item_request/details/" + id;
//        System.out.println("URL: " +  appendURL);
        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        String URL = IPaddress + appendURL;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            try {
                final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject jsonObjectData;
                            jsonObjectData = response.getJSONObject("data");
                            int countError = 0;
                            JSONArray jsonArray = jsonObjectData.getJSONArray("request_rows");
                            String selectedSapNumber = referenceNumber;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String fromBranch,
                                        itemName,
                                        toBranch,
                                        uom;
                                Double quantity;
                                int item_id = jsonObject.isNull("id") ? 0 : jsonObject.getInt("id");
                                int obj_type = jsonObject.isNull("objtype") ? 0 : jsonObject.getInt("objtype");
                                fromBranch = jsonObject.getString("from_whse");
                                itemName = jsonObject.getString("item_code");
                                quantity = jsonObject.getDouble("quantity");
                                toBranch = jsonObject.getString("to_whse");
                                uom = jsonObject.getString("uom");
                                double int_quantity = jsonObject.getDouble("quantity");
                                int int_received_quantity = jsonObject.isNull("deliverqty") ? 0 : jsonObject.getInt("deliverqty");
                                boolean isSuccess = myDb3.insertData(referenceNumber, fromBranch, itemName, quantity, int_quantity, 0, toBranch, id,hidden_title,0,uom,int_received_quantity,item_id,obj_type,0);
                                if (!isSuccess) {
                                    countError += 1;
                                }
                            }

                            if (countError <= 0) {
                                Toast.makeText(APIReceived.this, "'" + selectedSapNumber + "' added", Toast.LENGTH_SHORT).show();
                                loadData();
                            } else {
                                Toast.makeText(APIReceived.this, "'" + id + "' not added", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String msg = response.getString("message");
                            if (msg.equals("Token is invalid")) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                builder.setCancelable(false);
                                builder.setMessage("Your session is expired. Please login again.");
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    pc.loggedOut(APIReceived.this);
                                    pc.removeToken(APIReceived.this);
                                    startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            } else {
                                Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getBaseContext(), "Connection Timeout", Toast.LENGTH_SHORT).show()) {
                    @Override
                    public Map<String, String> getHeaders() {
                        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "Bearer " + token);
                        return params;
                    }
                };
                mQueue.add(request);
            }catch (Exception ex){
                Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            }
        },500);
    }

    public void insertIssueProduction(int id, String reference){
        String appendURL= (hidden_title.equals("API Issue For Production") ? "/api/production/item_to_issue/get_all/" : "/api/production/issue_for_prod/details/") + id + (hidden_title.equals("API Issue For Production") ? "" : "?mode=confirm");
        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPAddress = sharedPreferences2.getString("IPAddress", "");

        String URL = IPAddress + appendURL;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            try {
                final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
                    try {
                        if (response.getBoolean("success")) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            String currentDate = sdf.format(new Date());
                            boolean isSuccess = myDb9.insertData(appendURL,"GET", "Issue For Production", response.toString(),currentDate);
                            if(isSuccess){
                                Toast.makeText(getBaseContext(), reference + " added" , Toast.LENGTH_SHORT).show();
                                loadData();
                            }
                        } else {
                            String msg = response.getString("message");
                            if (msg.equals("Token is invalid")) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(APIReceived.this);
                                builder.setCancelable(false);
                                builder.setMessage("Your session is expired. Please login again.");
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    pc.loggedOut(APIReceived.this);
                                    pc.removeToken(APIReceived.this);
                                    startActivity(uic.goTo(APIReceived.this, MainActivity.class));
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            } else {
                                Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getBaseContext(), "Connection Timeout", Toast.LENGTH_SHORT).show()) {
                    @Override
                    public Map<String, String> getHeaders() {
                        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "Bearer " + token);
                        return params;
                    }
                };
                mQueue.add(request);
            }catch (Exception ex){
                Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            }
        },500);
    }

    public String cutWord(String value, int limit){
        String result;
        int limitTo = limit - 3;
        result = (value.length() > limit ? value.substring(0, limitTo) + "..." : value);
        return result;
    }

    public void navigateDone() {
//        if (hidden_title.equals("API Received Item") && spinnerType.getSelectedItem().toString() == "Select Type") {
//            Toast.makeText(getBaseContext(), "Please select Type", Toast.LENGTH_SHORT).show();
//            return;
//        }
        Intent intent;
        intent = new Intent(getBaseContext(), (hidden_title.equals("API Menu Items") ? ShoppingCart.class : API_SelectedItems.class));
        intent.putExtra("title", title);
//        if(hidden_title.equals("API Received Item") && spinnerType.getSelectedItem().toString() != "Select Type" && !spinnerType.getSelectedItem().toString().isEmpty()){
//            intent.putExtra("type", spinnerType.getSelectedItem().toString());
//        }
        intent.putExtra("selectedWarehouse", gSelectedBranch);
        intent.putExtra("hiddenTitle", hidden_title);
        startActivity(intent);
    }
}