package se.qxx.android.jukebox.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.support.MovieMediaLayoutAdapter;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.settings.CacheData;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

public class MovieDetailActivity extends AppCompatActivity {

    private JukeboxSettings settings;
    private CacheData cacheData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cacheData = new CacheData(this);
        settings = new JukeboxSettings(this);
        setContentView(R.layout.movieitem);

        initializeView();
    }

    private void initializeView() {
        JukeboxDomain.Movie m = this.getMovie();
        View v = findViewById(android.R.id.content);

        if (m != null) {
            if (!m.getThumbnail().isEmpty()) {
                Bitmap bm = GUITools.getBitmapFromByteArray(m.getThumbnail().toByteArray());
                Bitmap scaledImage = GUITools.scaleImage(120, bm, getApplicationContext());

                GUITools.setImageOnImageView(R.id.imageView1, scaledImage, v);
            }

            int duration = m.getDuration();
            int hours = duration / 60;
            int minutes = duration % 60;

            GUITools.setTextOnTextview(R.id.textViewTitle, m.getTitle(), v);
            GUITools.setTextOnTextview(R.id.textViewYear, Integer.toString(m.getYear()), v);
            GUITools.setTextOnTextview(R.id.textViewStory, m.getStory(), v);
            GUITools.setTextOnTextview(R.id.textViewGenre, String.format("Genre :: %s", StringUtils.join(m.getGenreList(), " / ")), v);
            GUITools.setTextOnTextview(R.id.textViewDirector, String.format("Director :: %s", m.getDirector()), v);
            GUITools.setTextOnTextview(R.id.textViewDuration, String.format("Duration :: %s h %s m", hours, minutes) , v);
            GUITools.setTextOnTextview(R.id.textViewRating, String.format("Rating :: %s / 10", m.getRating()), v);
            //GUITools.setTextOnTextview(R.id.textViewFilename, String.format("Filename :: %s", m.getFilename()), v);

            MovieMediaLayoutAdapter adapter = new MovieMediaLayoutAdapter(v.getContext(), m);
            ListView listView = (ListView)v.findViewById(R.id.listViewFilename);
            listView.setAdapter(adapter);

            // updateProgressBar(m);

            //detector = new SimpleGestureFilter(this, this);
        }

    }

    private void updateProgressBar(JukeboxDomain.Movie m) {
        if (m!= null) {
            ProgressBar progressWatched = findViewById(R.id.progressWatched);
            int progress = 0;
            int duration = m.getDuration();
            if (duration > 0) {
                int position = getCachedPosition(m.getMedia(0).getID());
                progress = (int) (100f * (float) position / (float) (duration * 60));
            }
            progressWatched.setProgress(progress);
        }
    }

    private JukeboxDomain.Movie getMovie() {
        Bundle b = getIntent().getExtras();

        if (b!= null) {
            return (JukeboxDomain.Movie)b.getSerializable("movie");
        }

        return null;
    }


    public void onButtonClicked(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btnPlay:
                Intent iPlay = new Intent(this, NowPlayingActivity.class);
                iPlay.putExtra("mode", ViewMode.Movie);
                iPlay.putExtra("ID", this.getMovie().getID());
                startActivity(iPlay);
                break;
            case R.id.btnViewInfo:
                String url = this.getMovie().getImdbUrl();
                if (url != null && url.length() > 0) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
                else {
                    Toast.makeText(this, "No IMDB link available", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu, settings.getCurrentMediaPlayer());

        return true;
    }


    public int getCachedPosition(int mediaID) {
        return cacheData.getMediaState(mediaID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProgressBar(this.getMovie());
    }

}
