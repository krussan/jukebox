package se.qxx.android.jukebox.comm;

import se.qxx.android.jukebox.model.Model;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListSubtitles;

import com.google.protobuf.RpcCallback;

public class OnListSubtitlesCompleteHandler implements RpcCallback<JukeboxResponseListSubtitles> {

	@Override
	public void run(JukeboxResponseListSubtitles response) {
		// TODO Auto-generated method stub
		Model.get().clearSubtitles();
		Model.get().addAllSubtitles(response.getSubtitleList());
	}

}
