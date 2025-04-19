package com.tancrede.spotifytinder.spotify;

import com.tancrede.spotifytinder.model.TrackInfo;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.library.GetUsersSavedTracksRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.core5.http.ParseException;

public class SpotifyTrackFetcher {
	private SpotifyApi spotifyApi;
	private List<TrackInfo> trackList=new ArrayList<>();
	
	
    public SpotifyTrackFetcher(SpotifyApi spotifyApi) {
		super();
		this.spotifyApi = spotifyApi;
	}

    public List<TrackInfo> getLikedTracks() {
        int offset = 0;
        int limit = 50;
        boolean hasMore = true;

        while (hasMore) {
            GetUsersSavedTracksRequest request = spotifyApi
                    .getUsersSavedTracks()
                    .limit(limit)
                    .offset(offset)
                    .build();

            try {
                Paging<SavedTrack> savedTracks = request.execute();
                SavedTrack[] items = savedTracks.getItems();

                for (SavedTrack musique : items) {
                    Track m = musique.getTrack();

                    TrackInfo trackInfo = new TrackInfo(
                            m.getName(),
                            m.getArtists(), // tu peux formater plus tard
                            m.getPreviewUrl(), // lien de 30 secondes pour ecouter la musique
                            m.getAlbum().getImages()[0].getUrl(),
                            m.getId(),
                            m.getAlbum(),
                            m.getDurationMs() // La durée en ms
                    );

                    trackList.add(trackInfo);
                }

                // Vérifie s’il reste encore des morceaux
                hasMore = savedTracks.getNext() != null;
                offset += limit;

            } catch (ParseException | SpotifyWebApiException | IOException e) {
                e.printStackTrace();
                hasMore = false; // arrête la boucle en cas d’erreur
            }
        }

        return trackList;
    }

	@Override
	public String toString() {
		
		StringBuilder sb= new StringBuilder();
		sb.append("Les " + trackList.size() + " titres liked sont : \n");
		
		for(TrackInfo t : trackList) 
			sb.append(t+"\n");
		
		return sb.toString();
	}
}
