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

    private int currentTrackIndex = 0;
    private VBox trackCardBox;
    
    public static void setController(TinderController c) {
        controller = c;
    }

    

    private Node buildTopHeader() {
        userInfoLabel = new Label();
        userInfoLabel.setStyle("-fx-font-size: 16px;");
        updateUserInfoLabel();

        ToggleButton autoUnlikeToggle = new ToggleButton("Auto-Unlike 🚫❤️");
        // Icône d'info à côté
        Label infoIcon = new Label("ℹ️");
        infoIcon.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");

        // Tooltip explicatif
        Tooltip infoTooltip = new Tooltip("Si activé, la musique sera retirée des titres likés après l’avoir ajoutée à une playlist.");
        Tooltip.install(infoIcon, infoTooltip);

        autoUnlikeToggle.setStyle("-fx-font-size: 12px;");
        autoUnlikeToggle.setSelected(false);

        autoUnlikeToggle.setOnAction(e -> {
            controller.setAutoUnlike(autoUnlikeToggle.isSelected());
            System.out.println("[Auto-Unlike] " + (autoUnlikeToggle.isSelected() ? "Activé" : "Désactivé"));
        });

        Region spacer = new Region(); // pour pousser à droite
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBox = new HBox(10, userInfoLabel, spacer, autoUnlikeToggle,infoIcon);
        topBox.setPadding(new Insets(10));
        topBox.setAlignment(Pos.CENTER_LEFT);

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
        VBox.setVgrow(leftPlaylistsBox, Priority.ALWAYS); 
        return leftPlaylistsBox;
    }

    private Node buildRightPlaylists() {
        rightPlaylistsBox = new VBox(10);
        rightPlaylistsBox.setPadding(new Insets(10));
        rightPlaylistsBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(rightPlaylistsBox, Priority.ALWAYS);
        return rightPlaylistsBox;
    }

    private Node buildPlaylistCreator() {
        VBox bottomContainer = new VBox(10);
        bottomContainer.setAlignment(Pos.CENTER_LEFT);
        bottomContainer.setPadding(new Insets(10));

        Button toggleFormButton = new Button("+ Créer une playlist");
        toggleFormButton.setStyle("-fx-font-size: 14px;");

        HBox formBox = new HBox(10);
        formBox.setAlignment(Pos.CENTER_LEFT);
        formBox.setVisible(false);
        formBox.setManaged(false);

        TextField titleField = new TextField();
        titleField.setPromptText("Nom de la playlist");
        titleField.setPrefWidth(200);

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        descriptionField.setPrefWidth(250);

        CheckBox publicCheckBox = new CheckBox("Publique");

        Button createButton = new Button("Créer");

        createButton.setOnAction(e -> {
            String title = titleField.getText();
            String description = descriptionField.getText();
            boolean isPublic = publicCheckBox.isSelected();

            if (!title.isEmpty()) {
                controller.createNewPlaylist(title, description, isPublic);
                titleField.clear();
                descriptionField.clear();
                publicCheckBox.setSelected(false);
                updatePlaylistsUI();
                updateUserInfoLabel();
            } else {
                System.out.println("❌ Titre obligatoire.");
            }
        });

        formBox.getChildren().addAll(titleField, descriptionField, publicCheckBox, createButton);

        toggleFormButton.setOnAction(e -> {
            boolean visible = formBox.isVisible();
            formBox.setVisible(!visible);
            formBox.setManaged(!visible);
            toggleFormButton.setText(visible ? "+ Créer une playlist" : "Retour");
        });

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
        cardBox.setMaxHeight(145);
        cardBox.setStyle(
            "-fx-border-color: #ccc; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-background-color: white; " +
            "-fx-padding: 10;"
        );
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
                    controller.unlikeTrack(trackId);             // API Spotify
                    controller.removeTrackFromLiked(trackId);    // Mémoire locale
                    updateUserInfoLabel();                       // Rafraîchit l’UI
                    updateTrackCard();                           // Met à jour la carte
                }

                success = true;
            }

            e.setDropCompleted(success);
            e.consume();
        });

        ImageView imageView;
        if (playlist.getImageUrl() != null && !playlist.getImageUrl().isEmpty()) {
            imageView = new ImageView(new Image(playlist.getImageUrl(), 90, 90, true, true));
        } else {
            imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/default_playlist.png"), 80, 80, true, true));
        }
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(false);

        Button deleteButton = new Button("✖");
        deleteButton.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: red;
            -fx-font-size: 16px;
            -fx-cursor: hand;
            -fx-padding: 0;
        """);
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
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Suppression de playlist");
            confirm.setContentText("Êtes-vous sûr de vouloir supprimer \"" + playlist.getName() + "\" ?");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    controller.deletePlaylist(playlist.getId());
                    updatePlaylistsUI();
                    updateUserInfoLabel();
                }
            });
        });

        StackPane imageContainer = new StackPane();
        StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);
        imageContainer.getChildren().addAll(imageView, deleteButton);

        Label label = new Label(playlist.getName());
        label.setMaxWidth(100);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-font-size: 12px; -fx-text-alignment: center;");

        cardBox.getChildren().addAll(imageContainer, label);
        return cardBox;
    }
    
    private Node buildTrackCardWithNavigation() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("⬅");
        prevBtn.setOnAction(e -> {
            int size = controller.getLikedTracks().size();
            if (size > 0) {
                currentTrackIndex = (currentTrackIndex - 1 + size) % size;
                updateTrackCard();
            }
        });

        Button nextBtn = new Button("➡");
        nextBtn.setOnAction(e -> {
            int size = controller.getLikedTracks().size();
            if (size > 0) {
                currentTrackIndex = (currentTrackIndex + 1) % size;
                updateTrackCard();
            }
        });


        trackCardBox = new VBox(); // initialisée ici
        trackCardBox.setAlignment(Pos.CENTER);
        updateTrackCard(); // 👈 on affiche le premier morceau

        container.getChildren().addAll(prevBtn, trackCardBox, nextBtn);
        return container;
    }
    
    private void updateTrackCard() {
        trackCardBox.getChildren().clear();

        if (controller.getLikedTracks().isEmpty()) {
            trackCardBox.getChildren().add(new Label("Aucune musique likée."));
            return;
        }

        TrackInfo track = controller.getLikedTracks().get(currentTrackIndex);

        // === Image de l'album ===
        ImageView cover = new ImageView(new Image(track.getImageUrl(), 250, 250, true, true));
        cover.setPreserveRatio(true);

        // === Bouton unlike (croix rouge) ===
        Button unlikeBtn = new Button("✖");
        unlikeBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: red;
            -fx-font-size: 20px;
            -fx-cursor: hand;
            -fx-padding: 0;
            
        """);

        unlikeBtn.setOnMouseEntered(e -> {
            unlikeBtn.setScaleX(1.4);
            unlikeBtn.setScaleY(1.4);
        });
        unlikeBtn.setOnMouseExited(e -> {
            unlikeBtn.setScaleX(1.0);
            unlikeBtn.setScaleY(1.0);
        });

        unlikeBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer cette musique likée ?");
            confirm.setContentText("Tu vas retirer \"" + track.getTitle() + "\" de tes titres likés.");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    controller.unlikeTrack(track.getTrackId());
                    updateUserInfoLabel();
                    updateTrackCard(); // passe au morceau suivant
                }
            });
        });

        StackPane imageContainer = new StackPane(cover, unlikeBtn);
        StackPane.setAlignment(unlikeBtn, Pos.TOP_RIGHT);

     // === Infos texte ===
        Label title = new Label(track.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label artists = new Label(track.getArtist());
        artists.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        // Album
        Label album = new Label("Album : " + track.getAlbum().getName());
        album.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        // Durée en mm:ss
        int durationMs = track.getDuree();
        int minutes = durationMs / 60000;
        int seconds = (durationMs % 60000) / 1000;
        String durationFormatted = String.format("%d:%02d", minutes, seconds);

        Label durationLabel = new Label("Durée : " + durationFormatted + " min");
        durationLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        // === Bouton play
        Button playBtn = new Button("▶️ Play 5s");
        if (track.getPreviewUrl() == null) {
            playBtn.setDisable(true);
            playBtn.setText("🔇 Pas d'extrait");
        }

        // === Bloc carte complet
        VBox box = new VBox(10, imageContainer, title, artists, album, durationLabel, playBtn);

        box.setPadding(new Insets(20));
        box.setStyle("-fx-border-color: #ccc; -fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: white;");
        box.setAlignment(Pos.CENTER);

        // === Drag & Drop
        box.setOnDragDetected(e -> {
            Dragboard db = box.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(track.getTrackId());
            db.setContent(content);
            e.consume();
        });

        box.setOnDragDone(e -> {
            System.out.println("🎵 Drag terminé");
            e.consume();
        });

        trackCardBox.getChildren().add(box);
    }



    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        root.setTop(buildTopHeader());
        root.setLeft(buildLeftPlaylists());
        root.setRight(buildRightPlaylists());
        root.setCenter(buildTrackCardWithNavigation());
        root.setBottom(buildPlaylistCreator());

        // Couleur de fond
        root.setStyle("-fx-background-color: #75bc7f;");

        updatePlaylistsUI(); // Affiche les playlists au démarrage

        Scene scene = new Scene(root, 1000, 600);

        // Icône (assure-toi que le SVG est bien dans /resources/images/logoo.svg)
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.svg")));

        primaryStage.setTitle("Sortify 🎶");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    
    
    public static void main(String[] args) {
        launch(args);
    }
}
