package com.tancrede.spotifytinder.model;

import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

public class TrackInfo {
    private String title;
    private ArtistSimplified[] artists;
    private String previewUrl;
    private String imageUrl;
    private String trackId;
    private AlbumSimplified album;
    private Integer duree;
    
    public TrackInfo(String title, ArtistSimplified[] artists, String previewUrl, String imageUrl, String trackId, AlbumSimplified albumSimplified,Integer duree) {
        this.title = title;
        this.artists = artists;
        this.previewUrl = previewUrl;
        this.imageUrl = imageUrl;
        this.trackId = trackId;
        this.album=albumSimplified;
        this.duree=duree;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < artists.length; i++) {
            sb.append(artists[i].getName());
            if (i < artists.length - 1) {
                sb.append(" - "); // ajoute un tiret sauf après le dernier
            }
        }
        return sb.toString();
    }


    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTrackId() {
        return trackId;
    }

	public AlbumSimplified getAlbum() {
		return album;
	}
	
	@Override
    public String toString() {
        return title + " - " + getArtist();
    }

	public Integer getDuree() {
		return duree;
	}
}
