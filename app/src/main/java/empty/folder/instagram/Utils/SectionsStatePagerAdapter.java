package empty.folder.instagram.Utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> fragmentList = new ArrayList();
    private final HashMap<Fragment, Integer> myFragments = new HashMap();
    private final HashMap<String, Integer> myFragmentNumbers = new HashMap();
    private final HashMap<Integer, String> myFragmentNames = new HashMap();

    public SectionsStatePagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    public int getCount() {
        return fragmentList.size();
    }

    public void addSectionsFragment(Fragment fragment, String fragment_name) {
        this.fragmentList.add(fragment);
        this.myFragments.put(fragment, this.fragmentList.size() - 1);
        this.myFragmentNumbers.put(fragment_name, this.fragmentList.size() - 1);
        this.myFragmentNames.put(this.fragmentList.size() - 1, fragment_name);
    }

    /**
     * returns the fragment with the name @param
     * @param fragmentName
     * @return
     */
    public Integer getFragmentNumber(String fragmentName){
        if(myFragmentNumbers.containsKey(fragmentName)){
            return myFragmentNumbers.get(fragmentName);
        }else{
            return null;
        }
    }


    /**
     * returns the fragment with the name @param
     * @param fragment
     * @return
     */
    public Integer getFragmentNumber(Fragment fragment){
        if(myFragmentNumbers.containsKey(fragment)){
            return myFragmentNumbers.get(fragment);
        }else{
            return null;
        }
    }

    /**
     * returns the fragment with the name @param
     * @param fragmentNumber
     * @return
     */
    public String getFragmentName(Integer fragmentNumber){
        if(myFragmentNames.containsKey(fragmentNumber)){
            return myFragmentNames.get(fragmentNumber);
        }else{
            return null;
        }
    }
}

