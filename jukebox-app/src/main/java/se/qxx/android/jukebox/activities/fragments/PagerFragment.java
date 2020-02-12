package se.qxx.android.jukebox.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.gms.cast.framework.CastContext;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.viewmode.JukeboxFragmentAdapter;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.Logger;

public class PagerFragment extends Fragment {
    ViewPager pager;
    private JukeboxSettings settings;
    private JukeboxFragmentAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUserVisibleHint(false);

        settings = new JukeboxSettings(this.getContext());

        Logger.Log().d("Initializing - loading data");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.jukebox_main_wrapper, container, false);
        initializeView(v);
        return v;
    }

    private void initializeView(View v) {
        pager = getActivity().findViewById(R.id.rootJukeboxViewPager);
        adapter = new JukeboxFragmentAdapter(this.getActivity().getSupportFragmentManager(), this.getActivity());
        pager.setAdapter(adapter);
    }

    public static PagerFragment newInstance() {
        PagerFragment pagerFragment = new PagerFragment();
        return pagerFragment;
    }

}
