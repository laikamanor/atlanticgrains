package com.example.atlanticgrains.Helper;

import androidx.fragment.app.FragmentManager;

import com.example.atlanticgrains.API_InventoryConfirmation;
import com.example.atlanticgrains.Interface.NavigationManager;

public class FragmentNavigationManager_InventoryConfirmation implements NavigationManager {
    private static FragmentNavigationManager_InventoryConfirmation mInstance;

    private FragmentManager mFragmentManager;
    public static FragmentNavigationManager_InventoryConfirmation getmInstance(API_InventoryConfirmation activity){
        if(mInstance == null)
            mInstance = new FragmentNavigationManager_InventoryConfirmation();
        mInstance.configure(activity);
        return  mInstance;
    }

    private void configure(API_InventoryConfirmation activity) {
        activity = activity;
        mFragmentManager = activity.getSupportFragmentManager();

    }

    @Override
    public void showFragment(String title) {

    }

//    @Override
//    public void showFragment(String title) {
//        showFragment(FragmentContent.newInstance(title),false);
//    }
//
//    public void showFragment(Fragment fragment, boolean b){
//        FragmentManager fragmentManager = mFragmentManager;
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.container,fragment);
//        fragmentTransaction.addToBackStack(null);
//        if(b || !BuildConfig.DEBUG)
//            fragmentTransaction.commitAllowingStateLoss();
//        else
//            fragmentTransaction.commit();
//        fragmentManager.executePendingTransactions();
//    }
}
