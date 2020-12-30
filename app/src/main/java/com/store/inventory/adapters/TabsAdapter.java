package com.store.inventory.adapters;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.store.inventory.fragments.PlaceholderFragment;
import com.store.inventory.models.Product;

import java.util.ArrayList;
import java.util.List;

public class TabsAdapter extends FragmentPagerAdapter {

    private final List<Fragment> FragmentList = new ArrayList<>();
    private final List<String> FragmentTitleList = new ArrayList<>();
    private final List<List<Product>> FragmentProductList = new ArrayList<>();

    public void addFragment(Fragment fragment, String title){
        FragmentList.add(fragment);
        FragmentTitleList.add(title);
    }

//    public void addFragment(Fragment fragment, String title , List<Product> products){
//        FragmentList.add(fragment);
//        FragmentTitleList.add(title);
//        FragmentProductList.add(products);
//    }


    public TabsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return FragmentTitleList.get(position);
    }

//    @Override
//    public Fragment getItem(int position) {
//        return FragmentList.get(position);
//    }

    @Override
    public Fragment getItem(int position) {
        return PlaceholderFragment.newInstance(FragmentTitleList.get(position),position);
    }


//    @NonNull
//    @Override
//    public Fragment getItem(int position) {
//        return PlaceholderFragment.newInstance(FragmentProductList.get(position),position);
//    }

    @Override
    public int getCount() {
        return FragmentList.size();
    }


}
