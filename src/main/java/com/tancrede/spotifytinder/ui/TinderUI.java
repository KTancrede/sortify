package com.tancrede.spotifytinder.ui;

import com.tancrede.spotifytinder.controller.TinderController;
import com.tancrede.spotifytinder.model.PlaylistInfo;
import com.tancrede.spotifytinder.model.TrackInfo;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class TinderUI extends Application {

    private static TinderController controller;
    private VBox leftPlaylistsBox;
    private VBox rightPlaylistsBox;
    private Label userInfoLabel;
    private VBox trackCardBox;

    public static void setController(TinderController c) {
        controller = c;
    }

    // === TOP HEADER ===
    private Node buildTopHeader() {
        userInfoLabel = new Label();
        userInfoLabel.setStyle("-fx-font-size: 16px;"); // CSS: .user-info-label
        updateUserInfoLabel();

        ToggleButton autoUnlikeToggle = new ToggleButton("Auto-Unlike 🚫❤️");
        autoUnlikeToggle.getStyleClass().add("auto-unlike-toggle");

        //autoUnlikeToggle.setStyle("-fx-font-size: 12px;"); // CSS: .auto-unlike-toggle
        autoUnlikeToggle.setSelected(false);
        autoUnlikeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            controller.setAutoUnlike(newVal);
            System.out.println("[Auto-Unlike] " + (newVal ? "Activé" : "Désactivé"));
        });

        Label infoIcon = new Label("ℹ️");
        infoIcon.setStyle("-fx-font-size: 14px; -fx-cursor: hand;"); // CSS: .info-icon
        Tooltip tooltip = new Tooltip("Si activé, la musique sera retirée des titres likés après l’avoir ajoutée à une playlist.");
        Tooltip.install(infoIcon, tooltip);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBox = new HBox(10, userInfoLabel, spacer, autoUnlikeToggle, infoIcon);
        topBox.setPadding(new Insets(10));
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.getStyleClass().add("top-header");
        return topBox;
    }

    private void updateUserInfoLabel() {
        userInfoLabel.setText("👋 Salut " + controller.getUserInfo().getDisplayName() +
                " ! Tu as " + controller.getLikedTracks().size() +
                " musiques likées et " + controller.getPlaylists().size() + " playlists.");
    }

    private Node buildLeftPlaylists() {
        leftPlaylistsBox = new VBox(10);
        leftPlaylistsBox.setPadding(new Insets(10));
        leftPlaylistsBox.setAlignment(Pos.CENTER);
        leftPlaylistsBox.getStyleClass().add("playlist-container");
        VBox.setVgrow(leftPlaylistsBox, Priority.ALWAYS);
        return leftPlaylistsBox;
    }

    private Node buildRightPlaylists() {
        rightPlaylistsBox = new VBox(10);
        rightPlaylistsBox.setPadding(new Insets(10));
        rightPlaylistsBox.setAlignment(Pos.CENTER);
        rightPlaylistsBox.getStyleClass().add("playlist-container");
        VBox.setVgrow(rightPlaylistsBox, Priority.ALWAYS);
        return rightPlaylistsBox;
    }

    private Node buildPlaylistCreator() {
        VBox bottomContainer = new VBox(10);
        bottomContainer.setAlignment(Pos.CENTER_LEFT);
        bottomContainer.setPadding(new Insets(10));

        Button toggleFormButton = new Button("+ Créer une playlist");
        toggleFormButton.getStyleClass().add("create-playlist-button");

        HBox formBox = new HBox(10);
        formBox.setId("playlist-form");
        formBox.setVisible(false);
        formBox.setManaged(false);

        TextField titleField = new TextField();
        titleField.getStyleClass().add("text-field");
        titleField.setPromptText("Nom");
        TextField descriptionField = new TextField();
        descriptionField.getStyleClass().add("text-field");
        descriptionField.setPromptText("Description");
        CheckBox publicBox = new CheckBox("Publique");
        publicBox.getStyleClass().add("check-box");
        Button createButton = new Button("Créer");
        createButton.getStyleClass().add("submit-playlist-button");

        createButton.setOnAction(e -> {
            if (!titleField.getText().isEmpty()) {
                controller.createNewPlaylist(titleField.getText(), descriptionField.getText(), publicBox.isSelected());
                titleField.clear();
                descriptionField.clear();
                publicBox.setSelected(false);
                updatePlaylistsUI();
                updateUserInfoLabel();
            }
        });

        toggleFormButton.setOnAction(e -> {
            boolean visible = !formBox.isVisible();
            formBox.setVisible(visible);
            formBox.setManaged(visible);
            toggleFormButton.setText(visible ? "Retour" : "+ Créer une playlist");
        });

        formBox.getChildren().addAll(titleField, descriptionField, publicBox, createButton);
        bottomContainer.getChildren().addAll(toggleFormButton, formBox);

        return bottomContainer;
    }

    private void updatePlaylistsUI() {
        leftPlaylistsBox.getChildren().clear();
        rightPlaylistsBox.getChildren().clear();

        List<PlaylistInfo> playlists = controller.getPlaylists().reversed();
        for (int i = 0; i < playlists.size(); i++) {
            VBox card = createPlaylistCard(playlists.get(i));
            if (i % 2 == 0) {
                leftPlaylistsBox.getChildren().add(card);
            } else {
                rightPlaylistsBox.getChildren().add(card);
            }
        }
    }

    private VBox createPlaylistCard(PlaylistInfo playlist) {
        VBox cardBox = new VBox(5);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setPrefWidth(150);
        cardBox.setMinHeight(145);
        cardBox.setStyle("-fx-border-color: #ccc; -fx-background-color: white; -fx-border-radius: 10; -fx-padding: 10;");
        cardBox.getStyleClass().add("playlist-card");

        cardBox.setOnDragOver(e -> {
            if (e.getGestureSource() != cardBox && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                cardBox.setScaleX(0.96);
                cardBox.setScaleY(0.96);
            }
            e.consume();
        });

        cardBox.setOnDragExited(e -> {
            cardBox.setScaleX(1.0);
            cardBox.setScaleY(1.0);
        });

        cardBox.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                String trackId = db.getString();

                controller.addTrackToPlaylist(trackId, playlist.getId());

                if (controller.isAutoUnlikeEnabled()) {
                    controller.unlikeTrack(trackId);
                    controller.nextTrack();
                } else {
                    controller.nextTrack();
                }

                updateUserInfoLabel();
                updateTrackCard();
                success = true;
            }

            e.setDropCompleted(success);
            e.consume();
        });

        ImageView imageView = new ImageView(
                playlist.getImageUrl() != null && !playlist.getImageUrl().isEmpty()
                        ? new Image(playlist.getImageUrl(), 90, 90, true, true)
                        : new Image(getClass().getResourceAsStream("/images/default_playlist.png"), 90, 90, true, true)
        );
        imageView.setPreserveRatio(false);

        Button deleteButton = new Button("✖");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 16px;");
        deleteButton.setOnMouseEntered(e -> {
            deleteButton.setScaleX(1.4);
            deleteButton.setScaleY(1.4);
        });
        deleteButton.setOnMouseExited(e -> {
            deleteButton.setScaleX(1.0);
            deleteButton.setScaleY(1.0);
        });
        deleteButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Supprimer playlist");
            confirm.setHeaderText("Supprimer \"" + playlist.getName() + "\" ?");
            confirm.setContentText("Tu es sûr ?");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Alert askAdd = new Alert(Alert.AlertType.CONFIRMATION);
                    askAdd.setTitle("Ajouter les titres ?");
                    askAdd.setContentText("Ajouter les titres de cette playlist à tes musiques likées ?");
                    askAdd.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

                    askAdd.showAndWait().ifPresent(choice -> {
                        if (choice == ButtonType.YES) {
                            controller.transferPlaylistToLiked(playlist.getId());
                        }
                        if (choice != ButtonType.CANCEL) {
                            controller.deletePlaylist(playlist.getId());
                            updatePlaylistsUI();
                            updateTrackCard();
                            updateUserInfoLabel();
                        }
                    });
                }
            });
        });

        StackPane imageContainer = new StackPane(imageView, deleteButton);
        StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);

        Label label = new Label(playlist.getName());
        label.setWrapText(true);
        label.setMaxWidth(100);
        label.setStyle("-fx-font-size: 12px;");
        label.getStyleClass().add("playlist-label");

        cardBox.getChildren().addAll(imageContainer, label);
        return cardBox;
    }

    private Node buildTrackCardWithNavigation() {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(10);
        container.setPadding(new Insets(10));

        Button prev = new Button("⬅");
        prev.setPrefSize(40, 40);
        prev.getStyleClass().add("nav-button");
        prev.setOnAction(e -> {
            controller.previousTrack();
            updateTrackCard();
        });

        Button next = new Button("➡");
        next.setPrefSize(40, 40);
        next.getStyleClass().add("nav-button");
        next.setOnAction(e -> {
            controller.nextTrack();
            updateTrackCard();
        });

        trackCardBox = new VBox();
        trackCardBox.setAlignment(Pos.CENTER);
        trackCardBox.setFillWidth(true);
        trackCardBox.setMinWidth(100);
        // On laisse le CSS gérer la largeur via .track-card
        trackCardBox.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(trackCardBox, Priority.ALWAYS);

        container.getChildren().addAll(prev, trackCardBox, next);
        return container;
    }

    private void updateTrackCard() {
        trackCardBox.getChildren().clear();

        if (controller.getLikedTracks().isEmpty()) {
            trackCardBox.getChildren().add(new Label("Aucune musique likée."));
            return;
        }

        TrackInfo track = controller.getCurrentTrack();
        ImageView cover = new ImageView(new Image(track.getImageUrl(), 120, 120, true, true));
        cover.setPreserveRatio(true);

        Button unlikeBtn = new Button("✖");
        unlikeBtn.getStyleClass().add("unlike-button");
        unlikeBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Supprimer musique");
            alert.setHeaderText("Supprimer \"" + track.getTitle() + "\" de tes titres likés ?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    controller.unlikeTrack(track.getTrackId());
                    updateUserInfoLabel();
                    updateTrackCard();
                }
            });
        });

        StackPane imageContainer = new StackPane(cover, unlikeBtn);
        StackPane.setAlignment(unlikeBtn, Pos.TOP_RIGHT);

        Label title = new Label(track.getTitle());
        title.getStyleClass().add("track-title");
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        Label artist = new Label(track.getArtist());
        artist.getStyleClass().add("track-artist");
        artist.setWrapText(true);
        artist.setAlignment(Pos.CENTER);
        artist.setMaxWidth(Double.MAX_VALUE);

        Label album = new Label("Album : " + track.getAlbum().getName());
        album.getStyleClass().add("track-album");
        album.setAlignment(Pos.CENTER);
        album.setMaxWidth(Double.MAX_VALUE);

        Label duration = new Label(
            "Durée : " + String.format("%d:%02d",
                track.getDuree() / 60000,
                (track.getDuree() % 60000) / 1000
            ) + " min"
        );
        duration.getStyleClass().add("track-duration");
        duration.setAlignment(Pos.CENTER);
        duration.setMaxWidth(Double.MAX_VALUE);

        Button playBtn = new Button(track.getPreviewUrl() == null ? "🔇 Pas d'extrait" : "▶️ Play 5s");
        playBtn.setDisable(track.getPreviewUrl() == null);
        playBtn.getStyleClass().add("play-button");

        VBox card = new VBox(10, imageContainer, title, artist, album, duration, playBtn);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("track-card"); // Taille, max-width/height et style gérés par CSS

        card.setOnDragDetected(e -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(track.getTrackId());
            db.setContent(content);
            e.consume();
        });

        trackCardBox.setMinHeight(Region.USE_COMPUTED_SIZE);
        trackCardBox.setMinWidth(Region.USE_COMPUTED_SIZE);
        trackCardBox.getChildren().add(card);
    }


    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.getStyleClass().add("root-pane");

        root.setTop(buildTopHeader());
        root.setLeft(buildLeftPlaylists());
        root.setRight(buildRightPlaylists());
        root.setCenter(buildTrackCardWithNavigation());
        root.setBottom(buildPlaylistCreator());

        updatePlaylistsUI();

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.svg")));
        primaryStage.setTitle("Sortify 🎶");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        primaryStage.show();

        scene.widthProperty().addListener((obs, oldVal, newVal) -> updateTrackCard());
        scene.heightProperty().addListener((obs, oldVal, newVal) -> updateTrackCard());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
