package com.tancrede.spotifytinder.model;

import java.io.IOException;

import org.apache.hc.core5.http.ParseException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.User;

public class UserInfo {
	private String id;
	private String displayName;
	
	
	public UserInfo(SpotifyApi spotifyApi) {
		try {
			User user=spotifyApi.getCurrentUsersProfile().build().execute();
			
			this.id = user.getId();
			this.displayName = user.getDisplayName();
			
		} catch (ParseException | SpotifyWebApiException | IOException e) {
			e.printStackTrace();
		}
	}
	public String getId() {
		return id;
	}
	public String getDisplayName() {
		return displayName;
	}
}
