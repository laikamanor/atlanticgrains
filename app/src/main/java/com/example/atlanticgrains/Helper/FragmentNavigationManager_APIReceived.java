package com.example.atlanticgrains.Helper;

import androidx.fragment.app.FragmentManager;

import com.example.atlanticgrains.APIReceived;
import com.example.atlanticgrains.Interface.NavigationManager;

public class FragmentNavigationManager_APIReceived implements NavigationManager {
    private static FragmentNavigationManager_APIReceived mInstance;

    private FragmentManager mFragmentManager;
    public static FragmentNavigationManager_APIReceived getmInstance(APIReceived activity){
        if(mInstance == null)
            mInstance = new FragmentNavigationManager_APIReceived();
        mInstance.configure(activity);
        return  mInstance;
    }

    private void configure(APIReceived activity) {
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
