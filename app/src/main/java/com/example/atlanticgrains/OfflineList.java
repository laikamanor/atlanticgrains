package com.example.atlanticgrains;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.example.atlanticgrains.Helper.FragmentNavigationManager_OfflineList;
import com.example.atlanticgrains.Interface.NavigationManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OfflineList extends AppCompatActivity {
    prefs_class pc = new prefs_class();
    ui_class uic = new ui_class();
    DatabaseHelper myDb;
    DatabaseHelper7 myDb7;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listTitle;
    private Map<String, List<String>> listChild;
    private NavigationManager navigationManager;
    navigation_class navc = new navigation_class();

    private OkHttpClient client;
    ListView listView;
    String title,hidden_title;
    Spinner cmbType;
    ProgressBar progressBar;
    Button btnUpload;
    long mLastClickTime;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_list);

        myDb7 = new DatabaseHelper7(this);

        cmbType = findViewById(R.id.cmbType);
        progressBar = findViewById(R.id.progWait);
        progressBar.setVisibility(View.GONE);
        myDb = new DatabaseHelper(this);
        btnUpload = findViewById(R.id.btnUpload);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        expandableListView = (ExpandableListView)findViewById(R.id.navList);
        navigationManager = FragmentNavigationManager_OfflineList.getmInstance(this);

        SharedPreferences sharedPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
        String fullName = Objects.requireNonNull(sharedPreferences.getString("fullname", ""));
        title = getIntent().getStringExtra("title");
        hidden_title = getIntent().getStringExtra("hiddenTitle");

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


        listView = findViewById(R.id.listView);

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OfflineList.this);
                builder.setMessage("Are you sure want to upload?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialog, which) -> {
                            UploadData uploadData = new UploadData();
                            uploadData.execute();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.cancel());
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
        AlertDialog.Builder myDialog = new AlertDialog.Builder(OfflineList.this);
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(OfflineList.this);
                    builder.setMessage("Are you sure want to submit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    OfflineList.myChangePassword myChangePassword = new OfflineList.myChangePassword(txtPassword.getText().toString().trim());
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
        LoadingDialog loadingDialog = new LoadingDialog(OfflineList.this);
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(OfflineList.this);
                        builder.setMessage("We redirect you to Login Page")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pc.loggedOut(OfflineList.this);
                                        pc.removeToken(OfflineList.this);
                                        startActivity(uic.goTo(OfflineList.this, MainActivity.class));
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

    private class UploadData extends AsyncTask<String, Void, String> {
        int int_success = 0;
        @Override
        protected void onPreExecute() {
            btnUpload.setEnabled(false);
            listView.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            String appendJsons = "";
            try{
                Cursor cursor = myDb7.getAllDataByModule(cmbType.getSelectedItem().toString(), "from_module");
                if(cursor != null){
                    while (cursor.moveToNext()) {
                        client = new OkHttpClient();
                        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                        SharedPreferences sharedPreferences0 = getSharedPreferences("TOKEN", MODE_PRIVATE);
                        String token = sharedPreferences0.getString("token", "");

//                        System.out.println("BODY: " + cursor.getString(3) + "\n" + "URL: " + cursor.getString(1) + "\n" + "METHOD" + cursor.getString(2));

                        RequestBody body = RequestBody.create(JSON, cursor.getString(3));
                        okhttp3.Request request = new okhttp3.Request.Builder()
                                .url(cursor.getString(1))
                                .method(cursor.getString(2), body)
                                .addHeader("Authorization", "Bearer " + token)
                                .build();
                        Response response = null;

                        String menuItemsFormat = "ORDER #" + cursor.getInt(0),
                                receivedItemFormat = "RECV #" + cursor.getString(0),
                                transferItemFormat = "TRFR #" + cursor.getString(0),
                                itemRequestFormat = "ITEMR #" + cursor.getString(0),
                                resultFormat = cmbType.getSelectedItem().toString().equals("Sales") ? menuItemsFormat : cmbType.getSelectedItem().toString().equals("Received Item") ? receivedItemFormat : cmbType.getSelectedItem().toString().equals("Transfer Item") ? transferItemFormat : itemRequestFormat;
                        try {
                            response = client.newCall(request).execute();
                            String s = response.body().string();
                            if(response.isSuccessful()){
                                JSONObject jsonObjectResponse = new JSONObject(s);
                                boolean apiSuccess = jsonObjectResponse.getBoolean("success");

                                if (apiSuccess) {
                                    int_success += 1;
                                    appendJsons += resultFormat + " uploaded \n";
                                    myDb7.deleteData(cursor.getString(0));
                                }
                            }else {
                                if(s.substring(0,1).equals("{")){
                                    JSONObject jsonObject = new JSONObject(s);
                                    appendJsons += resultFormat + ": " + jsonObject.getString("message") + "\n";
                                }else{
                                    appendJsons += resultFormat + ": " +  s + "\n";
                                }
                            }
                        } catch (Exception ex) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnUpload.setEnabled(true);
                                    listView.setEnabled(true);
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                            ex.printStackTrace();
                            return ex.getMessage();
                        }
                    }
                    return appendJsons;
                }
                cursor.close();
            }catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnUpload.setEnabled(true);
                        listView.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });
                ex.printStackTrace();
                return ex.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (s != null) {
                    btnUpload.setEnabled(true);
                    listView.setEnabled(true);
                    progressBar.setVisibility(View.GONE);

                    if (s.contains("Token is invalid")) {
                        final AlertDialog.Builder builder2 = new AlertDialog.Builder(OfflineList.this);
                        builder2.setCancelable(false);
                        builder2.setTitle("Message");
                        builder2.setMessage("Your token is Invalid. We redirect you to Login Page.");

                        builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pc.loggedOut(OfflineList.this);
                                pc.removeToken(OfflineList.this);
                                startActivity(uic.goTo(OfflineList.this, MainActivity.class));
                                finish();
                                dialog.dismiss();
                            }
                        });
                        builder2.show();
                    } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(OfflineList.this);
                        builder.setCancelable(false);
                        builder.setTitle("Message");
                        builder.setMessage(s);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (int_success > 0) {
                                    Intent intent = new Intent(getBaseContext(), OfflineList.class);
                                    intent.putExtra("title", title);
                                    intent.putExtra("hiddenTitle", hidden_title);
                                    startActivity(intent);
                                }
                                dialog.dismiss();
                            }
                        });


                        builder.show();
                    }
                }
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnUpload.setEnabled(true);
                        listView.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        ex.printStackTrace();
                        Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public void showMessage(String title, String message){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<String> types = new ArrayList<>();
        Cursor cursor = myDb7.getAllData();
        if(cursor != null){
            while (cursor.moveToNext()){
                if(!types.contains(cursor.getString(4))){
                    types.add(cursor.getString(4));
                }
            }
        }
        cursor.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbType.setAdapter(adapter);
        cmbType.setSelection(0);
        cmbType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor1 = myDb7.getAllDataByModule(cmbType.getSelectedItem().toString(), "from_module");
                if(cursor1 != null) {
                    ArrayList<String> myReference = new ArrayList<String>();
                    ArrayList<String> myID = new ArrayList<String>();
                    while (cursor1.moveToNext()) {
                        try {
                            String selectedModule = cmbType.getSelectedItem().toString();
                            String menuItemsFormat = "ORDER #" + cursor1.getInt(0),
                                    receivedItemFormat = "RECV #" + cursor1.getString(0),
                                    transferItemFormat = "TRFR #" + cursor1.getString(0),
                                    itemRequestFormat = "ITEMR #" + cursor1.getString(0),
                                    resultFormat = selectedModule.equals("Sales") ? menuItemsFormat : selectedModule.equals("Received Item") ? receivedItemFormat : selectedModule.equals("Transfer Item") ? transferItemFormat : itemRequestFormat;
                            myReference.add(resultFormat);
                            myID.add(cursor1.getString(0));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    OfflineList.MyAdapter adapter = new OfflineList.MyAdapter(getBaseContext(), myReference, myID);
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
                                            intent = new Intent(getBaseContext(), OfflineItems.class);
                                            intent.putExtra("id", textView.getText().toString());
                                            intent.putExtra("reference", textView1.getText().toString());
                                            intent.putExtra("type", cmbType.getSelectedItem().toString());
                                            intent.putExtra("title", title);
                                            intent.putExtra("hiddenTitle", hidden_title);
                                            intent.putExtra("type", cmbType.getSelectedItem().toString());
                                            startActivity(intent);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
                cursor1.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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

    public  void onBtnLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pc.loggedOut(OfflineList.this);
                        pc.removeToken(OfflineList.this);
                        startActivity(uic.goTo(OfflineList.this, MainActivity.class));
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
