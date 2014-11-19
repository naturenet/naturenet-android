package org.naturenet.adapters;
import org.naturenet.fragments.AboutFragment;
import org.naturenet.fragments.ActivitiesFragment;
import org.naturenet.fragments.HomeFragment;
import org.naturenet.fragments.HomeFragmentListener;
import org.naturenet.fragments.ObservationFragment;
import org.naturenet.fragments.TourFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
 
public class TabsPagerAdapter extends FragmentPagerAdapter {
	private final FragmentManager mFragmentManager;
    private Fragment mFragmentAtPos0;
	public HomePageListener listener = new HomePageListener();
    
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragmentManager = fm;
    }
    
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
//        	return new HomeFragment();
        	if (mFragmentAtPos0 == null) {
        		mFragmentAtPos0 =  HomeFragment.newInstance(listener);
        	}
        	return mFragmentAtPos0;
        case 1:
            // Top Rated fragment activity
//        	return new ActivitiesFragment();
            return ActivitiesFragment.newInstance();
        case 2:
            // Games fragment activity
//        	return new TourFragment();
            return TourFragment.newInstance();
        case 3:
            // Movies fragment activity
//        	return new AboutFragment();
            return AboutFragment.newInstance();
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 4;
    }
    
    @Override
    public int getItemPosition(Object object) {
        if (object instanceof HomeFragment && mFragmentAtPos0 instanceof ObservationFragment) {
        	Log.d("debug", "it's homefragment");
            return POSITION_NONE;
        } else if (object instanceof ObservationFragment && mFragmentAtPos0 instanceof HomeFragment) {
        	Log.d("debug", "it's homefragment");
            return POSITION_NONE;
        }
        else {
        	Log.d("debug", "it's not homefragment");
        	return POSITION_UNCHANGED;
        }
    }
    
    private final class HomePageListener implements HomeFragmentListener {
        public void onSwitchToNextFragment() {
            mFragmentManager.beginTransaction().remove(mFragmentAtPos0)
            .commit();
            if (mFragmentAtPos0 instanceof HomeFragment){
                mFragmentAtPos0 = ObservationFragment.newInstance(listener);
            } else { // Instance of NextFragment
                mFragmentAtPos0 = HomeFragment.newInstance(listener);
            }
            notifyDataSetChanged();
        }
    }
    
  

 
}

