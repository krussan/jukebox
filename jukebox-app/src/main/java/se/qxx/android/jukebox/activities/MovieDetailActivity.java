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

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
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
            GUITools.setTextOnTextview(R.id.textViewDuration,  getDuration(m.getDuration()), v);
            GUITools.setTextOnTextview(R.id.textViewRating, String.format("Rating :: %s / 10", m.getRating()), v);
            //GUITools.setTextOnTextview(R.id.textViewFilename, String.format("Filename :: %s", m.getFilename()), v);

            MovieMediaLayoutAdapter adapter = new MovieMediaLayoutAdapter(v.getContext(), m);
            ListView listView = v.findViewById(R.id.listViewFilename);
            listView.setAdapter(adapter);

            initializeDetails(m);
            // updateProgressBar(m);

            //detector = new SimpleGestureFilter(this, this);
        }

    }

    private String getDuration(int duration) {
        int hours = duration / 60;
        int minutes = duration % 60;

        return String.format("Duration :: %s h %s m", hours, minutes);
    }

    private void initializeDetails(JukeboxDomain.Movie m) {
        TableView<String[]> tableView = findViewById(R.id.tableView);
        String[][] data = getDetailsData(m);
        tableView.setDataAdapter(new SimpleTableDataAdapter(this, data));
    }

    private String[][] getDetailsData(JukeboxDomain.Movie m) {

        List<String[]> data = new ArrayList<>();
        data.add(new String[] {"Duration", Integer.toString(m.getDuration())});
        data.add(new String[] {"Format", m.getFormat()});
        data.add(new String[] {"Genre", StringUtils.join(m.getGenreList(), " / ")});
        data.add(new String[] {"Group", m.getGroupName()});
        data.add(new String[] {"Language", m.getLanguage()});
        data.add(new String[] {"Sound", m.getSound()});
        data.add(new String[] {"Type", m.getType()});
        data.add(new String[] {"Blacklisted", Integer.toString(m.getBlacklistCount())});
        data.add(new String[] {"Identifier", m.getIdentifier().name()});
        data.add(new String[] {"Identifier", Integer.toString(m.getIdentifierRating())});
        data.add(new String[] {"Sub_QueuedAt", formatDateTime(m.getSubtitleQueue().getSubtitleQueuedAt())});
        data.add(new String[] {"Sub_RetreivedAt", formatDateTime(m.getSubtitleQueue().getSubtitleRetreivedAt())});
        data.add(new String[] {"Sub_Result", getSubtitleResult(m)});
        data.add(new String[] {"Sub_Result", Boolean.toString(m.getWatched())});

        if (!m.getMediaList().isEmpty()) {
            List<JukeboxDomain.Subtitle> sortedSubtitles = Sorter.sortSubtitlesByRating(m.getMedia(0).getSubsList());

            data.add(new String[]{"Converter_state", m.getMedia(0).getConverterState().name()});
            data.add(new String[]{"Converter_filename", m.getMedia(0).getConvertedFileName()});
            data.add(new String[]{"Framerate", m.getMedia(0).getMetaFramerate()});
            data.add(new String[]{"Download complete", Boolean.toString(m.getMedia(0).getDownloadComplete())});
            data.add(new String[]{"Meta duration", Integer.toString(m.getMedia(0).getMetaDuration())});
            data.add(new String[]{"Meta duration", getDuration(m.getMedia(0).getMetaDuration())});
            data.add(new String[]{"Subs_Count", Integer.toString(m.getMedia(0).getSubsCount())});
            if (!sortedSubtitles.isEmpty())
                data.add(new String[]{"Subs_Rating", sortedSubtitles.get(0).getRating().name()});
        }

        return data.toArray(new String[][] {{}});

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
        DateFormat format = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        return format.format(dd);
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
