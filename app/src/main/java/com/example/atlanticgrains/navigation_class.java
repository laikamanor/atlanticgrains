package com.example.atlanticgrains;

import java.util.Arrays;
import java.util.List;

public class navigation_class {
    public List<String> getTitles(String appName){
        return Arrays.asList("Settings","Production","Inventory", "Logs","Count");
    }

    public List<String> getItem(String parentItem){
        if(parentItem.equals("Settings")){
            return Arrays.asList("Logout","Cut Off", "Change Password", "Offline Pending Transactions");
        }else if(parentItem.equals("Inventory")){
            return Arrays.asList("Manual Received Item", "Manual Transfer Item","Item Request","Item Request For Transfer");
        }else if(parentItem.equals("Production")){
//            return Arrays.asList("Item Request","Issue For Production", "Confirm Issue For Production", "Received from Production");
            return Arrays.asList("Issue For Production","Received from Production");
        }else if(parentItem.equals("Logs")){
            return Arrays.asList("Logs");
        }else if(parentItem.equals("Count")){
            return Arrays.asList("Inventory Count", "Inventory Count Confirmation");
        }
        else {
            return null;
        }
    }
}
