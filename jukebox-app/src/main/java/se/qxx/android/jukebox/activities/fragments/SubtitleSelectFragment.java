package se.qxx.android.jukebox.activities.fragments;


import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.support.SubtitleLayoutAdapter;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain;

/**
 * A simple {@link Fragment} subclass.
 */
public class SubtitleSelectFragment extends DialogFragment implements SubtitleLayoutAdapter.SubtitleSelectedListener {

    private JukeboxDomain.Media mMedia;
    private SubtitleSelectDialogListener mListener;
    private JukeboxSettings settings;

    public interface SubtitleSelectDialogListener {
        void SubtitleSelected(JukeboxDomain.SubtitleUri subtitleUri);
    }

    public SubtitleSelectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMedia = (JukeboxDomain.Media)getArguments().getSerializable("media");
        }

        settings = new JukeboxSettings(getContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.subtitlepicker, container, false);

        initializeView(v);
        return v;
    }

    private void initializeView(View v) {
        GUITools.setTextOnTextview(R.id.lblSubpickerFilename, mMedia.getFilename(), v);

        initializeSubtitles(v);
    }

    protected void initializeSubtitles(View v) {
        final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
                settings.getServerIpAddress(),
                settings.getServerPort());

        final ListView lv = (ListView)v.findViewById(R.id.listSubtitlePicker);

        jh.listSubtitles(
                mMedia,
                (response) -> {
                    getActivity().runOnUiThread(() -> {
                        SubtitleLayoutAdapter adapter =
                                new SubtitleLayoutAdapter(getContext(), mMedia.getID(), response.getSubtitleUrisList(), this);
                        lv.setOnItemClickListener(adapter);
                        lv.setAdapter(adapter);
                    });
                });

    }

    public static SubtitleSelectFragment newInstance(JukeboxDomain.Media media, SubtitleSelectDialogListener listener) {
        SubtitleSelectFragment fragment = new SubtitleSelectFragment();
        fragment.mListener = listener;

        Bundle args = new Bundle();
        args.putSerializable("media", media);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onSubtitleSelected(JukeboxDomain.SubtitleUri subtitleUri) {
        mListener.SubtitleSelected(subtitleUri);
        dismiss();
    }
}
