package cn.antraces.ycdms.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class MainPagerAdapter extends FragmentPagerAdapter {
    public List<Fragment> mViewList;

    public MainPagerAdapter(FragmentManager fragmentManager, List<Fragment> views) {
        super(fragmentManager);
        mViewList = views;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mViewList.get(position);
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

}
