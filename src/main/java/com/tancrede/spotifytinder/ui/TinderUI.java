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
import java.util.Objects;

/**
 * Classe principale de l'UI JavaFX. Toutes les configurations CSS sont externalisées,
 * les composants sont factorisés, et les styles en dur supprimés.
 */
public class TinderUI extends Application {

    private static TinderController controller;
    private static final Insets STANDARD_PADDING = new Insets(10);
    private VBox leftPlaylistsBox;
    private VBox rightPlaylistsBox;
    private Label userInfoLabel;
    private VBox trackCardBox;

    /**
     * Setter statique pour injection depuis Main.java ou autre bootstrap.
     */
    public static void setController(TinderController c) {
        controller = Objects.requireNonNull(c, "Controller ne peut être null");
    }

    @Override
    public void start(Stage primaryStage) {
        if (controller == null) {
            throw new IllegalStateException("TinderController non initialisé: appeler TinderUI.setController(...) avant le lancement");
        }

        BorderPane root = new BorderPane();
        root.setPadding(STANDARD_PADDING);
        root.getStyleClass().add("root-pane");

        // construction des zones
        root.setTop(buildTopHeader());
        root.setLeft(buildPlaylistsColumn(true));
        root.setRight(buildPlaylistsColumn(false));
        root.setCenter(buildTrackCardWithNavigation());
        root.setBottom(buildPlaylistCreator());

        // affichage initial de la carte de track
        updatePlaylistsUI();
        updateUserInfoLabel();
        updateTrackCard();

        // initialisation UI
        updatePlaylistsUI();
        updateUserInfoLabel();

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setTitle("Sortify ");
        primaryStage.getIcons().add(new Image(
            Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.svg"))));
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        primaryStage.show();

        // réagir au redimensionnement pour recalcule du track card
        scene.widthProperty().addListener((obs, o, n) -> updateTrackCard());
        scene.heightProperty().addListener((obs, o, n) -> updateTrackCard());
    }

    /** En-tête haut : info utilisateur + toggle auto-unlike */
    private Node buildTopHeader() {
        userInfoLabel = new Label();
        userInfoLabel.getStyleClass().add("user-info-label");

        ToggleButton autoUnlikeToggle = new ToggleButton("Auto-Unlike ");
        autoUnlikeToggle.getStyleClass().add("auto-unlike-toggle");
        autoUnlikeToggle.setSelected(false);
        autoUnlikeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            controller.setAutoUnlike(newVal);
            System.out.println("[Auto-Unlike] " + (newVal ? "Activé" : "Désactivé"));
        });

        Label infoIcon = new Label("i");
        infoIcon.getStyleClass().add("info-icon");
        Tooltip.install(infoIcon, new Tooltip(
            "Si activé, la musique sera retirée des titres likées après l’avoir ajoutée à une playlist."));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBox = new HBox(10, userInfoLabel, spacer, autoUnlikeToggle, infoIcon);
        topBox.setPadding(STANDARD_PADDING);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.getStyleClass().add("top-header");

        return topBox;
    }

    private void updateUserInfoLabel() {
        userInfoLabel.setText(String.format("👋 Salut %s ! Tu as %d musiques likées et %d playlists.",
            controller.getUserInfo().getDisplayName(),
            controller.getLikedTracks().size(),
            controller.getPlaylists().size()));
    }

    /** Crée la colonne de playlists (gauche ou droite selon left=true). */
    private Node buildPlaylistsColumn(boolean left) {
        VBox box = new VBox(10);
        box.setPadding(STANDARD_PADDING);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("playlist-column");
        if (left) this.leftPlaylistsBox = box;
        else this.rightPlaylistsBox = box;
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }

