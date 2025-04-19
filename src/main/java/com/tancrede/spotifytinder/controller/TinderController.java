package com.tancrede.spotifytinder.controller;

import com.tancrede.spotifytinder.model.PlaylistInfo;
import com.tancrede.spotifytinder.model.TrackInfo;
import com.tancrede.spotifytinder.model.UserInfo;
import com.tancrede.spotifytinder.spotify.SpotifyPlaylistManager;
import com.tancrede.spotifytinder.spotify.SpotifyTrackFetcher;

import java.util.List;

public class TinderController {

    // === Données ===
    private UserInfo userInfo;
    private List<TrackInfo> likedTracks;
    private List<PlaylistInfo> playlists;

    // === Composants métiers ===
    private final SpotifyPlaylistManager playlistManager;
    private final SpotifyTrackFetcher trackFetcher;

    // === État de l'application ===
    private int currentTrackIndex = 0;
    private boolean autoUnlike = false;

    // === Constructeur ===
    public TinderController(UserInfo userInfo,
                            List<TrackInfo> likedTracks,
                            List<PlaylistInfo> playlists,
                            SpotifyPlaylistManager playlistManager,
                            SpotifyTrackFetcher trackFetcher) {

        this.userInfo = userInfo;
        this.likedTracks = likedTracks;
        this.playlists = playlists;
        this.playlistManager = playlistManager;
        this.trackFetcher = trackFetcher;
    }

    // === Getters ===
    public UserInfo getUserInfo() {
        return userInfo;
    }

    public List<TrackInfo> getLikedTracks() {
        return likedTracks;
    }

    public List<PlaylistInfo> getPlaylists() {
        return playlists;
    }

    public boolean isAutoUnlikeEnabled() {
        return autoUnlike;
    }

    // === Setters ===
    public void setAutoUnlike(boolean enabled) {
        this.autoUnlike = enabled;
    }

    // === Navigation dans les musiques ===
    public TrackInfo getCurrentTrack() {
        if (likedTracks.isEmpty()) return null;
        return likedTracks.get(currentTrackIndex);
    }
    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public void nextTrack() {
        if (likedTracks.isEmpty()) return;

        currentTrackIndex = (currentTrackIndex + 1) % likedTracks.size();
        System.out.println("[DEBUG] nextTrack() → Index now: " + currentTrackIndex);
    }


    public void previousTrack() {
        if (!likedTracks.isEmpty()) {
            currentTrackIndex = (currentTrackIndex - 1 + likedTracks.size()) % likedTracks.size();
        }
    }

    // === Gestion des playlists ===
    public void createNewPlaylist(String name, String description, boolean isPublic) {
        playlistManager.createPlaylist(name, description, isPublic);
        refreshPlaylists();
    }

    public void deletePlaylist(String playlistId) {
        playlistManager.unfollowPlaylist(playlistId);
        refreshPlaylists();
    }

    public void refreshPlaylists() {
        this.playlists = playlistManager.getAllPlaylists();
    }

    // === Ajout et suppression de tracks ===
    public void addTrackToPlaylist(String trackId, String playlistId) {
        playlistManager.addTrackToPlaylist(trackId, playlistId);
    }

    public void unlikeTrack(String trackId) {
        playlistManager.unlikeTrack(trackId);
        removeTrackFromLiked(trackId); // aussi en local
    }


    public void removeTrackFromLiked(String trackId) {
        likedTracks.removeIf(track -> track.getTrackId().equals(trackId));
    }

    public void refreshLikedTracks() {
        this.likedTracks = trackFetcher.getLikedTracks(); // à jour depuis l’API
    }

    // === Opérations de transfert ===
    public void transferPlaylistToLiked(String playlistId) {
        playlistManager.transferPlaylistToLiked(playlistId);
        refreshLikedTracks(); // on rafraîchit après
    }

    public void likeAllTracksFromPlaylist(String playlistId) {
        List<String> trackIds = playlistManager.getTracksFromPlaylist(playlistId);
        playlistManager.likeTracks(trackIds);
        refreshLikedTracks();
    }
}
