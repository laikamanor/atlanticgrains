package com.example.atlanticgrains;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
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
import com.example.atlanticgrains.Helper.FragmentNavigationManager_API_ItemInfo;
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
import okhttp3.RequestBody;
import okhttp3.Response;

public class API_ItemInfo extends AppCompatActivity {
    String title = "", hidden_title = "", itemName = "";
    TextView lblItem, lblPrice, lblReceivedQuantity, lblVariance, lblPercent;
    EditText txtDiscount, txtTotalPrice, txtQuantity;
    CheckBox checkFree;
    Button btnPlus, btnMinus, btnAddCart,btnBack;

    DatabaseHelper4 myDb4;
    DatabaseHelper3 myDb3;
    DatabaseHelper myDb;
    Double deliveredQuantity;
    int receivedQuantity;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listTitle;
    private Map<String, List<String>> listChild;
    private NavigationManager navigationManager;

    prefs_class pc = new prefs_class();
    ui_class uic = new ui_class();
    navigation_class navc = new navigation_class();

    Menu menu;
    long mLastClickTime;

    static boolean isSubmit = false;
    private OkHttpClient client;
    int uomGroup = 0;
    ProgressBar progressBar;
    Spinner spinnerAltUom;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_p_i__item_info);
        lblItem = findViewById(R.id.lblItemName);
        lblPrice = findViewById(R.id.lblPrice);
        lblReceivedQuantity = findViewById(R.id.lblReceivedQuantity);
        lblVariance = findViewById(R.id.lblVariance);
        lblPercent = findViewById(R.id.lblPercent);
        txtDiscount = findViewById(R.id.txtDisount);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        checkFree = findViewById(R.id.checkFree);
        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);
        txtQuantity = findViewById(R.id.txtQuantity);
        btnAddCart = findViewById(R.id.btnAddCart);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        spinnerAltUom = findViewById(R.id.spinnerZXC);
        progressBar.setVisibility(View.GONE);
        deliveredQuantity = getIntent().getDoubleExtra("deliveredQuantity", 0);
        receivedQuantity = getIntent().getIntExtra("receivedQuantity", 0);
        client = new OkHttpClient();

        myDb = new DatabaseHelper(this);
        myDb4 = new DatabaseHelper4(this);
        myDb3 = new DatabaseHelper3(this);

        title = getIntent().getStringExtra("title");
        hidden_title = getIntent().getStringExtra("hiddenTitle");
        itemName = getIntent().getStringExtra("item");

        if(hidden_title.equals("API Received Item") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Issue For Production") || hidden_title.equals("API Received from Production")) {
//            System.out.println("uom group: " + getIntent().getStringExtra("uomGroup"));
            uomGroup = Integer.parseInt(getIntent().getStringExtra("uomGroup"));
            loadAltUom(uomGroup);
        }

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        expandableListView = (ExpandableListView)findViewById(R.id.navList);
        navigationManager = FragmentNavigationManager_API_ItemInfo.getmInstance(this);

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
        getSupportActionBar().setTitle(getString(R.string.app_name));

        btnMinus.setOnClickListener(view -> {
            txtQuantity.setText(minusPlus("-"));
            txtQuantity.setSelection(txtQuantity.getText().length());
        });
        btnPlus.setOnClickListener(view -> {
            txtQuantity.setText(minusPlus("+"));
            txtQuantity.setSelection(txtQuantity.getText().length());
        });

        btnAddCart.setOnClickListener(view -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            progressBar.setVisibility(View.VISIBLE);
            btnAddCart.setEnabled(false);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
//            int qty = (txtQuantity.getText().toString().isEmpty() ? 0 : Integer.parseInt(txtQuantity.getText().toString()));
                    if ((txtQuantity.getText().toString().isEmpty()) && (hidden_title.equals("API Menu Items") || hidden_title.equals("API Received Item") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API System Transfer Item") || hidden_title.equals("API Item Request"))) {
                        Toast.makeText(getBaseContext(), "Input atleast 1 quantity", Toast.LENGTH_SHORT).show();
                        txtQuantity.requestFocus();
//                        progressBar.setVisibility(View.GONE);
//                        btnAddCart.setEnabled(true);
//            }else if (qty <= 0) {
//                Toast.makeText(getBaseContext(), "Input atleast 1 quantity", Toast.LENGTH_SHORT).show();
                    }else if((Double.parseDouble(txtQuantity.getText().toString()) <= 0) && (hidden_title.equals("API Menu Items") || hidden_title.equals("API Received Item") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API System Transfer Item") || hidden_title.equals("API Item Request"))){
                        Toast.makeText(getBaseContext(), "Input atleast 1 quantity", Toast.LENGTH_SHORT).show();
                        txtQuantity.requestFocus();
                    }
                    else {
                        double quantity = (txtQuantity.getText().toString().isEmpty() ? 0.00 : Double.parseDouble(txtQuantity.getText().toString()));
                        double int_quantity =  (txtQuantity.getText().toString().isEmpty() ? 0 : Double.parseDouble(txtQuantity.getText().toString()));
//                double double_quantity = getIntent().getDoubleExtra("quantity",0.0);
                        boolean isInserted = false;
                        String uom = getIntent().getStringExtra("uom");
                        if(hidden_title.equals("API Received Item") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Issue For Production") || hidden_title.equals("API Received from Production")){
                            isInserted = myDb4.insertData(itemName, quantity, title, 1,uom);
                        }else if(hidden_title.equals("API Received from SAP")){
                            int id = getIntent().getIntExtra("id",0);
                            isInserted = myDb3.updateSelected(Integer.toString(id),1, quantity);
                        }else if(hidden_title.equals("API System Transfer Item")){
                            int id = getIntent().getIntExtra("id",0);
                            isInserted = myDb3.updateSelected(Integer.toString(id),1, quantity);
                        }
                        else if(hidden_title.equals("API Item Request For Transfer")){
                            int id = getIntent().getIntExtra("id",0);
                            isInserted = myDb3.updateSelected(Integer.toString(id),1, quantity);
                        }
                        else if(hidden_title.equals("API Inventory Count") || hidden_title.equals("API Pull Out Count")){
                            double double_quantity =  getIntent().getDoubleExtra("quantity",0.0);
                            isInserted = myDb3.insertData("", "", itemName, double_quantity, int_quantity, 0, "", 0,hidden_title,1,uom,0,0,0,0);
                        }
                        else if(hidden_title.equals("API Menu Items")){
                            double price = Double.parseDouble(lblPrice.getText().toString().replace("₱",""));
                            double priceBefore = quantity * price;
                            double doubleTotalPrice = Double.parseDouble(txtTotalPrice.getText().toString());
                            double calculatedDiscount = ((priceBefore - doubleTotalPrice) / priceBefore) * 100;
                            if(calculatedDiscount > 10){
                                Toast.makeText(getBaseContext(), "10% is the maximum discount!", Toast.LENGTH_SHORT).show();
                                txtDiscount.requestFocus();
                                txtDiscount.setSelection(txtDiscount.getText().length());
                            }else{
                                double discountPercent = (txtDiscount.getText().toString().isEmpty() ? 0.00 : Double.parseDouble(txtDiscount.getText().toString()));
                                int isFree = (checkFree.isChecked() ? 1 : 0);
                                double totalPrice = (txtTotalPrice.getText().toString().isEmpty() ? 0.00 : Double.parseDouble(txtTotalPrice.getText().toString()));
                                isInserted = myDb.insertData(quantity, discountPercent, price, isFree, totalPrice, itemName, "Main Inventory",uom);
                            }
                        }
                        if (isInserted) {
                            isSubmit = true;
                            Toast.makeText(getBaseContext(), "Item Added", Toast.LENGTH_SHORT).show();
                            closeKeyboard();
                            finish();
                        } else {
                            Toast.makeText(getBaseContext(), "Item Not Added", Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    btnAddCart.setEnabled(true);
                }
            },500);
        });

        txtQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                double received_quantity;
                double quantity;
                if (txtQuantity.getText().toString().isEmpty()) {
                    quantity = 0.00;
                } else {
                    if(txtQuantity.getText().toString().substring(0,1).equals(".")){
                        quantity = Double.parseDouble( "0" + txtQuantity.getText().toString().trim());
                    }else{
                        quantity = Double.parseDouble(txtQuantity.getText().toString().trim());
                    }
                }

                if(hidden_title.equals("API Menu Items")){
                    double price = Double.parseDouble(lblPrice.getText().toString().replace("₱",""));
                    double priceBefore = price * quantity;
                    double discount = (txtDiscount.getText().toString().isEmpty() ? 0.00 : Double.parseDouble(txtDiscount.getText().toString()));
                    if(!checkFree.isChecked()){
                        if(discount < 0){
                            txtTotalPrice.setText(Double.toString(priceBefore));
                        }else{
                            double discountedTotalPrice = (priceBefore - (discount / 100) * priceBefore);
                            txtTotalPrice.setText(Double.toString(discountedTotalPrice));
                        }
                    }else {
                        txtTotalPrice.setText("0.00");
                        txtDiscount.setText("0");
                    }
                }

                if(hidden_title.equals("API Received from SAP") || hidden_title.equals("API System Transfer Item")){
                    received_quantity = Double.parseDouble(lblReceivedQuantity.getText().toString().replace("Delivered Qty.: ", ""));
                    double variance = quantity - received_quantity;
//                    System.out.println("Variance: " +variance);
                    lblVariance.setText("Variance: " + variance);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtQuantity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    btnAddCart.callOnClick();
                    return true;
                }
                return false;
            }
        });


        txtDiscount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(hidden_title.equals("API Menu Items")) {
                    if (txtDiscount.hasFocus()) {
                        double price = Double.parseDouble(lblPrice.getText().toString().replace("₱",""));
                        double discountPercent;
//                                , getPrice = itemc.returnItemNamePrice(ItemInfo.this, lblItem.getText().toString());
                        double qty = (txtQuantity.getText().toString().isEmpty() ? 0.00 : Double.parseDouble(txtQuantity.getText().toString()));
                        double priceBefore = qty * price;
                        try {
                            if (Double.parseDouble(txtDiscount.getText().toString()) > 10) {
                                txtDiscount.setText("10");
                                txtDiscount.setSelection(txtDiscount.getText().length() - 1);
                            }
                            if (txtDiscount.getText().toString().isEmpty()) {
                                txtDiscount.setSelection(txtDiscount.getText().length());
                            } else if (Double.parseDouble(txtQuantity.getText().toString()) < 0) {
                                txtDiscount.setText("0");
                                txtDiscount.setSelection(txtDiscount.getText().length());
                            } else if (Double.parseDouble(txtQuantity.getText().toString()) > 0) {
                                double totalAmount;
                                if (!checkFree.isChecked()) {
                                    discountPercent = Double.parseDouble(txtDiscount.getText().toString());
                                    totalAmount = (priceBefore - (discountPercent / 100) * priceBefore);
                                    txtTotalPrice.setText(Double.toString(totalAmount));

                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            if (txtDiscount.getText().toString().equals("")) {
                                txtTotalPrice.setText(Double.toString(priceBefore));
                            }
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        txtDiscount.setOnFocusChangeListener((view, b) -> {
            if(hidden_title.equals("API Menu Items")) {
                if (!b) {
                    if (txtDiscount.getText().toString().isEmpty()) {
                        txtDiscount.setText("0");
                    }
                }
            }
        });

        txtTotalPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(hidden_title.equals("API Menu Items")) {
                    double price = Double.parseDouble(lblPrice.getText().toString().replace("₱",""));
//                    double getPrice = itemc.returnItemNamePrice(ItemInfo.this, lblItem.getText().toString());
                    double quantity = (txtQuantity.getText().toString().isEmpty()) ? 0.00  : Double.parseDouble(txtQuantity.getText().toString());
                    double priceBefore = quantity * price;
                    if (txtTotalPrice.hasFocus()) {
                        try {
                            if (!checkFree.isChecked()) {
                                 if (Double.parseDouble(txtTotalPrice.getText().toString()) > priceBefore) {
                                    double doubleTotalPrice = Double.parseDouble(txtTotalPrice.getText().toString());
                                    double calculatedDiscount = ((priceBefore - doubleTotalPrice) / priceBefore) * 100;
                                    txtDiscount.setText(Double.toString(calculatedDiscount));
                                    txtTotalPrice.setText(Double.toString(priceBefore));
                                    txtTotalPrice.setSelection(txtTotalPrice.getText().length());
                                } else {
                                    double doubleTotalPrice = Double.parseDouble(txtTotalPrice.getText().toString());
                                    double calculatedDiscount = ((priceBefore - doubleTotalPrice) / priceBefore) * 100;
                                    txtDiscount.setText(Double.toString(calculatedDiscount));
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtTotalPrice.setOnFocusChangeListener((view, b) -> {
            if(hidden_title.equals("API Menu Items")) {
                if (!b) {
                    if (txtTotalPrice.getText().toString().isEmpty()) {
                        double price = Double.parseDouble(lblPrice.getText().toString().replace("₱",""));
//                            double getPrice = itemc.returnItemNamePrice(ItemInfo.this, lblItem.getText().toString());
                        double priceBefore = Double.parseDouble(txtQuantity.getText().toString()) * price;
                        txtTotalPrice.setText(Double.toString(priceBefore));
                        txtDiscount.setText("0.00");
                    }
                }
            }
        });

        checkFree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    txtDiscount.setText("0");
                    txtTotalPrice.setText("0.00");
                    txtDiscount.setEnabled(false);
                    txtTotalPrice.setEnabled(false);
                } else {
                    double price = Double.parseDouble(lblPrice.getText().toString().replace("₱",""));
//                        double getPrice = itemc.returnItemNamePrice(ItemInfo.this, lblItem.getText().toString());
                    double priceBefore = price * Double.parseDouble(txtQuantity.getText().toString());
                    txtTotalPrice.setText(Double.toString(priceBefore));
                    txtDiscount.setEnabled(true);
                    txtTotalPrice.setEnabled(true);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>" + title + " </font>"));
        lblItem.setText(itemName);

        if(hidden_title.equals("API Item Request For Transfer")){
            TextView textView2 = findViewById(R.id.textView2);
            textView2.setVisibility(View.GONE);
            spinnerAltUom.setVisibility(View.GONE);
            lblReceivedQuantity.setText("Delivered Quantity: " + deliveredQuantity);
            lblReceivedQuantity.setVisibility(View.VISIBLE);
        }
    }


    public void loadAltUom(int id){
//        JSONObject jsonObject = new JSONObject();
//        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(IPaddress + "/api/uom_grp/alt_uom/get_all/" + id)
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
                List<String> items = new ArrayList<>();
                String finalResult = result;
                API_ItemInfo.this.runOnUiThread(() -> {
                    try {
                        if(finalResult.substring(0,1).equals("{")){
                            JSONObject jj = new JSONObject(finalResult);
                            boolean isSuccess = jj.getBoolean("success");
                            if (isSuccess) {
                                JSONArray jsonArray;
                                String uomDefault = "";
                                jsonArray = jj.getJSONArray("data");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObjectData = jsonArray.getJSONObject(i);
                                    items.add(jsonObjectData.getString("alt_uom"));
                                    if(jsonObjectData.getBoolean("default")){
                                        uomDefault = jsonObjectData.getString("alt_uom");
                                    }
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(API_ItemInfo.this, android.R.layout.simple_spinner_item, items);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerAltUom.setAdapter(adapter);
                                spinnerAltUom.setSelection(adapter.getPosition(uomDefault));
                            } else {
                                String msg = jj.getString("message");
                                if (msg.equals("Token is invalid")) {
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_ItemInfo.this);
                                    builder.setCancelable(false);
                                    builder.setMessage("Your session is expired. Please login again.");
                                    builder.setPositiveButton("OK", (dialog, which) -> {
                                        pc.loggedOut(API_ItemInfo.this);
                                        pc.removeToken(API_ItemInfo.this);
                                        startActivity(uic.goTo(API_ItemInfo.this, MainActivity.class));
                                        finish();
                                        dialog.dismiss();
                                    });
                                    builder.show();
                                } else {
                                    Toast.makeText(getBaseContext(), "Error \n" + msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else {
                            Toast.makeText(getBaseContext(), finalResult, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
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

    public void changePassword(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(API_ItemInfo.this);
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(API_ItemInfo.this);
                    builder.setMessage("Are you sure want to submit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    API_ItemInfo.myChangePassword myChangePassword = new API_ItemInfo.myChangePassword(txtPassword.getText().toString().trim());
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
        LoadingDialog loadingDialog = new LoadingDialog(API_ItemInfo.this);
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
                ex.printStackTrace();
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(API_ItemInfo.this);
                        builder.setMessage("We redirect you to Login Page")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pc.loggedOut(API_ItemInfo.this);
                                        pc.removeToken(API_ItemInfo.this);
                                        startActivity(uic.goTo(API_ItemInfo.this, MainActivity.class));
                                        finish();
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismissDialog();
                    }
                });
            }
        }
    }


//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();

        isSubmit = false;

//        if(this.getWindow().getDecorView().getRootView().isShown()){
//
//        }


        if (hidden_title.equals("API Received Item") || hidden_title.equals("API Received from SAP") || hidden_title.equals("API Transfer Item") || hidden_title.equals("API Item Request") || hidden_title.equals("API Inventory Count") || hidden_title.equals("API Pull Out Count") || hidden_title.equals("API System Transfer Item") || hidden_title.equals("API Received from Production") || hidden_title.equals("API Item Request For Transfer") || hidden_title.equals("API Issue For Production") || hidden_title.equals("API Received from Production")) {
            checkFree.setVisibility(View.GONE);
            lblPrice.setVisibility(View.GONE);
            lblPercent.setVisibility(View.GONE);
            txtDiscount.setVisibility(View.GONE);
            txtTotalPrice.setVisibility(View.GONE);
            if (hidden_title.equals("API Received from SAP") || hidden_title.equals("API System Transfer Item")) {
                lblReceivedQuantity.setVisibility(View.VISIBLE);
                lblVariance.setVisibility((hidden_title.equals("API Received from Production") ? View.GONE : View.VISIBLE));
                lblReceivedQuantity.setText((hidden_title.equals("API Received from Production") ? "Received Qty. :" + receivedQuantity : "Delivered Qty.: " + deliveredQuantity));
            }else if(hidden_title.equals("API Inventory Count")){
                SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
                String isManager = sharedPreferences2.getString("isManager", "");
                if(Integer.parseInt(isManager) > 0){
                    lblReceivedQuantity.setVisibility(View.VISIBLE);
                    lblVariance.setVisibility(View.GONE);
                    lblReceivedQuantity.setText("Sales: " + getIntent().getIntExtra("store_quantity",0) + "\n" + "Auditor: " + getIntent().getIntExtra("auditor_quantity",0));
//                    lblVariance.setText("Variance: " + getIntent().getIntExtra("variance_quantity",0));
                }else{
                    lblReceivedQuantity.setVisibility(View.GONE);
                    lblVariance.setVisibility(View.GONE);
                }
            }else if(hidden_title.equals("API Pull Out Count")){
                SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
                String isManager = sharedPreferences2.getString("isManager", "");
                if(Integer.parseInt(isManager) > 0){
                    lblReceivedQuantity.setVisibility(View.VISIBLE);
                    lblVariance.setVisibility(View.GONE);
                    lblReceivedQuantity.setText("Sales: " + getIntent().getIntExtra("store_quantity",0) + "\n" + "Auditor: " + getIntent().getIntExtra("auditor_quantity",0));
//                    lblVariance.setText("Variance: " + getIntent().getIntExtra("variance_quantity",0));
                }else{
                    lblReceivedQuantity.setVisibility(View.GONE);
                    lblVariance.setVisibility(View.GONE);
                }
            }
            else {
                lblReceivedQuantity.setVisibility(View.GONE);
                lblVariance.setVisibility(View.GONE);
                lblReceivedQuantity.setText("Received Qty.: " + deliveredQuantity);
            }
        } else {
            checkFree.setVisibility(View.GONE);
            lblPrice.setVisibility(View.VISIBLE);
            lblPrice.setText("₱" + getIntent().getDoubleExtra("price",0.00));
            lblReceivedQuantity.setVisibility(View.GONE);
            lblVariance.setVisibility(View.GONE);
            lblPercent.setVisibility(View.VISIBLE);
            txtDiscount.setVisibility(View.VISIBLE);
            txtTotalPrice.setVisibility(View.VISIBLE);
        }
        txtQuantity.requestFocus();
        showKeyboard();

        if(hidden_title.equals("API Item Request For Transfer")){
            lblReceivedQuantity.setVisibility(View.VISIBLE);
            lblVariance.setVisibility(View.GONE);
            lblReceivedQuantity.setText("Received Qty.: " + deliveredQuantity);
        }
    }



    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @SuppressLint("SetTextI18n")
    public String minusPlus(String operator) {
        double quantity;
        if(txtDiscount.getText().toString().isEmpty() && !checkFree.isChecked()){
            txtDiscount.setText("0");
        }
        if(txtTotalPrice.getText().toString().isEmpty() && !checkFree.isChecked()){
            txtTotalPrice.setText("0.00");
        }
        if (txtQuantity.getText().toString().isEmpty()) {
            quantity = 0;
        } else {
            quantity = Double.parseDouble(txtQuantity.getText().toString());
        }
        if (operator.equals("+")) {
            quantity += 1;
        } else {
            if (quantity > 0) {
                quantity -= 1;
            }
        }
        if (hidden_title.equals("API Received from SAP")) {
            double received_quantity = Double.parseDouble(lblReceivedQuantity.getText().toString().replace("Delivered Qty.: ", ""));
            double variance = quantity - received_quantity;
            lblVariance.setText("Variance: " + variance);
        }
        return Double.toString(quantity);
    }

    public  void onBtnLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pc.loggedOut(API_ItemInfo.this);
                        pc.removeToken(API_ItemInfo.this);
                        startActivity(uic.goTo(API_ItemInfo.this, MainActivity.class));
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