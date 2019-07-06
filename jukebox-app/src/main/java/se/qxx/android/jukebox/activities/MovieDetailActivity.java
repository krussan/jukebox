package se.qxx.android.jukebox.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.support.MovieMediaLayoutAdapter;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.settings.CacheData;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.Sorter;

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

            GUITools.setTextOnTextview(R.id.textViewTitle, m.getTitle(), v);
            GUITools.setTextOnTextview(R.id.textViewYear, Integer.toString(m.getYear()), v);
            GUITools.setTextOnTextview(R.id.textViewStory, m.getStory(), v);
            GUITools.setTextOnTextview(R.id.textViewGenre, String.format("Genre :: %s", StringUtils.join(m.getGenreList(), " / ")), v);
            GUITools.setTextOnTextview(R.id.textViewDirector, String.format("Director :: %s", m.getDirector()), v);
            GUITools.setTextOnTextview(R.id.textViewDuration,  String.format("Duration :: %s", getDuration(m.getDuration())), v);
            GUITools.setTextOnTextview(R.id.textViewRating, String.format("Rating :: %s / 10", m.getRating()), v);
            //GUITools.setTextOnTextview(R.id.textViewFilename, String.format("Filename :: %s", m.getFilename()), v);

            MovieMediaLayoutAdapter adapter = new MovieMediaLayoutAdapter(v.getContext(), m);
            ListView listView = v.findViewById(R.id.listViewFilename);
            listView.setAdapter(adapter);

            initializeDetails(m, v);
            // updateProgressBar(m);

            //detector = new SimpleGestureFilter(this, this);
        }

    }

    private String getDuration(int duration) {
        int hours = duration / 60;
        int minutes = duration % 60;

        return String.format("%s h %s m", hours, minutes);
    }

    private void initializeDetails(JukeboxDomain.Movie m, View v) {
        GUITools.setTextOnTextview(R.id.txtDetailDuration, getDuration(m.getDuration()), v);
        GUITools.setTextOnTextview(R.id.txtDetailFormat, m.getFormat(), v);
        GUITools.setTextOnTextview(R.id.txtDetailGenre, StringUtils.join(m.getGenreList(), " / "), v);
        
        GUITools.setTextOnTextview(R.id.txtDetailGroup, m.getGroupName(), v);
        GUITools.setTextOnTextview(R.id.txtDetailLanguage, m.getLanguage(), v);
        GUITools.setTextOnTextview(R.id.txtDetailSound, m.getSound(), v);
        GUITools.setTextOnTextview(R.id.txtDetailType, m.getType(), v);
        GUITools.setTextOnTextview(R.id.txtDetailBlacklisted, Integer.toString(m.getBlacklistCount()), v);
        GUITools.setTextOnTextview(R.id.txtDetailIdentifier, m.getIdentifier().name(), v);
        GUITools.setTextOnTextview(R.id.txtDetailIdentRating, Integer.toString(m.getIdentifierRating()), v);
        GUITools.setTextOnTextview(R.id.txtDetailSubQueuedAt, formatDateTime(m.getSubtitleQueue().getSubtitleQueuedAt() * 1000), v);
        GUITools.setTextOnTextview(R.id.txtDetailSubRetreivedAt, formatDateTime(m.getSubtitleQueue().getSubtitleRetreivedAt() * 1000), v);
        GUITools.setTextOnTextview(R.id.txtDetailSubResult, getSubtitleResult(m), v);
        GUITools.setTextOnTextview(R.id.txtDetailWatched, Boolean.toString(m.getWatched()), v);

        if (!m.getMediaList().isEmpty()) {
            List<JukeboxDomain.Subtitle> sortedSubtitles = Sorter.sortSubtitlesByRating(m.getMedia(0).getSubsList());

            GUITools.setTextOnTextview(R.id.txtDetailConverterState, m.getMedia(0).getConverterState().name(), v);
            GUITools.setTextOnTextview(R.id.txtDetailConvertedFilename, m.getMedia(0).getConvertedFileName(), v);
            GUITools.setTextOnTextview(R.id.txtDetailFramerate, m.getMedia(0).getMetaFramerate(), v);
            GUITools.setTextOnTextview(R.id.txtDetailDownloadComplete, Boolean.toString(m.getMedia(0).getDownloadComplete()), v);
            GUITools.setTextOnTextview(R.id.txtDetailMetaDuration, getDuration(m.getMedia(0).getMetaDuration() / 60), v);
            GUITools.setTextOnTextview(R.id.txtDetailSubsCount, Integer.toString(m.getMedia(0).getSubsCount()), v);

            if (!sortedSubtitles.isEmpty())
                GUITools.setTextOnTextview(R.id.txtDetailSubsRating, sortedSubtitles.get(0).getRating().name(), v);
        }

    }

    private String getSubtitleResult(JukeboxDomain.Movie m) {
        int result = m.getSubtitleQueue().getSubtitleRetreiveResult();
        switch (result) {
            case -1:
                return "Failed";
            case 1:
                return "Success";
            default:
                return "Queued";
        }
    }

    private String formatDateTime(long timestamp) {
        Date dd = new Date(timestamp);
        DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(getApplicationContext());
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());

        return String.format("%s %s", dateFormat.format(dd), timeFormat.format(dd));
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
