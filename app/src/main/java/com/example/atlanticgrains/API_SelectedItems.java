package com.example.atlanticgrains;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.text.Html;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.atlanticgrains.Adapter.CustomExpandableListAdapter;
import com.example.atlanticgrains.Interface.NavigationManager;
import com.example.atlanticgrains.Helper.FragmentNavigationManager_API_SelectedItems;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
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

public class API_SelectedItems extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    DatabaseHelper4 myDb4;
    DatabaseHelper3 myDb3;
    DatabaseHelper7 myDb7;
    DatabaseHelper8 myDb8;
    long mLastClickTime = 0;
    Button btnProceed,btnBack;
    DecimalFormat df = new DecimalFormat("#,###");
    String title,hiddenTitle;
    private OkHttpClient client;
    private RequestQueue mQueue;


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
    TextView lblSelectedDate;

    Menu menu;

    String gBranch = "";
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"WrongConstant", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_p_i__selected_items);

        myDb = new DatabaseHelper(this);
        myDb8 = new DatabaseHelper8(this);
        client = new OkHttpClient();
        myDb4 = new DatabaseHelper4(this);
        myDb3 = new DatabaseHelper3(this);
        myDb7 = new DatabaseHelper7(this);
        btnProceed = findViewById(R.id.btnProceed);
        btnBack = findViewById(R.id.btnBack);
        mQueue = Volley.newRequestQueue(this);

        hiddenTitle = getIntent().getStringExtra("hiddenTitle");

        if(hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Transfer Item") || hiddenTitle.equals("API Item Request") || hiddenTitle.equals("API Issue For Production") || hiddenTitle.equals("API Received from Production")){
            while (gBranch.isEmpty()) {
                try {
                    while (gBranch.isEmpty()){
                        gBranch = loadWarehouse();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        expandableListView = (ExpandableListView)findViewById(R.id.navList);
        navigationManager = FragmentNavigationManager_API_SelectedItems.getmInstance(this);
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
        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>" + title + " </font>"));



        btnProceed.setOnClickListener(view -> {
            final AlertDialog.Builder myDialog = new AlertDialog.Builder(API_SelectedItems.this);
            myDialog.setCancelable(false);
            LinearLayout layout = new LinearLayout(getBaseContext());
            layout.setPadding(40, 40, 40, 40);
            layout.setOrientation(LinearLayout.VERTICAL);

            TextView lblFromSelectedBranch = new TextView(getBaseContext());
            TextView lblToSelectedBranch = new TextView(getBaseContext());
            TextInputLayout textInputLayoutSAPNumber = new TextInputLayout(API_SelectedItems.this);
            EditText txtSupplier = new EditText(getBaseContext());
            LinearLayout.LayoutParams layoutParamsBranch = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if(hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Transfer Item") || hiddenTitle.equals("API Item Request") || hiddenTitle.equals("API Issue For Production") || hiddenTitle.equals("API Received from Production")){
                    TextView lblFromBranch = new TextView(getBaseContext());
                    lblFromBranch.setText((hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Item Request") || hiddenTitle.equals("API Issue For Production")) ? "*From Warehouse:" : "*To Warehouse:");
                    lblFromBranch.setTextColor(Color.rgb(0,0,0));
                    lblFromBranch.setTextSize(15);
                    lblFromBranch.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    layout.addView(lblFromBranch);

                    LinearLayout layoutFromBranch = new LinearLayout(getBaseContext());
                    LinearLayout.LayoutParams layoutParamsFromBranch = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParamsFromBranch.setMargins(20,0,0,20);
                    layoutFromBranch.setLayoutParams(layoutParamsFromBranch);
                    layoutFromBranch.setOrientation(LinearLayout.HORIZONTAL);

                    LinearLayout.LayoutParams layoutParamsBranch2 = new LinearLayout.LayoutParams(350, LinearLayout.LayoutParams.WRAP_CONTENT);

                    LinearLayout.LayoutParams layoutParamsBranch3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParamsBranch3.setMargins(10,0,0,0);


                    lblFromSelectedBranch.setText("N/A");
                    lblFromSelectedBranch.setTextColor(Color.rgb(0,0,0));
                    lblFromSelectedBranch.setTextSize(15);
                    lblFromSelectedBranch.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    lblFromSelectedBranch.setLayoutParams(layoutParamsBranch2);
                    layoutFromBranch.addView(lblFromSelectedBranch);

                    TextView btnFromSelectBranch = new TextView(API_SelectedItems.this);
                    btnFromSelectBranch.setPadding(20,10,20,10);
                    btnFromSelectBranch.setText("...");
                    btnFromSelectBranch.setBackgroundResource( R.color.colorPrimary);
                    btnFromSelectBranch.setTextColor(Color.WHITE);
                    btnFromSelectBranch.setTextSize(13);
                    btnFromSelectBranch.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    btnFromSelectBranch.setLayoutParams(layoutParamsBranch3);

                    btnFromSelectBranch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showWarehouses(lblFromSelectedBranch);
                        }
                    });
                    layoutFromBranch.addView(btnFromSelectBranch);
                    layout.addView(layoutFromBranch);

                if(hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Transfer Item") || hiddenTitle.equals("API Item Request")){
                                        TextView lblToBranch = new TextView(getBaseContext());
                    lblToBranch.setText((hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Item Request")) ? "*To Warehouse:" : "*From Warehouse:");
                    lblToBranch.setTextColor(Color.rgb(0,0,0));
                    lblToBranch.setTextSize(15);
                    lblToBranch.setGravity(View.TEXT_ALIGNMENT_CENTER);

                    LinearLayout layoutToBranch = new LinearLayout(getBaseContext());
                    LinearLayout.LayoutParams layoutParamsToBranch = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParamsToBranch.setMargins(20,0,0,20);
                    layoutToBranch.setLayoutParams(layoutParamsFromBranch);
                    layoutToBranch.setOrientation(LinearLayout.HORIZONTAL);

                    lblToSelectedBranch.setText("N/A");
                    lblToSelectedBranch.setTextColor(Color.rgb(0,0,0));
                    lblToSelectedBranch.setTextSize(15);
                    lblToSelectedBranch.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    lblToSelectedBranch.setLayoutParams(layoutParamsBranch2);
                    layoutToBranch.addView(lblToSelectedBranch);


                    TextView btnToSelectBranch = new TextView(getBaseContext());
                    btnToSelectBranch.setText("...");
                    btnToSelectBranch.setPadding(20,10,20,10);
                    btnToSelectBranch.setBackgroundResource(R.color.colorPrimary);
                    btnToSelectBranch.setTextColor(Color.WHITE);
                    btnToSelectBranch.setTextSize(15);
                    btnToSelectBranch.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    btnToSelectBranch.setLayoutParams(layoutParamsBranch3);

                    btnToSelectBranch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showWarehouses(lblToSelectedBranch);
                        }
                    });
                    layoutToBranch.addView(btnToSelectBranch);
                    layout.addView(lblToBranch);
                    layout.addView(layoutToBranch);
                }



                if(hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Transfer Item") || hiddenTitle.equals("API Issue For Production") || hiddenTitle.equals("API Received from Production")){
//                    TextView lblSAPNumber = new TextView(getBaseContext());
//                    lblSAPNumber.setText("SAP #:");
//                    lblSAPNumber.setTextColor(Color.rgb(0,0,0));
//                    lblSAPNumber.setTextSize(15);
//                    lblSAPNumber.setGravity(View.TEXT_ALIGNMENT_CENTER);
//                    lblSAPNumber.setLayoutParams(layoutParamsBranch);
//                    layout.addView(lblSAPNumber);

                    textInputLayoutSAPNumber.setHint("SAP #");
                    textInputLayoutSAPNumber.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
                    textInputLayoutSAPNumber.setBoxBackgroundColor(Color.WHITE);
                    textInputLayoutSAPNumber.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                    textInputLayoutSAPNumber.setBoxStrokeColor(Color.parseColor("#1687a7"));

                    //Must use context of textInputLayout
                    TextInputEditText editTextSAPNumber = new TextInputEditText(textInputLayoutSAPNumber.getContext());
                    textInputLayoutSAPNumber.addView(editTextSAPNumber);
                    editTextSAPNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                    layout.addView(textInputLayoutSAPNumber);

//                    txtSAPNumber.setTextSize(15);
//                    txtSAPNumber.setGravity(View.TEXT_ALIGNMENT_CENTER);
//                    txtSAPNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
//                    txtSAPNumber.setLayoutParams(layoutParamsBranch);
//                    layout.addView(txtSAPNumber);
                }
            }

           if(hiddenTitle.equals("API Item Request")){
               Button btnPickDate = new Button(getBaseContext());
               LinearLayout.LayoutParams layoutParamsBtnDate = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
               layoutParamsBtnDate.setMargins(0,0,0,20);
               btnPickDate.setText("Pick Due Date");
               btnPickDate.setPadding(10,10,10,0);
               btnPickDate.setLayoutParams(layoutParamsBtnDate);
               btnPickDate.setBackgroundResource(R.color.colorPrimary);
               btnPickDate.setTextColor(Color.WHITE);
               btnPickDate.setGravity(View.TEXT_ALIGNMENT_CENTER);
               btnBack.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

               btnPickDate.setOnClickListener(new View.OnClickListener() {
                   @RequiresApi(api = Build.VERSION_CODES.N)
                   @Override
                   public void onClick(View view) {
                       showDatePickerDialog();
                   }
               });
               layout.addView(btnPickDate);

               lblSelectedDate = new TextView(getBaseContext());
               LinearLayout.LayoutParams layoutParamsLblSelectedDate = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
               layoutParamsLblSelectedDate.setMargins(0,0,0,20);
               lblSelectedDate.setLayoutParams(layoutParamsLblSelectedDate);
               lblSelectedDate.setTextColor(Color.rgb(0,0,0));
               lblSelectedDate.setText("N/A");
               lblSelectedDate.setTextSize(15);
               lblSelectedDate.setGravity(View.TEXT_ALIGNMENT_CENTER);
               layout.addView(lblSelectedDate);
           }
//            if(hiddenTitle.equals("API Received from Production") || hiddenTitle.equals("API Item Request For Transfer")){
//                TextView lblSAPNumber = new TextView(getBaseContext());
//                lblSAPNumber.setText("SAP #:");
//                lblSAPNumber.setTextColor(Color.rgb(0,0,0));
//                lblSAPNumber.setTextSize(15);
//                lblSAPNumber.setGravity(View.TEXT_ALIGNMENT_CENTER);
//                lblSAPNumber.setLayoutParams(layoutParamsBranch);
//                layout.addView(lblSAPNumber);
//
//                txtSAPNumber.setTextSize(15);
//                txtSAPNumber.setGravity(View.TEXT_ALIGNMENT_CENTER);
//                txtSAPNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
//                txtSAPNumber.setLayoutParams(layoutParamsBranch);
//                layout.addView(txtSAPNumber);
//            }

//            TextView lblRemarks = new TextView(getBaseContext());
//            lblRemarks.setTextColor(Color.rgb(0,0,0));
//            lblRemarks.setText("*Remarks:");
//            lblRemarks.setTextSize(15);
//            lblRemarks.setGravity(View.TEXT_ALIGNMENT_CENTER);
//            layout.addView(lblRemarks);
//
//            EditText txtRemarks = new EditText(API_SelectedItems.this);
//            txtRemarks.setTextColor(Color.rgb(0,0,0));
//            txtRemarks.setTextSize(15);
//            txtRemarks.setGravity(View.TEXT_ALIGNMENT_CENTER);
//            layout.addView(txtRemarks);

            TextInputLayout textInputLayoutRemarks = new TextInputLayout(API_SelectedItems.this);
            textInputLayoutRemarks.setHint("Remarks");
            textInputLayoutRemarks.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
            textInputLayoutRemarks.setBoxBackgroundColor(Color.WHITE);
            textInputLayoutRemarks.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
            textInputLayoutRemarks.setBoxStrokeColor(Color.parseColor("#1687a7"));

            //Must use context of textInputLayout
            TextInputEditText editTextRemarks = new TextInputEditText(textInputLayoutRemarks.getContext());
            textInputLayoutRemarks.addView(editTextRemarks);
//                    txt
            layout.addView(textInputLayoutRemarks);

            String supplier = "";
            Cursor cursor = myDb3.getAllData(hiddenTitle);
            if(cursor != null){
                if(cursor.moveToNext()){
                    supplier = cursor.getString(2);
                }
            }

            String finalSupplier = supplier;
            myDialog.setPositiveButton("Submit", (dialogInterface, i) -> {
                if(lblFromSelectedBranch.getText().toString().equals("N/A") && hiddenTitle.equals("API Transfer Item")){
                    Toast.makeText(getBaseContext(), "Please select to Warehouse", Toast.LENGTH_SHORT).show();
                }
                else if(lblFromSelectedBranch.getText().toString().equals("N/A") && hiddenTitle.equals("API Item Request")) {
                    Toast.makeText(getBaseContext(), "Please select to Warehouse", Toast.LENGTH_SHORT).show();
                }
                else if(lblFromSelectedBranch.getText().toString().equals("N/A") && hiddenTitle.equals("API Issue For Production")) {
                    Toast.makeText(getBaseContext(), "Please select from Warehouse", Toast.LENGTH_SHORT).show();
                }
                else if(lblFromSelectedBranch.getText().toString().equals("N/A") && hiddenTitle.equals("API Received from Production")) {
                    Toast.makeText(getBaseContext(), "Please select to Warehouse", Toast.LENGTH_SHORT).show();
                }
                else if(textInputLayoutRemarks.getEditText().getText().toString().isEmpty()){
                    Toast.makeText(getBaseContext(), "Remarks field is empty", Toast.LENGTH_SHORT).show();
                }else if(hiddenTitle.equals("API Item Request") && lblSelectedDate.getText().toString() == "N/A"){
                    Toast.makeText(getBaseContext(), "Please select Due Date", Toast.LENGTH_SHORT).show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                    builder.setMessage("Are you sure want to submit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String whseCode = findWarehouseCode(lblFromSelectedBranch.getText().toString());
                                    String whseToCode = findWarehouseCode(lblToSelectedBranch.getText().toString());

                                    if(hiddenTitle.equals("API Received Item")){
                                        int sapNumber = (textInputLayoutSAPNumber.getEditText().getText().toString().isEmpty() ? 0 : Integer.parseInt(textInputLayoutSAPNumber.getEditText().getText().toString()));
                                        btnProceed.setEnabled(false);
                                        SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
                                        String currentWhse = Objects.requireNonNull(sharedPreferences.getString("whse", ""));
                                        whseCode = whseCode.equals("N/A") || whseCode.isEmpty() ? currentWhse : whseCode;
                                        apiSaveManualReceived(whseCode,lblToSelectedBranch.getText().toString(), textInputLayoutRemarks.getEditText().getText().toString(), sapNumber,txtSupplier.getText().toString());
                                    }else if(hiddenTitle.equals("API Issue For Production") || hiddenTitle.equals("API Received from Production")){
                                        int sapNumber = (textInputLayoutSAPNumber.getEditText().getText().toString().isEmpty() ? 0 : Integer.parseInt(textInputLayoutSAPNumber.getEditText().getText().toString()));
                                        btnProceed.setEnabled(false);
                                        apiSaveIssueForProduction(whseCode, textInputLayoutRemarks.getEditText().getText().toString(), sapNumber);
                                    }
                                    else if(hiddenTitle.equals("API Transfer Item")){
                                        apiSaveTransferItem(whseCode, whseToCode, textInputLayoutRemarks.getEditText().getText().toString());
                                    }else if(hiddenTitle.equals("API Item Request")){
                                        apiItemRequest(textInputLayoutRemarks.getEditText().getText().toString(),whseCode,whseToCode);
                                    }else if(hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API Pull Out Count")){
                                        apiSaveInventoryCount(textInputLayoutRemarks.getEditText().getText().toString());
                                    }else if(hiddenTitle.equals("API Received from Production")){
                                        apiSaveReceivedProduction(textInputLayoutRemarks.getEditText().getText().toString(), textInputLayoutSAPNumber.getEditText().getText().toString().isEmpty() ? 0 : Integer.parseInt(textInputLayoutSAPNumber.getEditText().getText().toString()));
                                    }
                                    else if(hiddenTitle.equals("API Item Request For Transfer")){
                                        apiSaveItemRequestForTransfer(textInputLayoutSAPNumber.getEditText().getText().toString(), textInputLayoutRemarks.getEditText().getText().toString().trim());
                                    }
                                    else{
                                        apiSaveDataRec(finalSupplier, textInputLayoutRemarks.getEditText().getText().toString());
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

            myDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

            myDialog.setView(layout);
            myDialog.show();
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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
            navigationManager.showFragment(title);
            getSupportActionBar().setTitle(title);
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

    public void apiSaveReceivedProduction(String remarks, int sapNumber){
        Cursor cursor = myDb3.getAllData(hiddenTitle);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                JSONObject jsonObject = new JSONObject();
                try {
                    // create your json here
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());

                    JSONArray arrayDetails = new JSONArray();
                    String whseCode = "";
                    int baseID = 0;
                    Cursor cursor2 = myDb3.getAllData(hiddenTitle);
                    while (cursor2.moveToNext()) {
                        if(cursor2.getInt(6) == 1) {
                            JSONObject objectDetails = new JSONObject();
                            String itemName = cursor2.getString(3);
                            whseCode = cursor2.getString(8);
                            baseID = cursor2.getInt(9);
                            Double actualQty = cursor2.getDouble(5);
                            objectDetails.put("item_code", itemName);
                            objectDetails.put("quantity", actualQty);
                            objectDetails.put("whsecode", whseCode);
                            objectDetails.put("uom", cursor2.getString(11));
                            arrayDetails.put(objectDetails);
                        }
                    }
                    cursor2.close();
                    JSONObject objectHeaders = new JSONObject();
                    objectHeaders.put("prod_order_id", baseID);
                    objectHeaders.put("transdate", currentDateandTime);
                    objectHeaders.put("sap_number", (sapNumber <= 0 ? JSONObject.NULL : sapNumber));
                    objectHeaders.put("remarks", remarks);
                    objectHeaders.put("whsecode", whseCode);
                    jsonObject.put("header", objectHeaders);
                    jsonObject.put("rows", arrayDetails);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                System.out.println("HOY: " + jsonObject);
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPaddress = sharedPreferences2.getString("IPAddress", "");
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPaddress + "/api/production/rec_from_prod/new")
                        .method("POST", body)
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
                        API_SelectedItems.this.runOnUiThread(() -> {
                            try {
                                JSONObject jj = new JSONObject(finalResult);
                                boolean isSuccess = jj.getBoolean("success");
                                if (isSuccess) {
                                    myDb3.truncateTable();
                                    loadItems();
                                } else {
                                    String msg = jj.getString("message");
                                    if (msg.equals("Token is invalid")) {
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                        builder.setCancelable(false);
                                        builder.setMessage("Your session is expired. Please login again.");
                                        builder.setPositiveButton("OK", (dialog, which) -> {
                                            pc.loggedOut(API_SelectedItems.this);
                                            pc.removeToken(API_SelectedItems.this);
                                            startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
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
        }
    }

    public void showWarehouses(TextView lblSelectedBranch){
        AlertDialog _dialog = null;
        AlertDialog.Builder dialogSelectWarehouse = new AlertDialog.Builder(API_SelectedItems.this);
        dialogSelectWarehouse.setTitle("Select Warehouse");
        dialogSelectWarehouse.setCancelable(false);
        LinearLayout layout = new LinearLayout(getBaseContext());
        layout.setPadding(40, 40, 40, 40);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextInputLayout textInputLayout = new TextInputLayout(API_SelectedItems.this);
        textInputLayout.setHint("Search Branch");
        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        textInputLayout.setHelperText("eg. SILO-1, CWB-3, etc...");
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE);
        textInputLayout.setBoxStrokeColor(Color.RED);

////Must use context of textInputLayout
//        TextInputEditText editText = new TextInputEditText(textInputLayout.getContext());

        AutoCompleteTextView txtSearchBranch = new AutoCompleteTextView(getBaseContext());
        txtSearchBranch.setTextSize(13);
        textInputLayout.addView(txtSearchBranch);
        layout.addView(textInputLayout);

        final List<String>[] warehouses = new List[]{returnBranches()};
        final ArrayList<String>[] myReference = new ArrayList[]{getReference(warehouses[0], txtSearchBranch.getText().toString().trim())};
        final ArrayList<String>[] myID = new ArrayList[]{getID(warehouses[0], txtSearchBranch.getText().toString().trim())};
        final List<String>[] listItems = new List[]{getListItems(warehouses[0])};

        TextView btnSearchBranch = new TextView(getBaseContext());
        btnSearchBranch.setBackgroundResource(R.color.colorPrimary);
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

                API_SelectedItems.MyAdapter adapter = new API_SelectedItems.MyAdapter(API_SelectedItems.this, myReference[0], myID[0]);

                listView.setAdapter(adapter);
            }
        });

        layout.addView(btnSearchBranch);


        LinearLayout.LayoutParams layoutParamsWarehouses = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,300);
        layoutParamsWarehouses.setMargins(10,10,10,10);
        listView.setLayoutParams(layoutParamsWarehouses);

        txtSearchBranch.setAdapter(fillItems(listItems[0]));
        API_SelectedItems.MyAdapter adapter = new API_SelectedItems.MyAdapter(API_SelectedItems.this, myReference[0], myID[0]);
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
                        lblSelectedBranch.setText(textView1.getText().toString());
                        lblSelectedBranch.setTag(textView1.getText().toString());
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




    public ArrayAdapter<String> fillItems(List<String> items){
        return new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, items);
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
            textView2.setVisibility(View.GONE);

            return row;
        }
    }

    public void changePassword(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(API_SelectedItems.this);
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                    builder.setMessage("Are you sure want to submit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    API_SelectedItems.myChangePassword myChangePassword = new API_SelectedItems.myChangePassword(txtPassword.getText().toString().trim());
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
        LoadingDialog loadingDialog = new LoadingDialog(API_SelectedItems.this);
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                        builder.setMessage("We redirect you to Login Page")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pc.loggedOut(API_SelectedItems.this);
                                        pc.removeToken(API_SelectedItems.this);
                                        startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
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
        loadItems();
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
                .setPositiveButton("Yes", (dialog, which) -> {
                    pc.loggedOut(API_SelectedItems.this);
                    pc.removeToken(API_SelectedItems.this);
                    startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



    public void apiSaveTransferItem(String toBranch,String fromBranch, String remarks){
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        JSONObject jsonObject = new JSONObject();
        try {
            // create your json here
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());

            JSONObject objectHeaders = new JSONObject();
//            objectHeaders.put("transtype", "TRFR");
            objectHeaders.put("sap_number", null);
            objectHeaders.put("transdate", currentDateandTime);
            objectHeaders.put("remarks", remarks);
            objectHeaders.put("reference2", null);

            jsonObject.put("header", objectHeaders);

            JSONArray arrayDetails = new JSONArray();

            Cursor cursor = myDb4.getAllData(title);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject objectDetails = new JSONObject();
                    objectDetails.put("item_code", cursor.getString(1));
                    objectDetails.put("to_whse", toBranch);
                    objectDetails.put("from_whse", fromBranch);
                    objectDetails.put("quantity", cursor.getDouble(2));
                    objectDetails.put("uom", cursor.getString(5));
                    arrayDetails.put(objectDetails);
                }
                jsonObject.put("details", arrayDetails);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(jsonObject);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        String sURL = IPaddress + "/api/inv/trfr/new";
        String method = "POST";
        String bodyy = jsonObject.toString();
        String fromModule = title;
        String hiddenFromModule = hiddenTitle;

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(sURL)
                .method(method, body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        boolean isInserted = myDb7.insertData(sURL,method, bodyy, fromModule, hiddenFromModule,currentDate);
                        if(isInserted){
                            Toast.makeText(getBaseContext(),  "The data is inserted to local database", Toast.LENGTH_SHORT).show();
                            myDb4.truncateTable();
                            loadItems();
                        }else{
                            Toast.makeText(getBaseContext(),  "Your data is failed to insert in local database", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) {
                String result;
                try {
                    result = response.body().string();
                    if(result.substring(0,1).equals("{")){
                        JSONObject jj = new JSONObject(result);
                        boolean isSuccess = jj.getBoolean("success");
                        if (isSuccess) {
                            JSONObject jsonObjectData = jj.getJSONObject("data");
                            String reference = jsonObjectData.getString("reference");
                            String finalReference = reference;
                            API_SelectedItems.this.runOnUiThread(() -> {
                                myDb4.truncateTable();
                                Toast.makeText(getBaseContext(), "Transaction Completed", Toast.LENGTH_SHORT).show();
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setMessage("Reference #: " + finalReference);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loadItems();
                                    }
                                });
                                builder.show();
                            });
                        }else {
                            API_SelectedItems.this.runOnUiThread(() -> {
                                try {
                                    System.out.println(jj.getString("message"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    Toast.makeText(getBaseContext(), jj.getString("message"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }else {
                        Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void apiItemRequest(String remarks,String fromWarehouse,String toWhse){
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        JSONObject jsonObject = new JSONObject();
        try {
            // create your json here
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());


            JSONObject objectHeaders = new JSONObject();
            objectHeaders.put("transdate", currentDateandTime);
            objectHeaders.put("duedate", lblSelectedDate.getText().toString() + " 00:00");
            objectHeaders.put("remarks", remarks);
            objectHeaders.put("reference2", null);

            jsonObject.put("header", objectHeaders);

            JSONArray arrayDetails = new JSONArray();

            Cursor cursor = myDb4.getAllData(title);

//            SharedPreferences sharedPreferences2 = getSharedPreferences("LOGIN", MODE_PRIVATE);
//            String branch = Objects.requireNonNull(sharedPreferences2.getString("whse", ""));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject objectDetails = new JSONObject();
                    objectDetails.put("item_code", cursor.getString(1));
                    objectDetails.put("to_whse", toWhse);
                    objectDetails.put("quantity", cursor.getDouble(2));
                    objectDetails.put("uom", cursor.getString(5));
                    objectDetails.put("from_whse", fromWarehouse);
                    arrayDetails.put(objectDetails);
                }
                jsonObject.put("rows", arrayDetails);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        String sURL = IPaddress + "/api/inv/item_request/new";
        String method = "POST";
        String bodyy = jsonObject.toString();
        String fromModule = title;
        String hiddenFromModule = hiddenTitle;

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(sURL)
                .method(method, body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        boolean isInserted = myDb7.insertData(sURL,method, bodyy, fromModule, hiddenFromModule,currentDate);
                        if(isInserted){
                            Toast.makeText(getBaseContext(),  "The data is inserted to local database", Toast.LENGTH_SHORT).show();

                            myDb4.truncateTable();
                            loadItems();
                        }else{
                            Toast.makeText(getBaseContext(),  "Your data is failed to insert in local database", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) {
                String result;
                try {
                    result = response.body().string();
                    if(result.substring(0,1).equals("{")) {
                        JSONObject jj = new JSONObject(result);
                        boolean isSuccess = jj.getBoolean("success");
                        if (isSuccess) {
                            JSONObject jsonObjectData = jj.getJSONObject("data");
                            String reference = jsonObjectData.getString("reference");
                            API_SelectedItems.this.runOnUiThread(() -> {
                                myDb4.truncateTable();
                                Toast.makeText(getBaseContext(), "Transaction Completed", Toast.LENGTH_SHORT).show();
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setMessage("Reference #: " + reference);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                       loadItems();
                                    }
                                });
                                builder.show();
                            });
                        } else {
                            API_SelectedItems.this.runOnUiThread(() -> {
                                try {
                                    System.out.println(jj.getString("message"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    Toast.makeText(getBaseContext(), jj.getString("message"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }else{
                        Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public int returnDate(String value){
        int result = 0;
        SimpleDateFormat y = new SimpleDateFormat(value, Locale.getDefault());
        result = Integer.parseInt(y.format(new Date()));
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePickerDialog.OnDateSetListener) this, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        lblSelectedDate.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
    }


    public void apiSaveManualReceived(String fromBranch,String toBranch, String remarks,Integer sapNumber,String supplier){
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        JSONObject jsonObject = new JSONObject();
        try {
            // create your json here
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());

            JSONObject objectHeaders = new JSONObject();
            objectHeaders.put("transtype", "MNL");
            objectHeaders.put("transfer_id", null);
            objectHeaders.put("sap_number", (sapNumber <= 0 ? JSONObject.NULL: sapNumber));
            objectHeaders.put("transdate", currentDateandTime);
            objectHeaders.put("remarks", remarks);
            objectHeaders.put("reference2", null);
            objectHeaders.put("supplier", supplier.isEmpty() ? JSONObject.NULL : supplier);
            objectHeaders.put("type2", "N/A");

            jsonObject.put("header", objectHeaders);


            JSONArray arrayDetails = new JSONArray();

            Cursor cursor = myDb4.getAllData(title);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject objectDetails = new JSONObject();
                    objectDetails.put("item_code", cursor.getString(1));
                    objectDetails.put("from_whse", fromBranch);
                    objectDetails.put("to_whse", toBranch);
//                    objectDetails.put("to_whse", null);
                    objectDetails.put("quantity", cursor.getDouble(2));
                    objectDetails.put("actualrec", cursor.getDouble(2));
                    objectDetails.put("uom", cursor.getString(5));
                    arrayDetails.put(objectDetails);
                }
                jsonObject.put("details", arrayDetails);
            }
        } catch (JSONException e) {
            btnProceed.setEnabled(true);
            e.printStackTrace();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");


        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        System.out.println(jsonObject);
        String sURL = IPaddress + "/api/inv/recv/new";
        String method = "POST";
        String bodyy = jsonObject.toString();
        String fromModule = title;
        String hiddenFromModule = hiddenTitle;
//        System.out.println("body: " + jsonObject);
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(sURL)
                .method("POST", body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        boolean isInserted = myDb7.insertData(sURL,method, bodyy, fromModule, hiddenFromModule,currentDate);
                        btnProceed.setEnabled(true);
                        if(isInserted){
                            Toast.makeText(getBaseContext(),  "The data is inserted to local database", Toast.LENGTH_SHORT).show();

                            myDb4.truncateTable();
                            loadItems();
                        }else{
                            Toast.makeText(getBaseContext(),  "Your data is failed to insert in local database", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String answer = response.body().string();
                formatResponse(answer);
            }
        });
    }

    public void apiSaveIssueForProduction(String fromBranch, String remarks,Integer sapNumber){
        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
        String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
        JSONObject jsonObject = new JSONObject();
        try {
            // create your json here
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());

            JSONObject objectHeaders = new JSONObject();
            objectHeaders.put("sap_number", (sapNumber <= 0 ? JSONObject.NULL: sapNumber));
            objectHeaders.put("transdate", currentDateandTime);
            objectHeaders.put("whsecode", fromBranch);
            objectHeaders.put("remarks", remarks);

            jsonObject.put("header", objectHeaders);

            JSONArray arrayDetails = new JSONArray();

            Cursor cursor = myDb4.getAllData(title);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject objectDetails = new JSONObject();
                    objectDetails.put("item_code", cursor.getString(1));
                    objectDetails.put("whsecode", fromBranch);
                    objectDetails.put("quantity", cursor.getDouble(2));
                    objectDetails.put("uom", cursor.getString(5));
                    arrayDetails.put(objectDetails);
                }
                jsonObject.put("rows", arrayDetails);
            }
        } catch (JSONException e) {
            btnProceed.setEnabled(true);
            e.printStackTrace();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");


        SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPaddress = sharedPreferences2.getString("IPAddress", "");

        System.out.println(jsonObject);
        String appendURL = hiddenTitle.equals("API Issue For Production") ? "/api/production/issue_for_prod/new" : "/api/production/rec_from_prod/new";
        String sURL = IPaddress +  appendURL;
        String method = "POST";
        String bodyy = jsonObject.toString();
        String fromModule = title;
        String hiddenFromModule = hiddenTitle;
//        System.out.println("body: " + jsonObject);
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(sURL)
                .method("POST", body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        boolean isInserted = myDb7.insertData(sURL,method, bodyy, fromModule, hiddenFromModule,currentDate);
                        btnProceed.setEnabled(true);
                        if(isInserted){
                            Toast.makeText(getBaseContext(),  "The data is inserted to local database", Toast.LENGTH_SHORT).show();

                            myDb4.truncateTable();
                            loadItems();
                        }else{
                            Toast.makeText(getBaseContext(),  "Your data is failed to insert in local database", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String answer = response.body().string();
                formatResponse(answer);
            }
        });
    }

    public void formatResponse(String temp){
        if(!temp.isEmpty() && temp.substring(0,1).equals("{")){
            try{
                JSONObject jj = new JSONObject(temp);
                String msg = jj.getString("message");
//                System.out.println("JJ: " + jj);
                boolean isSuccess = jj.getBoolean("success");
                if (isSuccess) {
                    myDb4.truncateTable();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnProceed.setEnabled(true);
                            final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                            builder.setCancelable(false);
                            builder.setMessage(msg);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    loadItems();
                                }
                            });
                            builder.show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnProceed.setEnabled(true);
                            if(msg.equals("Token is invalid")){
                                final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                builder.setCancelable(false);
                                builder.setMessage("Your session is expired. Please login again.");
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    pc.loggedOut(API_SelectedItems.this);
                                    pc.removeToken(API_SelectedItems.this);
                                    startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.show();
                            }else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        btnProceed.setEnabled(true);
                                        Toast.makeText(getBaseContext(), "Error \n" +  msg, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
            }catch (Exception ex){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnProceed.setEnabled(true);
                        ex.printStackTrace();
                        Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }else{
            System.out.println(temp);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(getBaseContext(),temp, Toast.LENGTH_SHORT).show();
                    btnProceed.setEnabled(true);

                }
            });
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
            System.out.println("response: "+ response);
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
            System.out.println("RESPONSE: " + res);
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
            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    public List<String> returnBranches(){
        List<String> result = new ArrayList<>();
        result.add("Select Warehouse");
//        System.out.println(gBranch);
       try{
           JSONObject jsonObjectResponse = new JSONObject(gBranch);
           JSONArray jsonArray = jsonObjectResponse.getJSONArray("data");
           for(int i = 0; i < jsonArray.length(); i++){
               JSONObject jsonObject = jsonArray.getJSONObject(i);
//                            String branch = jsonObject.getString("whsecode") + "," + jsonObject.getString("whsename");
               String branch = jsonObject.getString("whsename");
               result.add(branch);

           }
       }catch (Exception ex){
           Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
       }
        return result;
    }


    @SuppressLint({"SetTextI18n", "RtlHardcoded"})
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void loadItems() {
        Cursor cursor;
        int count = (hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API System Transfer Item") ? myDb3.countItems(hiddenTitle) : myDb4.countItems(title));
//                int count = myDb4.countItems(title);

        if (hiddenTitle.equals("API Received from SAP")) {
            count = myDb3.countSAPItems(hiddenTitle);
        } else if (hiddenTitle.equals("API Inventory Count")) {
            count = myDb3.countItems(hiddenTitle);
        } else if (hiddenTitle.equals("API Pull Out Count")) {
            count = myDb3.countItems(hiddenTitle);
        } else if (hiddenTitle.equals("API System Transfer Item")) {
            count = myDb3.countItems(hiddenTitle);
        } else if (hiddenTitle.equals("API Received from Production")) {
            count = myDb4.countItems(title);
        } else if (hiddenTitle.equals("API Item Request For Transfer")) {
            count = myDb3.countItems(hiddenTitle);
        } else {
            count = myDb4.countItems(title);
        }

        LinearLayout layout = findViewById(R.id.layoutNoItems);

        if (count == 0) {
            layout.setVisibility(View.VISIBLE);
            Button btnGoto = findViewById(R.id.btnGoto);
            btnGoto.setOnClickListener(view -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                finish();
            });
            btnProceed.setVisibility(View.GONE);
        } else {
            layout.setVisibility(View.GONE);

            btnProceed.setVisibility(View.VISIBLE);

            TableRow tableColumn = new TableRow(API_SelectedItems.this);
            tableColumn.setBackgroundColor(Color.rgb(245, 245, 245));
            LinearLayout.LayoutParams layoutParamsTableColumn = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1.0f);
            tableColumn.setLayoutParams(layoutParamsTableColumn);
            final TableLayout tableLayout = findViewById(R.id.table_main);
            tableLayout.removeAllViews();
            System.out.println("TITLE: " + hiddenTitle);
            String[] columns = null;

            if(hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API System Transfer Item")){
                columns =  new String[]{"Item", "Del. Qty.", "Act. Qty.", "Var.", "Action"};
            }else if( hiddenTitle.equals("API Item Request For Transfer")){
                columns = new String[]{"Item", "Delivered Qty.", "Actual Qty."};
            }else{
                columns = new String[]{"Item", "Qty.", "Uom"};
            }

            for (String s : columns) {
                TextView lblColumn1 = new TextView(API_SelectedItems.this);
                lblColumn1.setGravity(View.TEXT_ALIGNMENT_CENTER);
                lblColumn1.setText(s);
                lblColumn1.setPadding(10, 0, 10, 0);
                tableColumn.addView(lblColumn1);
            }
            tableLayout.addView(tableColumn);
            cursor = (hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API Pull Out Count") || hiddenTitle.equals("API System Transfer Item") || hiddenTitle.equals("API Item Request For Transfer") ? myDb3.getAllData(hiddenTitle) : myDb4.getAllData(title));

//                    cursor = myDb4.getAllData(title);

            if (cursor != null) {
                while (cursor.moveToNext()) {

                    if (hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API Pull Out Count") || hiddenTitle.equals("API System Transfer Item") || hiddenTitle.equals("API Item Request For Transfer")) {
                        if (cursor.getInt(6) == 1) {
                            final TableRow tableRow = new TableRow(API_SelectedItems.this);
                            tableRow.setBackgroundColor(Color.WHITE);
                            String itemName = cursor.getString((hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API Pull Out Count") || hiddenTitle.equals("API System Transfer Item") || hiddenTitle.equals("API Received from Production") || hiddenTitle.equals("API Item Request For Transfer") ? 3 : 1));
                            String v = cutWord(itemName);
                            double quantity = 0.00;

                            if (hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API System Transfer Item") || hiddenTitle.equals("API Received from Production") || hiddenTitle.equals("API Item Request For Transfer")) {
                                quantity = cursor.getDouble(4);
                            } else if (hiddenTitle.equals("API Inventory Count") || hiddenTitle.equals("API Pull Out Count")) {
                                quantity = cursor.getDouble(5);
                            } else {
                                quantity = cursor.getDouble(2);
                            }

                            final int id = cursor.getInt(0);

                            LinearLayout linearLayoutItem = new LinearLayout(this);
                            linearLayoutItem.setPadding(10, 10, 10, 10);
                            linearLayoutItem.setOrientation(LinearLayout.VERTICAL);
                            linearLayoutItem.setBackgroundColor(Color.WHITE);
                            linearLayoutItem.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            tableRow.addView(linearLayoutItem);

                            LinearLayout.LayoutParams layoutParamsItem = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            TextView lblColumn1 = new TextView(this);
                            lblColumn1.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            lblColumn1.setLayoutParams(layoutParamsItem);
//                       String v = cutWord(item);
                            lblColumn1.setText(itemName);
                            lblColumn1.setTextSize(13);
                            lblColumn1.setBackgroundColor(Color.WHITE);

                            lblColumn1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getBaseContext(), itemName, Toast.LENGTH_SHORT).show();
                                }
                            });

                            linearLayoutItem.addView(lblColumn1);

                            TextView lblColumn2 = new TextView(API_SelectedItems.this);
                            lblColumn2.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            lblColumn2.setText(String.valueOf(quantity));
                            lblColumn2.setTextSize(13);
                            lblColumn2.setBackgroundColor(Color.WHITE);
                            lblColumn2.setPadding(10, 10, 10, 10);
                            tableRow.addView(lblColumn2);

                            if (hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API System Transfer Item") || hiddenTitle.equals("API Received from Production") || hiddenTitle.equals("API Item Request For Transfer")) {
                                TextView lblColumn4 = new TextView(API_SelectedItems.this);
                                lblColumn4.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                lblColumn4.setText(df.format(cursor.getDouble(5)));
                                lblColumn4.setBackgroundColor(Color.WHITE);
                                lblColumn4.setPadding(10, 10, 10, 10);
                                lblColumn4.setTextSize(13);
                                tableRow.addView(lblColumn4);

                                if(hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API System Transfer Item")){
                                    TextView lblColumn5 = new TextView(API_SelectedItems.this);
                                    lblColumn5.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                    double variance = cursor.getDouble(5) - quantity;
                                    lblColumn5.setText(df.format(variance));
                                    lblColumn5.setBackgroundColor(Color.WHITE);
                                    lblColumn5.setTextSize(13);
                                    lblColumn5.setPadding(10, 10, 10, 10);
                                    tableRow.addView(lblColumn5);
                                }
                            }

                            TextView lblColumn3 = new TextView(API_SelectedItems.this);
                            lblColumn3.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            lblColumn3.setTag(id);
                            lblColumn3.setBackgroundColor(Color.WHITE);
                            lblColumn3.setText("Remove");
                            lblColumn3.setTextSize(13);
                            lblColumn3.setPadding(10, 10, 10, 10);
                            lblColumn3.setTextColor(Color.RED);

                            lblColumn3.setOnClickListener(view -> {
                                boolean deletedItem;
                                deletedItem = (hiddenTitle.equals("API Received from SAP") || hiddenTitle.equals("API System Transfer Item") || hiddenTitle.equals("API Received from Production") ? myDb3.removeData(Integer.toString(id)) : myDb3.deleteData(Integer.toString(id)));
                                if (!deletedItem) {
                                    Toast.makeText(API_SelectedItems.this, "Item not remove", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(API_SelectedItems.this, "Item removed", Toast.LENGTH_SHORT).show();
                                    loadItems();
                                }


//                                if (myDb4.countItems(title).equals(0)) {
//                                    tableLayout.removeAllViews();
//                                    btnProceed.setVisibility(View.GONE);
//                                }
                            });

                            tableRow.addView(lblColumn3);

                            tableLayout.addView(tableRow);
                        }
                        View viewLine = new View(this);
                        LinearLayout.LayoutParams layoutParamsLine = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        viewLine.setLayoutParams(layoutParamsLine);
                        viewLine.setBackgroundColor(Color.GRAY);
                        tableLayout.addView(viewLine);
                    } else {
                        final TableRow tableRow = new TableRow(API_SelectedItems.this);
                        tableRow.setBackgroundColor(Color.WHITE);
                        String itemName = cursor.getString(1);
                        String v = cutWord(itemName);
                        double quantity = cursor.getDouble(2);
                        final int id = cursor.getInt(0);

                        LinearLayout linearLayoutItem = new LinearLayout(this);
                        linearLayoutItem.setPadding(10, 10, 10, 10);
                        linearLayoutItem.setOrientation(LinearLayout.VERTICAL);
                        linearLayoutItem.setBackgroundColor(Color.WHITE);
                        linearLayoutItem.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        tableRow.addView(linearLayoutItem);

                        LinearLayout.LayoutParams layoutParamsItem = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        TextView lblColumn1 = new TextView(this);
                        lblColumn1.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        lblColumn1.setLayoutParams(layoutParamsItem);
                        lblColumn1.setBackgroundColor(Color.WHITE);
                        lblColumn1.setText(itemName);
                        lblColumn1.setTextSize(15);
                        lblColumn1.setBackgroundColor(Color.WHITE);

                        lblColumn1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getBaseContext(), itemName, Toast.LENGTH_SHORT).show();
                            }
                        });

                        linearLayoutItem.addView(lblColumn1);


                        TextView lblColumn2 = new TextView(API_SelectedItems.this);
                        lblColumn2.setBackgroundColor(Color.WHITE);
                        lblColumn2.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        lblColumn2.setText(df.format(quantity));
                        lblColumn2.setTextSize(15);
                        lblColumn2.setPadding(10, 10, 10, 10);
                        tableRow.addView(lblColumn2);

                        if(hiddenTitle.equals("API Received Item") || hiddenTitle.equals("API Transfer Item") || hiddenTitle.equals("API Issue For Production") || hiddenTitle.equals("API Received from Production")){
                            TextView lblColumn4 = new TextView(API_SelectedItems.this);
                            lblColumn4.setGravity(View.TEXT_ALIGNMENT_CENTER);
                            lblColumn4.setTag(id);
                            lblColumn4.setBackgroundColor(Color.WHITE);
                            lblColumn4.setText(cursor.getString(5));
                            lblColumn4.setTextSize(13);
                            lblColumn4.setPadding(10, 10, 10, 10);
                            tableRow.addView(lblColumn4);
                        }

                        TextView lblColumn3 = new TextView(API_SelectedItems.this);
                        lblColumn3.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        lblColumn3.setBackgroundColor(Color.WHITE);
                        lblColumn3.setTag(id);
                        lblColumn3.setText("Remove");
                        lblColumn3.setTextSize(13);
                        lblColumn3.setPadding(10, 10, 10, 10);
                        lblColumn3.setTextColor(Color.RED);

                        lblColumn3.setOnClickListener(view -> {
                            int deletedItem;
                            deletedItem = myDb4.deleteData(Integer.toString(id));
                            if (deletedItem < 0) {
                                Toast.makeText(API_SelectedItems.this, "Item not remove", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(API_SelectedItems.this, "Item removed", Toast.LENGTH_SHORT).show();
//                                Intent intent;
//                                intent = new Intent(getBaseContext(), API_SelectedItems.class);
//                                intent.putExtra("title", title);
//                                intent.putExtra("hiddenTitle", hiddenTitle);
//                                startActivity(intent);
//                                finish();
                                loadItems();
                            }

                            if (myDb4.countItems(title).equals(0)) {
                                tableLayout.removeAllViews();
                                btnProceed.setVisibility(View.GONE);
                            }
                        });

                        tableRow.addView(lblColumn3);

                        tableLayout.addView(tableRow);

                        View viewLine = new View(this);
                        LinearLayout.LayoutParams layoutParamsLine = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        viewLine.setLayoutParams(layoutParamsLine);
                        viewLine.setBackgroundColor(Color.GRAY);
                        tableLayout.addView(viewLine);

                    }
                }
            }
        }
    }

    public void apiSaveItemRequestForTransfer(String sapNumber, String remarks) {
        Cursor cursor = myDb3.getAllData(hiddenTitle);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                String fromBranch = cursor.getString(2);

                SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                JSONObject jsonObject = new JSONObject();
                try {
                    // create your json here
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());

                    JSONObject objectHeaders = new JSONObject();

                    objectHeaders.put("sap_number", sapNumber.isEmpty() ? null : sapNumber);
                    objectHeaders.put("transdate", currentDateandTime);
                    objectHeaders.put("remarks", remarks);
                    objectHeaders.put("reference2", null);
                    objectHeaders.put("base_id", cursor.getInt(9));
                    objectHeaders.put("base_objtype", cursor.getInt(14));
                    jsonObject.put("header", objectHeaders);

                    JSONArray arrayDetails = new JSONArray();

                    Cursor cursor2 = myDb3.getAllData(hiddenTitle);
                    while (cursor2.moveToNext()) {
                        if(cursor2.getInt(6) == 1) {
                            JSONObject objectDetails = new JSONObject();
                            String itemName = cursor2.getString(3);
                            Double actualQty = cursor2.getDouble(5);

                            objectDetails.put("base_id", cursor2.getInt(13));
                            objectDetails.put("item_code", itemName);
                            objectDetails.put("from_whse", cursor2.getString(2));
                            objectDetails.put("to_whse", cursor2.getString(8));
                            objectDetails.put("quantity", actualQty);
                            objectDetails.put("uom", cursor2.getString(11));
                            arrayDetails.put(objectDetails);
                        }
                    }
                    jsonObject.put("details", arrayDetails);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPaddress = sharedPreferences2.getString("IPAddress", "");

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPaddress + "/api/inv/trfr/new")
                        .method("POST", body)
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
                                Toast.makeText(getBaseContext(), "e\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onResponse(Call call, okhttp3.Response response) {
                        String result = "";
                        try {
                            result = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("result: " + result);
                        String finalResult = result;
                        API_SelectedItems.this.runOnUiThread(() -> {
                            try {
                                JSONObject jj = new JSONObject(finalResult);
                                boolean isSuccess = jj.getBoolean("success");
                                if (isSuccess) {
                                    myDb3.truncateTable();
                                    JSONObject jsonObjectData = jj.getJSONObject("data");
                                    Toast.makeText(getBaseContext(), jj.getString("message"), Toast.LENGTH_SHORT).show();
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                    builder.setCancelable(false);
                                    builder.setMessage("Reference #: " + jsonObjectData.getString("reference"));
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            loadItems();
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.show();
                                } else {
                                    String msg = jj.getString("message");
                                    if (msg.equals("Token is invalid")) {
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                        builder.setCancelable(false);
                                        builder.setMessage("Your session is expired. Please login again.");
                                        builder.setPositiveButton("OK", (dialog, which) -> {
                                            pc.loggedOut(API_SelectedItems.this);
                                            pc.removeToken(API_SelectedItems.this);
                                            startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
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
        }
    }

    public void apiSaveDataRec(String supplier, String remarks) {
        Cursor cursor = myDb3.getAllData(hiddenTitle);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                String sap_number = cursor.getString(1);
                String fromBranch = cursor.getString(2);

                SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                JSONObject jsonObject = new JSONObject();
                try {
                    // create your json here
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());

                    JSONObject objectHeaders = new JSONObject();

                    String transType;
                    if(cursor.getInt(7) == 0 && hiddenTitle.equals("API Received from SAP")){
                        transType = "SAPPO";
                    }else if(cursor.getInt(7) == 1 && hiddenTitle.equals("API Received from SAP")){
                        transType = "SAPIT";
                    }else if(cursor.getInt(7) == 2 && hiddenTitle.equals("API Received from SAP")){
                        transType = "SAPDN";
                    }else if(cursor.getInt(7) == 3 && hiddenTitle.equals("API Received from SAP")){
                        transType = "SAPAR";
                    }
                    else {
                        transType = "TRFR";
                    }

                    objectHeaders.put("transtype", transType);
                  if(hiddenTitle.equals("API Received from SAP")){
                      objectHeaders.put("transfer_id", null);
                  }else {
                      objectHeaders.put("base_id", (cursor.getInt(9) <= 0 ? null : cursor.getInt(9)));
                  }
                    objectHeaders.put("sap_number", (hiddenTitle.equals("API Received from SAP") ? (sap_number.isEmpty() ? null : sap_number) : null));
                    objectHeaders.put("transdate", currentDateandTime);
                    objectHeaders.put("remarks", remarks);
                    objectHeaders.put((hiddenTitle.equals("API Received from SAP") ? "reference2" : "ref2"), null);
                    objectHeaders.put("supplier", (cursor.getInt(7) == 0) ? supplier : null);
                    jsonObject.put("header", objectHeaders);

                    JSONArray arrayDetails = new JSONArray();

                    Cursor cursor2 = myDb3.getAllData(hiddenTitle);
                    while (cursor2.moveToNext()) {
                        if(cursor2.getInt(6) == 1) {
                            JSONObject objectDetails = new JSONObject();
                            String itemName = cursor2.getString(3);
                            Double deliveredQty = cursor2.getDouble(4);
                            Double actualQty = cursor2.getDouble(5);

                            objectDetails.put("item_code", itemName);
                            objectDetails.put("from_whse", fromBranch);
                            objectDetails.put("to_whse", cursor2.getString(8));
                            objectDetails.put("quantity", deliveredQty);
                            objectDetails.put("actualrec", actualQty);
                            objectDetails.put("uom", cursor2.getString(11));
                            arrayDetails.put(objectDetails);
                        }
                    }
                    jsonObject.put("details", arrayDetails);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println("SAPAR DAPAT: " + jsonObject);
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                System.out.println("DITOOOO? "+ jsonObject);
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPaddress = sharedPreferences2.getString("IPAddress", "");

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPaddress + "/api/inv/recv/new")
                        .method("POST", body)
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
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String finalResult = result;
                        API_SelectedItems.this.runOnUiThread(() -> {
                            try {
                                JSONObject jj = new JSONObject(finalResult);
                                boolean isSuccess = jj.getBoolean("success");
                                if (isSuccess) {
                                    myDb3.truncateTable();
                                    JSONObject jsonObjectData = jj.getJSONObject("data");
                                    Toast.makeText(getBaseContext(), jj.getString("message"), Toast.LENGTH_SHORT).show();
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                    builder.setCancelable(false);
                                    builder.setMessage("Reference #: " + jsonObjectData.getString("reference"));
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            loadItems();
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.show();
                                } else {
                                    String msg = jj.getString("message");
                                    if (msg.equals("Token is invalid")) {
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                        builder.setCancelable(false);
                                        builder.setMessage("Your session is expired. Please login again.");
                                        builder.setPositiveButton("OK", (dialog, which) -> {
                                            pc.loggedOut(API_SelectedItems.this);
                                            pc.removeToken(API_SelectedItems.this);
                                            startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
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
        }
    }

    public void apiSaveInventoryCount(String remarks) {
        Cursor cursor = myDb3.getAllData(hiddenTitle);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                String token = Objects.requireNonNull(sharedPreferences.getString("token", ""));
                JSONObject jsonObject = new JSONObject();
                try {
                    // create your json here
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());

                    JSONObject objectHeaders = new JSONObject();

                    objectHeaders.put("transdate", currentDateandTime);
                    objectHeaders.put("remarks", remarks);
                    jsonObject.put("header", objectHeaders);

                    JSONArray arrayDetails = new JSONArray();

                    Cursor cursor2 = myDb3.getAllData(hiddenTitle);
                    while (cursor2.moveToNext()) {
                        if(cursor2.getInt(6) == 1) {
                            JSONObject objectDetails = new JSONObject();
                            String itemName = cursor2.getString(3);
                            Double deliveredQty = cursor2.getDouble(4);
                            Double actualQty = cursor2.getDouble(5);

                            objectDetails.put("item_code", itemName);

                            if(hiddenTitle.equals("API Inventory Count")){
                                objectDetails.put("quantity", deliveredQty);
                            }

                            objectDetails.put((hiddenTitle.equals("API Inventory Count") ? "actual_count" : "quantity"), actualQty);
                            objectDetails.put("uom", cursor2.getString(11));
                            arrayDetails.put(objectDetails);
                        }
                    }
                    jsonObject.put("rows", arrayDetails);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                SharedPreferences sharedPreferences2 = getSharedPreferences("CONFIG", MODE_PRIVATE);
                String IPaddress = sharedPreferences2.getString("IPAddress", "");

                String isInvCount = (hiddenTitle.equals("API Inventory Count") ? "inv/count" : "pulloutreq");
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(IPaddress + "/api/" + isInvCount + "/create")
                        .method("POST", body)
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
                        API_SelectedItems.this.runOnUiThread(() -> {
                            try {
                                JSONObject jj = new JSONObject(finalResult);
                                boolean isSuccess = jj.getBoolean("success");
                                if (isSuccess) {
                                    myDb3.truncateTable();
                                    Toast.makeText(getBaseContext(), jj.getString("message"), Toast.LENGTH_SHORT).show();
                                    loadItems();
                                } else {
                                    String msg = jj.getString("message");
                                    if (msg.equals("Token is invalid")) {
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(API_SelectedItems.this);
                                        builder.setCancelable(false);
                                        builder.setMessage("Your session is expired. Please login again.");
                                        builder.setPositiveButton("OK", (dialog, which) -> {
                                            pc.loggedOut(API_SelectedItems.this);
                                            pc.removeToken(API_SelectedItems.this);
                                            startActivity(uic.goTo(API_SelectedItems.this, MainActivity.class));
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
        }
    }

    public String cutWord(String value){
        String result;
        int limit = 10;
        int limitTo = limit - 3;
        result = (value.length() > limit ? value.substring(0, limitTo) + "..." : value);
        return result;
    }
}