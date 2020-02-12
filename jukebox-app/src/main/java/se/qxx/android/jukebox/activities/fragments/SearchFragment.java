package se.qxx.android.jukebox.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.android.jukebox.adapters.list.MosLayoutAdapter;
import se.qxx.android.jukebox.adapters.viewmode.JukeboxFragment;
import se.qxx.android.jukebox.model.Constants;
import se.qxx.jukebox.domain.JukeboxDomain;

import java.util.List;

public class SearchFragment extends JukeboxFragment {

    private int totalMovies = 0;
    private int totalSeries = 0;

    @Override
    public int getTotalItems() {
        return this.getTotalMovies() + this.getTotalSeries();
    }

    public int getTotalMovies() {
        return totalMovies;
    }

    public void setTotalMovies(int totalMovies) {
        this.totalMovies = totalMovies;
    }

    public int getTotalSeries() {
        return totalSeries;
    }

    public void setTotalSeries(int totalSeries) {
        this.totalSeries = totalSeries;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.jukebox_main_wrapper, container, false);
        initializeView(v);
        return v;
    }

    private void initializeView(View v) {
    }

    public static SearchFragment newInstance() {
        Bundle b = new Bundle();
        b.putSerializable("mode", ViewMode.Search);
        SearchFragment f = new SearchFragment();
        f.setArguments(b);

        return f;
    }

    public void search(String query) {

    }

    @Override
    protected void loadMoreData(int offset) {
        setLoading(true);
        if (this.getHandler() != null) {
            // query on both movie and series
            this.getHandler().getConnectionHandler().connect(
                    this.getSearchString(),
                    offset,
                    Constants.NR_OF_ITEMS,
                    ViewMode.Movie,
                    -1,
                    -1,
                    true,
                    true);

            this.getHandler().getConnectionHandler().connect(
                    this.getSearchString(),
                    offset,
                    Constants.NR_OF_ITEMS,
                    ViewMode.Series,
                    -1,
                    -1,
                    true,
                    true);
        }
    }

    @Override
    public void handleMoviesUpdated(List<JukeboxDomain.Movie> movies, int totalMovies) {
        this.setRefreshing(false);
        final MosLayoutAdapter adapter = this.getMosLayoutAdapter();
        if (adapter != null) {
            setLoading(false);
            this.setTotalMovies(totalMovies);

            this.getActivity().runOnUiThread(() -> {
                adapter.addMovies(movies);
                adapter.setServerListSize(getTotalItems());
                adapter.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void handleSeriesUpdated(List<JukeboxDomain.Series> series, int totalSeries) {
        this.setRefreshing(false);
        final MosLayoutAdapter adapter = this.getMosLayoutAdapter();

        if (adapter != null) {
            setLoading(false);
            this.setTotalSeries(totalSeries);
            this.getActivity().runOnUiThread(() -> {

                adapter.addSeries(series);
                adapter.setServerListSize(getTotalItems());
                adapter.notifyDataSetChanged();
            });
        }
    }
}
