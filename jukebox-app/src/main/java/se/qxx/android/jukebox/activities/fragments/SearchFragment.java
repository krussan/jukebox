package se.qxx.android.jukebox.activities.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.android.jukebox.adapters.MosLayoutAdapter;
import se.qxx.android.jukebox.model.Constants;
import se.qxx.jukebox.domain.JukeboxDomain;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchFragment extends JukeboxFragment {

    private static final String TAG = "SearchFragment";

    private int totalMovies = 0;
    private int totalSeries = 0;
    private CyclicBarrier barrier = new CyclicBarrier(2);
    private AtomicBoolean adapterNotified = new AtomicBoolean(false);

    public SearchFragment() {
        super();
        barrier.reset();
    }

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

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.jukebox_main_wrapper, container, false);
//        initializeView(v);
//        return v;
//    }
//
//    protected void initializeView(View v) {
//        super.initializeView(v);
//    }

    public static SearchFragment newInstance() {
        Bundle b = new Bundle();
        b.putSerializable("mode", ViewMode.Search);
        SearchFragment f = new SearchFragment();
        f.setArguments(b);

        return f;
    }

    public void search(String query) {
        this.adapterNotified.set(false);
        this.setSearchString(query);
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
            this.setTotalMovies(movies.size());

            try {
                adapter.addMovies(movies);
                adapter.setServerListSize(getTotalItems());

                barrier.await();
                if (!adapterNotified.getAndSet(true))
                    this.getActivity().runOnUiThread(adapter::notifyDataSetChanged);

            } catch (Exception loggedAndIgnored) {
                Log.e(TAG, "Barrier error", loggedAndIgnored);
            }
        }
    }

    @Override
    public void handleSeriesUpdated(List<JukeboxDomain.Series> series, int totalSeries) {
        this.setRefreshing(false);
        final MosLayoutAdapter adapter = this.getMosLayoutAdapter();

        if (adapter != null) {
            setLoading(false);
            this.setTotalSeries(series.size());

            try {
                adapter.addSeries(series);
                adapter.setServerListSize(getTotalItems());

                barrier.await();
                if (!adapterNotified.getAndSet(true))
                    this.getActivity().runOnUiThread(adapter::notifyDataSetChanged);

            } catch (Exception loggedAndIgnored) {
                Log.e(TAG, "Barrier error", loggedAndIgnored);
            }
        }
    }
}