    /** Zone de création de playlist en bas. */
    private Node buildPlaylistCreator() {
        VBox bottom = new VBox(10);
        bottom.setPadding(STANDARD_PADDING);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.getStyleClass().add("playlist-creator");

        Button toggleForm = new Button("+ Créer une playlist");
        toggleForm.getStyleClass().add("create-playlist-button");

        HBox form = new HBox(10);
        form.setId("playlist-form");
        form.setVisible(false);
        form.setManaged(false);

        TextField titleField = new TextField();
        titleField.setPromptText("Nom");
        titleField.getStyleClass().add("text-field");
        TextField descField = new TextField();
        descField.setPromptText("Description");
        descField.getStyleClass().add("text-field");
        CheckBox publicBox = new CheckBox("Publique");
        publicBox.getStyleClass().add("check-box");
        Button createBtn = new Button("Créer");
        createBtn.getStyleClass().add("submit-playlist-button");

        createBtn.setOnAction(e -> {
            if (!titleField.getText().isEmpty()) {
                controller.createNewPlaylist(
                    titleField.getText(),
                    descField.getText(),
                    publicBox.isSelected()
                );
                titleField.clear();
                descField.clear();
                publicBox.setSelected(false);
                updatePlaylistsUI();
                updateUserInfoLabel();
            }
        });

        toggleForm.setOnAction(e -> {
            boolean show = !form.isVisible();
            form.setVisible(show);
            form.setManaged(show);
            toggleForm.setText(show ? "Retour" : "+ Créer une playlist");
        });

        form.getChildren().addAll(titleField, descField, publicBox, createBtn);
        bottom.getChildren().addAll(toggleForm, form);
        return bottom;
    }

    /** Répartit les playlists gauche/droite. */
    private void updatePlaylistsUI() {
        leftPlaylistsBox.getChildren().clear();
        rightPlaylistsBox.getChildren().clear();

        List<PlaylistInfo> list = controller.getPlaylists().reversed();
        for (int i = 0; i < list.size(); i++) {
            PlaylistCard card = new PlaylistCard(list.get(i));
            if (i % 2 == 0) leftPlaylistsBox.getChildren().add(card);
            else rightPlaylistsBox.getChildren().add(card);
        }
    }

