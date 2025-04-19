package com.tancrede.spotifytinder;

import com.tancrede.spotifytinder.model.UserInfo;
import com.tancrede.spotifytinder.spotify.SpotifyAuthManager;
import com.tancrede.spotifytinder.spotify.SpotifyPlaylistManager;
import com.tancrede.spotifytinder.spotify.SpotifyTrackFetcher;
import com.tancrede.spotifytinder.ui.TinderUI;

import javafx.application.Application;
import se.michaelthelin.spotify.SpotifyApi;

public class Testa {

	public static void main(String[] args) {
		System.out.println("Hello Spotify Tinder!");
		
		// Connexion API
		SpotifyAuthManager authManager = new SpotifyAuthManager();
		authManager.authorizeUser();
		SpotifyApi spotifyApi = authManager.getSpotifyApi();
		
		// On mets les infos dans user
		UserInfo userInfo = new UserInfo(spotifyApi);
						
		// Récupération des titres liked
		SpotifyTrackFetcher spotifyTrackFetcher = new SpotifyTrackFetcher(spotifyApi);
		spotifyTrackFetcher.getLikedTracks();
		System.out.println(spotifyTrackFetcher+"\n");
		
		// Récupération des playlist de l'user
		SpotifyPlaylistManager spotifyPlaylistManager = new SpotifyPlaylistManager(spotifyApi);
		spotifyPlaylistManager.getAllPlaylists();
		System.out.println(spotifyPlaylistManager.toString()+"\n");
		
		// Création d'une playlist et affichage
		spotifyPlaylistManager.createPlaylist("test", "ceci nest pas une description", false);
		spotifyPlaylistManager.getAllPlaylists();
		System.out.println(spotifyPlaylistManager.toString()+"\n");
		
		
		
		
	}

}
