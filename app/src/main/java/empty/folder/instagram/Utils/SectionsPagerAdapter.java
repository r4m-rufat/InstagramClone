package empty.folder.instagram.Utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import java.util.ArrayList;
import java.util.List;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> listFragment = new ArrayList();

    public SectionsPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    public Fragment getItem(int position) {
        return (Fragment)this.listFragment.get(position);
    }

    public int getCount() {
        return this.listFragment.size();
    }

    public void addFragment(Fragment fragment) {
        this.listFragment.add(fragment);
    }
}
