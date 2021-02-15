package com.example.atlanticgrains.Helper;

import androidx.fragment.app.FragmentManager;

import com.example.atlanticgrains.CutOff;
import com.example.atlanticgrains.Interface.NavigationManager;

public class FragmentNavigationManager_CutOff implements NavigationManager {
    private static FragmentNavigationManager_CutOff mInstance;

    private FragmentManager mFragmentManager;
    public static FragmentNavigationManager_CutOff getmInstance(CutOff activity){
        if(mInstance == null)
            mInstance = new FragmentNavigationManager_CutOff();
        mInstance.configure(activity);
        return  mInstance;
    }

    private void configure(CutOff activity) {
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
