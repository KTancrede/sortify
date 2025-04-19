package com.tancrede.spotifytinder.controller;

import java.util.List;

import com.tancrede.spotifytinder.model.PlaylistInfo;
import com.tancrede.spotifytinder.model.TrackInfo;
import com.tancrede.spotifytinder.model.UserInfo;
import com.tancrede.spotifytinder.spotify.SpotifyPlaylistManager;
import com.tancrede.spotifytinder.spotify.SpotifyTrackFetcher;

public class TinderController {
    
    private UserInfo userInfo;
    private List<TrackInfo> likedTracks;
    private List<PlaylistInfo> playlists;
    
    private SpotifyPlaylistManager playlistManager;
    
    private int currentTrackIndex = 0;
    
    private boolean autoUnlike = false;
    
    public TinderController(UserInfo userInfo, List<TrackInfo> likedTracks, List<PlaylistInfo> playlists,SpotifyPlaylistManager playlistManager) {
        this.userInfo = userInfo;
        this.likedTracks = likedTracks;
        this.playlists = playlists;
        this.playlistManager=playlistManager;
    }

    // === Accès aux infos ===
    public UserInfo getUserInfo() {
        return userInfo;
    }

    public List<TrackInfo> getLikedTracks() {
        return likedTracks;
    }

    public List<PlaylistInfo> getPlaylists() {
        return playlists;
    }

    // === Navigation dans les musiques ===
    public TrackInfo getCurrentTrack() {
        if (likedTracks.isEmpty()) return null;
        return likedTracks.get(currentTrackIndex);
    }

    public void nextTrack() {
        if (likedTracks.isEmpty()) return;
        currentTrackIndex = (currentTrackIndex + 1) % likedTracks.size();
    }

    public void previousTrack() {
        if (likedTracks.isEmpty()) return;
        currentTrackIndex = (currentTrackIndex - 1 + likedTracks.size()) % likedTracks.size();
    }
    
    // === Ajout d'une nouvelle playlist === 
    
    public void createNewPlaylist(String name, String description, boolean isPublic) {
        playlistManager.createPlaylist(name, description, isPublic);
        this.playlists = playlistManager.getAllPlaylists(); // refresh
    }

    // === Ajout à une playlist (à implémenter plus tard) ===
    public void addToPlaylist(String playlistId) {
        TrackInfo track = getCurrentTrack();
        if (track != null) {
            // Appeler ici une classe manager qui utilise SpotifyApi
            // Exemple : spotifyPlaylistManager.addTrackToPlaylist(track.getId(), playlistId);
        }
    }
    
    // === Unfollow Playlist ===
    public void deletePlaylist(String playlistId) {
        playlistManager.unfollowPlaylist(playlistId);
        this.playlists = playlistManager.getAllPlaylists(); // Refresh
    }
    
    public void addTrackToPlaylist(String trackId, String playlistId) {
        playlistManager.addTrackToPlaylist(trackId, playlistId);
    }

    public void unlikeTrack(String trackId) {
        playlistManager.unlikeTrack(trackId);
        removeTrackFromLiked(trackId);
    }

    public void removeTrackFromLiked(String trackId) {
        likedTracks.removeIf(track -> track.getTrackId().equals(trackId));
    }
    
    

    public boolean isAutoUnlikeEnabled() {
        return autoUnlike;
    }

    public void setAutoUnlike(boolean enabled) {
        this.autoUnlike = enabled;
    }


}
