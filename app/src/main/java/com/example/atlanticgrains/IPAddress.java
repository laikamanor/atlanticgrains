package com.example.atlanticgrains;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class IPAddress extends AppCompatActivity {
    Button btnSubmit;
    TextInputLayout txtIpAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i_p_address);

        Objects.requireNonNull(getSupportActionBar()).hide();

        btnSubmit = findViewById(R.id.btnSubmit);
        txtIpAddress = findViewById(R.id.ip_address);

        SharedPreferences sharedPreferences = getSharedPreferences("CONFIG", MODE_PRIVATE);
        String IPAddress = sharedPreferences.getString("IPAddress", "");
        assert IPAddress != null;
        if (!IPAddress.isEmpty()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtIpAddress.getEditText().getText().toString().trim().isEmpty()) {
                    Toast.makeText(getBaseContext(), "IP Address field is required", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(IPAddress.this);
                    alertDialog.setTitle("Message");
                    alertDialog.setMessage("Are you sure you want to submit?");
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences sharedPreferences = getSharedPreferences("CONFIG", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("IPAddress", txtIpAddress.getEditText().getText().toString().trim()).apply();

                            Intent intent = new Intent(IPAddress.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    alertDialog.show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}