    /** Carte de playlist personnalisée. */
    private class PlaylistCard extends VBox {
        PlaylistCard(PlaylistInfo p) {
            super(5);
            getStyleClass().add("playlist-card");
            setAlignment(Pos.CENTER);
            setPadding(STANDARD_PADDING);

            ImageView iv = new ImageView(
                p.getImageUrl() != null && !p.getImageUrl().isEmpty()
                    ? new Image(p.getImageUrl(), 90, 90, true, true)
                    : new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                        "/images/default_playlist.png")), 90, 90, true, true)
            );
            iv.setPreserveRatio(true);

            Button del = createIconButton("✖", "delete-playlist-button");
            StackPane.setMargin(del, new Insets(-8));
            del.setOnAction(e -> confirmDelete(p));

            StackPane imgCont = new StackPane(iv, del);
            StackPane.setAlignment(del, Pos.TOP_RIGHT);
            imgCont.getStyleClass().add("playlist-img-container");

            Label name = new Label(p.getName());
            name.getStyleClass().add("playlist-label");
            name.setWrapText(true);

            initDragDrop(this, p.getId());

            getChildren().addAll(imgCont, name);
        }
    }

    /** Zone centrale : track + navigation. */
    private Node buildTrackCardWithNavigation() {
        HBox container = new HBox(10);
        container.setPadding(STANDARD_PADDING);
        container.setAlignment(Pos.CENTER);

        Button prev = createNavButton("⬅");
        prev.setOnAction(e -> { controller.previousTrack(); updateTrackCard(); });
        Button next = createNavButton("➡");
        next.setOnAction(e -> { controller.nextTrack(); updateTrackCard(); });

        trackCardBox = new VBox();
        trackCardBox.getStyleClass().add("track-card-container");
        trackCardBox.setAlignment(Pos.CENTER);
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

        TrackInfo t = controller.getCurrentTrack();
        
        ImageView cover = new ImageView(new Image(t.getImageUrl()));
        cover.getStyleClass().add("track-image");
        cover.setPreserveRatio(true);
        cover.setFitWidth(150);   // <-- ici, la largeur
        cover.setFitHeight(150);  // <-- ici, la hauteur

        

        Button unlike = createIconButton("✖", "unlike-button");
        StackPane.setMargin(unlike, new Insets(-20));
        unlike.setOnAction(e -> confirmUnlike(t));

        StackPane imgCont = new StackPane(cover, unlike);
        StackPane.setAlignment(unlike, Pos.TOP_RIGHT);
        imgCont.getStyleClass().add("track-img-container");

        Label title = new Label(t.getTitle()); title.getStyleClass().add("track-title");
        Label artist = new Label(t.getArtist()); artist.getStyleClass().add("track-artist");
        Label album = new Label("Album : " + t.getAlbum().getName()); album.getStyleClass().add("track-album");
        Label duration = new Label(String.format("Durée : %d:%02d min", t.getDuree()/60000, (t.getDuree()%60000)/1000));
        duration.getStyleClass().add("track-duration");

        Button play = new Button(t.getPreviewUrl() == null ? "🔇 Pas d'extrait" : "▶️ Play 5s");
        play.getStyleClass().add("play-button");
        play.setDisable(t.getPreviewUrl() == null);

        VBox card = new VBox(10, imgCont, title, artist, album, duration, play);
        card.getStyleClass().add("track-card");
        card.setPadding(STANDARD_PADDING);
        initDragDrop(card, t.getTrackId());

        trackCardBox.getChildren().add(card);
    }

    /* Helpers génériques */
    private Button createNavButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-button");
        b.setPrefSize(40, 40);
        return b;
    }

    private Button createIconButton(String icon, String styleClass) {
        Button b = new Button(icon);
        b.getStyleClass().add(styleClass);
        b.setPickOnBounds(true);
        return b;
    }

    private void initDragDrop(Node node, String id) {
        node.setOnDragDetected(e -> {
            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(id);
            db.setContent(content);
            e.consume();
        });
        node.setOnDragOver(e -> {
            if (e.getGestureSource() != node && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                node.getStyleClass().add("drag-over");
            }
            e.consume();
        });
        node.setOnDragExited(e -> node.getStyleClass().remove("drag-over"));
        node.setOnDragDropped(e -> {
            String trackId = e.getDragboard().getString();
            if (trackId != null) {
                controller.addTrackToPlaylist(trackId, id);
                if (controller.isAutoUnlikeEnabled()) {
                    controller.unlikeTrack(trackId);
                    controller.nextTrack();
                } else {
                    controller.nextTrack();
                }
                updateUserInfoLabel();
                updateTrackCard();
                e.setDropCompleted(true);
            }
            e.consume();
        });
    }

    private void confirmDelete(PlaylistInfo p) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer '" + p.getName() + "'?", ButtonType.OK, ButtonType.CANCEL);
        c.setHeaderText(null);
        c.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                Alert ask = new Alert(Alert.AlertType.CONFIRMATION,
                    "Ajouter les titres de cette playlist à tes musiques likées?", ButtonType.YES, ButtonType.NO);
                ask.showAndWait().ifPresent(ch -> {
                    if (ch == ButtonType.YES) controller.transferPlaylistToLiked(p.getId());
                    if (ch != ButtonType.CANCEL) {
                        controller.deletePlaylist(p.getId());
                        updatePlaylistsUI(); updateTrackCard(); updateUserInfoLabel();
                    }
                });
            }
        });
    }

    private void confirmUnlike(TrackInfo t) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer '" + t.getTitle() + "' de tes titres likés?", ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                controller.unlikeTrack(t.getTrackId());
                updateUserInfoLabel(); updateTrackCard();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
