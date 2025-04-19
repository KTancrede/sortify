package com.tancrede.spotifytinder.model;


public class PlaylistInfo {
	private String name;
	private String id;
	private boolean isCollaborative;
	private boolean isPublic;
	private String imageUrl;
	private String description;
	
	public PlaylistInfo(String name, String id, boolean isCollaborative, boolean isPublic, String imageUrl,String description) {

		this.name = name;
		this.id = id;
		this.isCollaborative = isCollaborative;
		this.isPublic = isPublic;
		this.imageUrl = imageUrl;
		this.description = description;
		
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public boolean isCollaborative() {
		return isCollaborative;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getDescription() {
		return description;
	}
	
	
	
}
