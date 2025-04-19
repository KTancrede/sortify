package com.tancrede.spotifytinder.spotify;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.core5.http.ParseException;

import com.tancrede.spotifytinder.model.PlaylistInfo;

public class SpotifyPlaylistManager {

	private SpotifyApi spotifyApi;
	private List<PlaylistInfo> playlists = new ArrayList<>();

	public SpotifyPlaylistManager(SpotifyApi spotifyApi) {
		this.spotifyApi = spotifyApi;
	}

	public List<PlaylistInfo> getAllPlaylists() {
		playlists.clear();

		int offset = 0;
		int limit = 50;
		boolean hasMore = true;

		while (hasMore) {
			GetListOfCurrentUsersPlaylistsRequest request = spotifyApi
					.getListOfCurrentUsersPlaylists()
					.limit(limit)
					.offset(offset)
					.build();

			try {
				Paging<PlaylistSimplified> response = request.execute();
				PlaylistSimplified[] items = response.getItems();

				for (PlaylistSimplified playlist : items) {
					String imageUrl = null;
				    if (playlist.getImages() != null && playlist.getImages().length > 0) {
				        imageUrl = playlist.getImages()[0].getUrl();
				    }
				    String description = (playlist.getDescription() != null) ? playlist.getDescription() : "Pas de description";

					PlaylistInfo pi = new PlaylistInfo(
							playlist.getName(), 
							playlist.getId(), 
							playlist.getIsCollaborative(), 
							playlist.getIsPublicAccess(), 
							imageUrl, 
							description
							);
					playlists.add(pi);
				}

				hasMore = response.getNext() != null;
				offset += limit;

			} catch (IOException | SpotifyWebApiException | ParseException e) {
				System.out.println("Erreur pendant la récupération des playlists : " + e.getMessage());
				break;
			}
		}

		return playlists;
	}

	public void createPlaylist(String name,String description,boolean public_) {
		try{
			// On récupère l'userID
			User user=spotifyApi.getCurrentUsersProfile().build().execute();
			String userID=user.getId();
			
			CreatePlaylistRequest request = spotifyApi.createPlaylist(userID, name)
					.public_(public_)
					.description(description)
					.build();
			request.execute();
			
		}catch (Exception e) {
			System.out.println("❌ Erreur création playlist : " + e.getMessage());
	        e.printStackTrace();
		}
		
	}
	
	public void unfollowPlaylist(String playlistId) {
	    try {
	        spotifyApi.unfollowPlaylist(playlistId).build().execute();
	        System.out.println("✅ Playlist supprimée.");
	    } catch (Exception e) {
	        System.out.println("❌ Erreur suppression playlist : " + e.getMessage());
	    }
	}

	public void addTrackToPlaylist(String trackId, String playlistId) {
        try {
            spotifyApi.addItemsToPlaylist(playlistId, new String[] { "spotify:track:" + trackId })
                      .build()
                      .execute();
            System.out.println("🎵 Track ajouté à la playlist !");
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("❌ Erreur lors de l’ajout à la playlist : " + e.getMessage());
        }
    }
    
    public void unlikeTrack(String trackId) {
        try {
            spotifyApi.removeUsersSavedTracks(new String[] { trackId })
                      .build()
                      .execute();
            System.out.println("🗑️ Track retiré des musiques likées !");
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("❌ Erreur lors du unlike : " + e.getMessage());
        }
    }
    
    
    public List<String> getTracksFromPlaylist(String playlistId) {
        List<String> trackIds = new ArrayList<>();

        int offset = 0;
        int limit = 100;
        boolean hasMore = true;

        while (hasMore) {
            try {
                var request = spotifyApi.getPlaylistsItems(playlistId)
                                        .limit(limit)
                                        .offset(offset)
                                        .build();
                var response = request.execute();
                var items = response.getItems();

                for (var item : items) {
                    var track = (se.michaelthelin.spotify.model_objects.specification.Track) item.getTrack();
                    if (track != null) {
                        trackIds.add(track.getId());
                    }
                }

                hasMore = response.getNext() != null;
                offset += limit;

            } catch (Exception e) {
                System.out.println("❌ Erreur récupération morceaux de playlist : " + e.getMessage());
                break;
            }
        }

        return trackIds;
    }
    
    public void likeTracks(List<String> trackIds) {
        try {
            for (int i = 0; i < trackIds.size(); i += 50) {
                int end = Math.min(i + 50, trackIds.size());
                List<String> batch = trackIds.subList(i, end);

                spotifyApi.saveTracksForUser(batch.toArray(new String[0]))
                          .build()
                          .execute();
            }
            System.out.println("❤️ Tous les morceaux ont été ajoutés aux musiques likées !");
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du like en masse : " + e.getMessage());
        }
    }

    public void transferPlaylistToLiked(String playlistId) {
        try {
            var items = spotifyApi.getPlaylistsItems(playlistId).limit(100).build().execute().getItems();
            for (var item : items) {
                if (item.getTrack() != null && item.getTrack().getId() != null) {
                    spotifyApi.saveTracksForUser(new String[] { item.getTrack().getId() }).build().execute();
                    System.out.println("✅ Ajouté aux titres likés : " + item.getTrack().getName());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur pendant le transfert des morceaux : " + e.getMessage());
        }
    }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Les "+playlists.size()+ " playlists sont:\n");

		for (PlaylistInfo p : playlists)
			sb.append(p.getName()+" | id = " +p.getId()+" | Description : "+p.getDescription()+"\n");


		return sb.toString();
	}
}
