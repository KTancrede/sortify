package com.tancrede.spotifytinder;

import com.tancrede.spotifytinder.model.PlaylistInfo;
import com.tancrede.spotifytinder.model.TrackInfo;
import com.tancrede.spotifytinder.model.UserInfo;
import com.tancrede.spotifytinder.spotify.SpotifyAuthManager;
import com.tancrede.spotifytinder.spotify.SpotifyPlaylistManager;
import com.tancrede.spotifytinder.spotify.SpotifyTrackFetcher;
import com.tancrede.spotifytinder.ui.TinderUI;

import java.util.List;

import com.tancrede.spotifytinder.controller.TinderController;

import javafx.application.Application;
import se.michaelthelin.spotify.SpotifyApi;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello Spotify Tinder!");

        try {
            // Auth
            SpotifyAuthManager authManager = new SpotifyAuthManager();
            authManager.authorizeUser();
            SpotifyApi spotifyApi = authManager.getSpotifyApi();

            // Infos user
            UserInfo userInfo = new UserInfo(spotifyApi);
            if (userInfo.getDisplayName() == null) {
                System.out.println("❌ Impossible de récupérer le profil utilisateur.");
                return;
            }

            // Tracks
            SpotifyTrackFetcher trackFetcher = new SpotifyTrackFetcher(spotifyApi);
            List<TrackInfo> tracks = trackFetcher.getLikedTracks();
            if (tracks == null || tracks.isEmpty()) {
                System.out.println("⚠️ Aucun morceau liké récupéré !");
            }

            // Playlists
            SpotifyPlaylistManager playlistManager = new SpotifyPlaylistManager(spotifyApi);
            List<PlaylistInfo> playlists = playlistManager.getAllPlaylists();

            // Controller
            TinderController controller = new TinderController(userInfo, tracks, playlists, playlistManager);
            TinderUI.setController(controller);

            // Lancement de l'UI
            Application.launch(TinderUI.class, args);

        } catch (Exception e) {
            System.out.println("❌ Une erreur est survenue pendant l'initialisation de l'application.");
            e.printStackTrace();
        }
    }
}

