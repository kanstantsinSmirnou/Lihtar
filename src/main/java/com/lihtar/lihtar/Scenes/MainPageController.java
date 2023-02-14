package com.lihtar.lihtar.Scenes;

import com.lihtar.lihtar.Data.User;
import com.lihtar.lihtar.StartApplication;
import com.lihtar.lihtar.StartApplicationController;
import com.lihtar.lihtar.Utills.LoadXML;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

import static com.lihtar.lihtar.Data.Database.*;
import static com.lihtar.lihtar.Data.User.UserID;
import static com.lihtar.lihtar.Scenes.MainPageController.currentPlaylist.*;
import static com.lihtar.lihtar.StartApplication.primaryStage;
import static com.lihtar.lihtar.StartApplication.setScene;
import static java.lang.Double.NaN;
import static java.lang.Math.abs;

public class MainPageController {
    @FXML
    private Button artistName;
    @FXML
    private Button songName;
    @FXML
    private Button playButton;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Slider songProgressSlider;
    public static MediaPlayer player;
    public static Media currentSong;
    @FXML
    private ImageView songImage;

    public static Button followButton;
    public static int currentArtistID, lastSongLayout;
    public static int currentSongID = -1;
    public static Label followersCount;
    @FXML
    private Label currentTime;
    @FXML
    private Label songDuration;
    @FXML
    private VBox centralVBox;

    @FXML
    private TextField findTextField;
    @FXML
    private Pane mainPain;

    boolean stop = true;
    private Timer timer;
    private TimerTask task;
    private boolean running;
    public static ChoiceBox< String > pModesChoiceBox = new ChoiceBox<>();

    public static int loadAnotherArtist = -1;
    public int getLastListen() throws Exception {
        ArrayList < Integer > songsID = getLikedSongs(UserID);
        if (songsID.size() == 0) {
            currentPlaylist.songsID = new ArrayList<>();
            currentPlaylist.songsID.add(1);
            playingSongNumber = 0;
            return 1;
        }
        currentPlaylist.songsID = getLikedSongs(UserID);
        playingSongNumber = 0;
        return currentPlaylist.songsID.get(0);
    }

    public void initialize() throws Exception {
        animTimer.start();
        pModesChoiceBox.setLayoutX(510);
        pModesChoiceBox.setLayoutY(409);
        pModesChoiceBox.setMinWidth(100);
        pModesChoiceBox.setMaxWidth(100);
        mainPain.getChildren().add(pModesChoiceBox);
        loadSong(getLastListen());
        pModesChoiceBox.getItems().add("Randomly");
        pModesChoiceBox.getItems().add("In order");
        pModesChoiceBox.getItems().add("Repeat");
        pModesChoiceBox.setValue("In order");
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
                player.setVolume(volumeSlider.getValue() * 0.01);
            }
        });
        volumeSlider.setValue(50); //placeholder, change to last volume count
    }

    public void loadSong(int songID) throws Exception {
        if (lastPlayed.size() == 0 || lastPlayed.get(lastPlayed.size() - 1) != songID) {
            lastPlayed.add(songID);
        }
        currentSongID = songID;
        currentArtistID = getArtistID(getAlbumID(songID));
        songName.setText(getSongName(songID));
        artistName.setText(getArtistName(currentArtistID));
        Image albumImage = new ImageView(new File("src/main/resources/com/lihtar/lihtar/Images/AlbumsImages/" + getAlbumID(songID) + "albumImage.png").toURI().toString()).getImage();
        songImage.setImage(albumImage);
        currentSong = new Media(new File("src/main/resources/com/lihtar/lihtar/Music/" + songID + ".mp3").toURI().toString());
        player = new MediaPlayer(currentSong);
        player.setOnReady(new Runnable() {
            @Override
            public void run() {
                player.setOnEndOfMedia(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            playNextSong();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                songDuration.setText(parseTime(currentSong.getDuration().toSeconds()));
                songProgressSlider.valueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                        if (abs((double)t1 - (double)number) > 1.0) {
                            Duration newTime = new Duration(songProgressSlider.getValue() * 10.0 * currentSong.getDuration().toSeconds());
                            player.seek(newTime);
                        }
                    }
                });
            }
        });
    }

    AnimationTimer animTimer = new AnimationTimer() {
        @Override
        public void handle(long l) {
            if (songDuration.getText() != "00:00") {
                currentTime.setText(parseTime(player.getCurrentTime().toSeconds()));
            }
        }
    };

    public String parseTime(double t) {
        Integer s = (int)(t);
        Integer m = (int)(t / 60);
        s -= m * 60;
        return (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
    }

    public void play() {
        if (stop) {
            beginTimer();
            playButton.setText("STOP");
            player.play();
            stop = false;
        } else {
            cancelTimer();
            playButton.setText("PLAY");
            player.pause();
            stop = true;
        }
    }
    public void beginTimer() {
        timer = new Timer();
        task = new TimerTask() {
            public void run() {
                running = true;
                double current = player.getCurrentTime().toSeconds();
                double end = currentSong.getDuration().toSeconds();
                songProgressSlider.setValue((current / end) * 100);
                if(current / end == 1) {
                    cancelTimer();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    public void cancelTimer() {
        running = false;
        timer.cancel();
    }

    class follow implements EventHandler<ActionEvent> {
        private int artistID;
        follow(int artistID) {
            this.artistID = artistID;
        }
        @Override
        public void handle(ActionEvent actionEvent) {
            if (Objects.equals(followButton.getText(), "Follow")) {
                followButton.setText("Unfollow");
                try {
                    followArtist(UserID, artistID);
                    followersCount.setText(getFollowersCount(artistID, true));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                followButton.setText("Follow");
                try {
                    unfollowArtist(UserID, artistID);
                    followersCount.setText(getFollowersCount(artistID, true));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class likeSongAction implements EventHandler<ActionEvent> {
        private int songID;
        private Button likeSong;
        likeSongAction(int songID, Button likeSong) {
            this.songID = songID;
            this.likeSong = likeSong;
        }
        @Override
        public void handle(ActionEvent actionEvent) {
            if (Objects.equals(likeSong.getText(), "Liked")) {
                likeSong.setText("Like");
                try {
                    deleteSongFromLiked(UserID, songID);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                likeSong.setText("Liked");
                try {
                    likeSong(UserID, songID);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class likeAlbumAction implements EventHandler<ActionEvent> {
        private int albumID;
        private Button likeAlbumButton;
        likeAlbumAction(int albumID, Button likeAlbumButton) {
            this.albumID = albumID;
            this.likeAlbumButton = likeAlbumButton;
        }
        @Override
        public void handle(ActionEvent actionEvent) {
            if (Objects.equals(likeAlbumButton.getText(), "Delete playlist")) {
                likeAlbumButton.setText("Add playlist");
                try {
                    deleteAlbumFromLiked(UserID, albumID);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else  if (Objects.equals(likeAlbumButton.getText(), "Add playlist")) {
                likeAlbumButton.setText("Delete playlist");
                try {
                    likeAlbum(UserID, albumID);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (Objects.equals(likeAlbumButton.getText(), "Liked album")) {
                likeAlbumButton.setText("Like album");
                try {
                    deleteAlbumFromLiked(UserID, albumID);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                likeAlbumButton.setText("Liked album");
                try {
                    likeAlbum(UserID, albumID);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public void loadArtistPane(int artistID) throws Exception {
        Pane artistPane = new Pane();
        artistPane.setMinWidth(452);
        artistPane.setMaxWidth(452);
        artistPane.setMinHeight(200);
        artistPane.setMaxHeight(200);

        ImageView artistAvatar = new ImageView(new File("src/main/resources/com/lihtar/lihtar/Images/ArtistsAvatars/" + artistID + "artistAvatar.png").toURI().toString());
        artistAvatar.setLayoutX(5);
        artistAvatar.setLayoutY(5);
        artistAvatar.setFitHeight(150);
        artistAvatar.setFitWidth(150);
        artistPane.getChildren().add(artistAvatar);

        Label artistName = new Label(getArtistName(artistID));
        artistName.setLayoutX(200);
        artistName.setLayoutY(5);
        artistName.setFont(new Font("Arial", 40));
        artistPane.getChildren().add(artistName);

        followButton = new Button();
        followButton.setLayoutX(10);
        followButton.setLayoutY(160);
        if (isFollow(UserID, artistID)) {
            followButton.setText("Unfollow");
        } else {
            followButton.setText("Follow");
        }
        followButton.setOnAction(new follow(artistID));
        artistPane.getChildren().add(followButton);

        followersCount = new Label(getFollowersCount(artistID, true));
        followersCount.setLayoutX(87);
        followersCount.setLayoutY(163);
        artistPane.getChildren().add(followersCount);

        Button likedArtistsSongsButton = new Button("Liked songs");
        likedArtistsSongsButton.setFont(new Font(25));
        likedArtistsSongsButton.setLayoutX(190);
        likedArtistsSongsButton.setLayoutY(70);
        likedArtistsSongsButton.setMinHeight(70);
        likedArtistsSongsButton.setMaxHeight(70);
        likedArtistsSongsButton.setMinWidth(250);
        likedArtistsSongsButton.setMaxWidth(250);
        likedArtistsSongsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    ArrayList < Integer > songsID = getLikedArtistSongs(UserID, artistID);

                    centralVBox.getChildren().remove(0, centralVBox.getChildren().size());

                    Pane titlePane = new Pane();
                    titlePane.setMinWidth(452);
                    titlePane.setMaxWidth(452);
                    titlePane.setMinHeight(70);
                    titlePane.setMaxHeight(70);
                    titlePane.setLayoutX(5);
                    titlePane.setLayoutY(5);

                    Label welcomeLabel = new Label(getArtistName(artistID));
                    welcomeLabel.setMinHeight(50);
                    welcomeLabel.setMaxHeight(50);
                    welcomeLabel.setMinWidth(350);
                    welcomeLabel.setMaxWidth(350);
                    welcomeLabel.setLayoutX(140);
                    welcomeLabel.setLayoutY(5);
                    welcomeLabel.setFont(new Font("Arial", 35));
                    titlePane.getChildren().add(welcomeLabel);

                    Button createNewPL = new Button("Back to artist");
                    createNewPL.setLayoutX(10);
                    createNewPL.setLayoutY(20);
                    createNewPL.setOnAction((event) -> {
                        loadAnotherArtist = artistID;
                        try {
                            goToArtist();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    titlePane.getChildren().add(createNewPL);

                    centralVBox.getChildren().add(titlePane);

                    if (songsID.isEmpty()) {
                        Pane sadPane = new Pane();
                        sadPane.setMinHeight(70);
                        sadPane.setMaxHeight(70);
                        sadPane.setMinWidth(300);
                        sadPane.setMaxWidth(300);
                        Label sadLabel = new Label("No liked songs yet :(");
                        sadLabel.setFont(new Font("Allegro", 20));
                        sadLabel.setLayoutX(40);
                        sadPane.getChildren().add(sadLabel);
                        centralVBox.getChildren().add(sadPane);
                    }

                    for (Integer curSongID : songsID) {
                        centralVBox.getChildren().add(loadSongPane(curSongID, -2, true));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        artistPane.getChildren().add(likedArtistsSongsButton);

        centralVBox.getChildren().add(artistPane);
    }

    public Pane loadSongPane(int songID, int playlistID, boolean isLiked) throws Exception {
        Pane songPane = new Pane();
        songPane.setMinHeight(70);
        songPane.setMaxHeight(70);
        songPane.setMinWidth(280);
        songPane.setMaxWidth(280);
        songPane.setLayoutX(160);
        songPane.setLayoutY(lastSongLayout + 5);
        lastSongLayout += songPane.getMaxHeight();

        if (isLiked) {
            ImageView albumImage = new ImageView(new File("src/main/resources/com/lihtar/lihtar/Images/AlbumsImages/" + getAlbumID(songID) + "albumImage.png").toURI().toString());
            albumImage.setFitWidth(60);
            albumImage.setFitHeight(60);
            albumImage.setLayoutX(5);
            albumImage.setLayoutY(10);
            songPane.getChildren().add(albumImage);
        }

        Button playSong = new Button("PLAY");
        playSong.setLayoutX(5);
        playSong.setLayoutY(10);
        playSong.setMinHeight(55);
        playSong.setMaxHeight(55);
        if (isLiked) {
            playSong.setLayoutX(70);
        }
        if (playlistID == -2) {
            playSong.setOnAction(new playNewSongAction(getArtistID(getAlbumID(songID)), 3, songID));
        } else if (isLiked) {
            playSong.setOnAction(new playNewSongAction(-1, 1, songID));
        } else if (playlistID == -1) {
            playSong.setOnAction(new playNewSongAction(getAlbumID(songID), 0, songID));
        } else {
            playSong.setOnAction(new playNewSongAction(playlistID, 2, songID));
        }
        songPane.getChildren().add(playSong);

        Label songName = new Label(getSongName(songID));
        songName.setLayoutX(65);
        if (isLiked) {
            songName.setLayoutX(130);
        }
        songName.setLayoutY(10);
        songName.setMaxWidth(150);
        songName.setFont(new Font("BankGothic MD BT", 18));
        songPane.getChildren().add(songName);

        Label artistName = new Label(getArtistName(getArtistID(getAlbumID(songID))));
        artistName.setLayoutX(65);
        if (isLiked) {
            artistName.setLayoutX(130);
        }
        artistName.setLayoutY(35);
        artistName.setMaxWidth(185);
        artistName.setFont(new Font("Allegro", 14));
        songPane.getChildren().add(artistName);

        Button likeSong = new Button("Like");
        if (isLiked(UserID, songID)) {
            likeSong.setText("Liked");
        }
        likeSong.setOnAction(new likeSongAction(songID, likeSong));
        likeSong.setLayoutX(230);
        if (isLiked) {
            likeSong.setLayoutX(390);
        }
        likeSong.setLayoutY(8);
        songPane.getChildren().add(likeSong);

        if (playlistID == -1 || playlistID == -2) {
            MenuButton addToPlaylistsButton = new MenuButton();
            addToPlaylistsButton.setLayoutX(230);
            if (isLiked) {
                addToPlaylistsButton.setLayoutX(390);
            }
            addToPlaylistsButton.setLayoutY(40);
            addToPlaylistsButton.setText("ADD");
            for (int id : getUsersPlaylists(UserID)) {
                if (id >= 1e9) continue;
                MenuItem curItem = new MenuItem(getPlaylistName(id));
                curItem.setOnAction(new addSongToPlaylistAction(songID, id));
                addToPlaylistsButton.getItems().add(curItem);
            }
            songPane.getChildren().add(addToPlaylistsButton);
        } else {
            Button removeFromPlaylist = new Button("Remove");
            removeFromPlaylist.setLayoutX(230);
            removeFromPlaylist.setLayoutY(40);
            removeFromPlaylist.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    if (Objects.equals(removeFromPlaylist.getText(), "Remove")) {
                        removeFromPlaylist.setText("Add");
                        try {
                            deleteSongFromPlaylist(songID, playlistID);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        removeFromPlaylist.setText("Remove");
                        try {
                            addSongToPlaylist(songID, playlistID);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            songPane.getChildren().add(removeFromPlaylist);
        }
        return songPane;
    }

    class addSongToPlaylistAction implements EventHandler<ActionEvent> {
        private int songID, playlistID;
        addSongToPlaylistAction(int songID, int playlistID) {
            this.songID = songID;
            this.playlistID = playlistID;
        };
        @Override
        public void handle(ActionEvent actionEvent) {
            try {
                addSongToPlaylist(songID, playlistID);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void loadAlbumPane(int albumID) throws Exception {
        Pane albumPane = new Pane();
        albumPane.setMinWidth(452);
        albumPane.setMaxWidth(452);
        albumPane.setMinHeight(250);
        albumPane.setMaxHeight(250);

        ImageView albumImage = new ImageView(new File("src/main/resources/com/lihtar/lihtar/Images/AlbumsImages/" + albumID + "albumImage.png").toURI().toString());
        albumImage.setLayoutX(5);
        albumImage.setLayoutY(5);
        albumImage.setFitHeight(150);
        albumImage.setFitWidth(150);
        albumPane.getChildren().add(albumImage);

        Label albumName = new Label(getAlbumName(albumID));
        albumName.setLayoutX(10);
        albumName.setLayoutY(160);
        albumName.setFont(new Font("Verdana", 15));
        albumPane.getChildren().add(albumName);

        Label albumDate = new Label(getAlbumDate(albumID));
        albumDate.setLayoutX(10);
        albumDate.setLayoutY(180);
        albumDate.setFont(new Font("Verdana", 15));
        albumPane.getChildren().add(albumDate);

        Button likeAlbumButton = new Button();
        if (isLikedAlbum(UserID, (int) (1e9 + albumID))) {
            likeAlbumButton.setText("Liked album");
        } else {
            likeAlbumButton.setText("Like album");
        }
        likeAlbumButton.setLayoutX(10);
        likeAlbumButton.setLayoutY(200);
        likeAlbumButton.setOnAction(new likeAlbumAction(albumID, likeAlbumButton));
        albumPane.getChildren().add(likeAlbumButton);

        lastSongLayout = 0;
        int cnt = 0;
        for (Integer songID : getSongs(albumID)) {
            Pane songPane = loadSongPane(songID, -1, false);
            albumPane.getChildren().add(songPane);
            ++cnt;
            if (cnt > 3) {
                albumPane.setMinHeight(albumPane.getMinHeight() + songPane.getMinHeight());
                albumPane.setMaxHeight(albumPane.getMaxHeight() + songPane.getMaxHeight());
            }
        }

        centralVBox.getChildren().add(albumPane);
    }

    public void loadPlaylistPane(int albumID) throws Exception {
        Pane albumPane = new Pane();
        albumPane.setMinWidth(452);
        albumPane.setMaxWidth(452);
        albumPane.setMinHeight(250);
        albumPane.setMaxHeight(250);

        ImageView albumImage = new ImageView(new File("src/main/resources/com/lihtar/lihtar/Images/MusicPlaceHolder.png").toURI().toString());
        albumImage.setLayoutX(5);
        albumImage.setLayoutY(5);
        albumImage.setFitHeight(150);
        albumImage.setFitWidth(150);
        albumPane.getChildren().add(albumImage);

        Label albumName = new Label(getPlaylistName(albumID));
        albumName.setLayoutX(10);
        albumName.setLayoutY(160);
        albumName.setFont(new Font("Verdana", 15));
        albumPane.getChildren().add(albumName);

        Label albumDate = new Label(getPlaylistDate(albumID));
        albumDate.setLayoutX(10);
        albumDate.setLayoutY(180);
        albumDate.setFont(new Font("Verdana", 15));
        albumPane.getChildren().add(albumDate);

        Button likeAlbumButton = new Button();
        if (isLikedAlbum(UserID, albumID)) {
            likeAlbumButton.setText("Delete playlist");
        } else {
            likeAlbumButton.setText("Add playlist");
        }
        likeAlbumButton.setLayoutX(10);
        likeAlbumButton.setLayoutY(200);
        likeAlbumButton.setOnAction(new likeAlbumAction((int) (albumID - 1e9), likeAlbumButton));
        albumPane.getChildren().add(likeAlbumButton);

        lastSongLayout = 0;

        int cnt = 0;
        for (Integer songID : getSongsFromPL(albumID)) {
            Pane songPane = loadSongPane(songID, albumID, false);
            albumPane.getChildren().add(songPane);
            ++cnt;
            if (cnt > 3) {
                albumPane.setMinHeight(albumPane.getMinHeight() + songPane.getMinHeight());
                albumPane.setMaxHeight(albumPane.getMaxHeight() + songPane.getMaxHeight());
            }
        }

        centralVBox.getChildren().add(albumPane);
    }

    public void loadArtistAlbums(int artistID) throws Exception {
        for (Integer currentAlbumID : getArtistAlbums(artistID)) {
            loadAlbumPane(currentAlbumID);
        }
    }

    public void goToArtist() throws Exception {
        centralVBox.getChildren().remove(0, centralVBox.getChildren().size());
        int artistID = currentArtistID;
        if (loadAnotherArtist != -1) {
            artistID = loadAnotherArtist;
            loadAnotherArtist = -1;
        }
        loadArtistPane(artistID);
        loadArtistAlbums(artistID);
    }

    public void goToLikedSongs() throws Exception {
        lastSongLayout = 0;
        centralVBox.getChildren().remove(0, centralVBox.getChildren().size());
        for (Integer currentLikedSong : getLikedSongs(UserID)) {
            centralVBox.getChildren().add(loadSongPane(currentLikedSong, -1, true));
        }
    }

    public void findButton() throws Exception {
        if (!findTextField.getText().isEmpty()) {
            ArrayList < Integer > allSongs = getAllSongs();
            ArrayList < Integer > allArtists = getAllArtists();
            ArrayList < classForSearching > allResults = new ArrayList<>();
            for (Integer curSong : allSongs) {
                allResults.add(new classForSearching(Integer.parseInt(getFollowersCount(getArtistID(getAlbumID(curSong)), false)), curSong, -1));
            }
            for (Integer curArtist : allArtists) {
                allResults.add(new classForSearching(Integer.parseInt(getFollowersCount(curArtist, false)), curArtist, 1));
            }
            Collections.sort(allResults, new Comparator< classForSearching > (){
                @Override
                public int compare(classForSearching firstVal, classForSearching secondVal) {
                    if (secondVal.rating != firstVal.rating) {
                        return Integer.compare(secondVal.rating, firstVal.rating);
                    }
                    return Integer.compare(secondVal.type, firstVal.type);
                }
            });

            centralVBox.getChildren().remove(0, centralVBox.getChildren().size());
            lastSongLayout = 0;

            boolean hadFinedSmth = false;

            for (classForSearching currentResult : allResults) {
                if (currentResult.type == 1) {
                    if (getArtistName(currentResult.id).contains(findTextField.getText())) {
                        loadFindedArtist(currentResult.id);
                        hadFinedSmth = true;
                    }
                } else {
                    if (getSongName(currentResult.id).contains(findTextField.getText())) {
                        loadFindedSong(currentResult.id);
                        hadFinedSmth = true;
                    }
                }
            }
            if (!hadFinedSmth) {
                Pane sadPane = new Pane();
                sadPane.setMinHeight(70);
                sadPane.setMaxHeight(70);
                sadPane.setMinWidth(300);
                sadPane.setMaxWidth(300);
                Label sadLabel = new Label("No results founded :(");
                sadLabel.setFont(new Font("Allegro", 20));
                sadLabel.setLayoutY(10);
                sadLabel.setLayoutX(40);
                sadPane.getChildren().add(sadLabel);
                centralVBox.getChildren().add(sadPane);
            }
        }
    }

    public void loadFindedArtist(int artistID) throws Exception {

        Pane artistPane = new Pane();
        artistPane.setMinHeight(70);
        artistPane.setMaxHeight(70);
        artistPane.setMinWidth(280);
        artistPane.setMaxWidth(280);
        artistPane.setLayoutX(160);
        artistPane.setLayoutY(lastSongLayout + 5);
        lastSongLayout += 70;

        ImageView artistAvatar = new ImageView(new File("src/main/resources/com/lihtar/lihtar/Images/ArtistsAvatars/" + artistID + "artistAvatar.png").toURI().toString());
        artistAvatar.setLayoutX(5);
        artistAvatar.setLayoutY(10);
        artistAvatar.setFitHeight(60);
        artistAvatar.setFitWidth(60);
        artistPane.getChildren().add(artistAvatar);

        Label artistName = new Label(getArtistName(artistID));
        artistName.setFont(new Font("Allegro", 25));
        artistName.setLayoutX(80);
        artistName.setLayoutY(20);
        artistName.setMaxWidth(185);
        artistPane.getChildren().add(artistName);

        artistPane.setOnMouseClicked(new goToArtistAction(artistID));

        centralVBox.getChildren().add(artistPane);
    }

    public void loadFindedSong(int songID) throws Exception {

        Pane songPane = new Pane();
        songPane.setMinHeight(70);
        songPane.setMaxHeight(70);
        songPane.setMinWidth(280);
        songPane.setMaxWidth(280);
        songPane.setLayoutX(160);
        songPane.setLayoutY(lastSongLayout + 5);
        lastSongLayout += songPane.getMaxHeight();

        ImageView albumImage = new ImageView(new File("src/main/resources/com/lihtar/lihtar/Images/AlbumsImages/" + getAlbumID(songID) + "albumImage.png").toURI().toString());
        albumImage.setFitWidth(60);
        albumImage.setFitHeight(60);
        albumImage.setLayoutX(5);
        albumImage.setLayoutY(10);
        songPane.getChildren().add(albumImage);

        Button playSong = new Button("PLAY");
        playSong.setLayoutX(70);
        playSong.setLayoutY(10);
        playSong.setMinHeight(55);
        playSong.setMaxHeight(55);
        playSong.setOnAction(new playNewSongAction(-1, 4, songID));
        songPane.getChildren().add(playSong);

        Label songName = new Label(getSongName(songID));
        songName.setLayoutX(130);
        songName.setLayoutY(10);
        songName.setMaxWidth(150);
        songName.setFont(new Font("BankGothic MD BT", 18));
        songPane.getChildren().add(songName);

        Label artistName = new Label(getArtistName(getArtistID(getAlbumID(songID))));
        artistName.setLayoutX(130);
        artistName.setLayoutY(35);
        artistName.setMaxWidth(185);
        artistName.setFont(new Font("Allegro", 14));
        songPane.getChildren().add(artistName);

        Button likeSong = new Button("Like");
        if (isLiked(UserID, songID)) {
            likeSong.setText("Liked");
        }
        likeSong.setOnAction(new likeSongAction(songID, likeSong));
        likeSong.setLayoutX(390);
        likeSong.setLayoutY(8);
        songPane.getChildren().add(likeSong);

        MenuButton addToPlaylistsButton = new MenuButton();
        addToPlaylistsButton.setLayoutX(390);
        addToPlaylistsButton.setLayoutY(40);
        addToPlaylistsButton.setText("ADD");
        for (int id : getUsersPlaylists(UserID)) {
            if (id >= 1e9) continue;
            MenuItem curItem = new MenuItem(getPlaylistName(id));
            curItem.setOnAction(new addSongToPlaylistAction(songID, id));
            addToPlaylistsButton.getItems().add(curItem);
        }
        songPane.getChildren().add(addToPlaylistsButton);
        centralVBox.getChildren().add(songPane);
    }
   class goToArtistAction implements EventHandler<MouseEvent> {
        private int artistID;
        goToArtistAction(int artistID) {
            this.artistID = artistID;
        }
       @Override
       public void handle(MouseEvent mouseEvent) {
            loadAnotherArtist = artistID;
           try {
               goToArtist();
           } catch (Exception e) {
               throw new RuntimeException(e);
           }
       }
   }
    public class classForSearching {
        public int rating, id, type;
        classForSearching(int rating, int id, int type) {
            this.rating = rating;
            this.id = id;
            this.type = type;
        }
    }

    public void logOut() {
        if (Objects.equals(playButton.getText(), "STOP")) {
            play();
        }
        FXMLLoader loader = LoadXML.load("StartApplicationView.fxml");
        setScene(loader);
    }

    public void loadTitleLibPane() {
        Pane titlePane = new Pane();
        titlePane.setMinWidth(452);
        titlePane.setMaxWidth(452);
        titlePane.setMinHeight(70);
        titlePane.setMaxHeight(70);
        titlePane.setLayoutX(5);
        titlePane.setLayoutY(5);

        Label welcomeLabel = new Label("Your Library");
        welcomeLabel.setMinHeight(50);
        welcomeLabel.setMaxHeight(50);
        welcomeLabel.setMinWidth(300);
        welcomeLabel.setMaxWidth(300);
        welcomeLabel.setLayoutX(30);
        welcomeLabel.setLayoutY(5);
        welcomeLabel.setFont(new Font("Arial", 40));
        titlePane.getChildren().add(welcomeLabel);

        Button createNewPL = new Button("Create new Playlist");
        createNewPL.setLayoutX(300);
        createNewPL.setLayoutY(20);
        createNewPL.setOnAction(new createPLAction(titlePane));
        titlePane.getChildren().add(createNewPL);

        centralVBox.getChildren().add(titlePane);
    }
    public void goToLibrary() throws Exception {
        centralVBox.getChildren().remove(0, centralVBox.getChildren().size());
        loadTitleLibPane();
        ArrayList < Integer > playlistsID = getUsersPlaylists(UserID);
        for (int id : playlistsID) {
            if (id >= 1e9) {
                loadAlbumPane((int) (id - 1e9));
            } else {
                loadPlaylistPane(id);
            }
        }
    }

    class createPLAction implements EventHandler<ActionEvent> {
        private Pane curPane;
        createPLAction(Pane curPane) {
            this.curPane = curPane;
        }
        @Override
        public void handle(ActionEvent actionEvent) {
            curPane.getChildren().remove(0, curPane.getChildren().size());

            Button cancelButton = new Button("Cancel");
            cancelButton.setLayoutX(10);
            cancelButton.setLayoutY(5);
            cancelButton.setOnAction(new cancelAction());
            curPane.getChildren().add(cancelButton);

            TextField nameOfPL = new TextField();
            nameOfPL.setPromptText("Give your playlist a name");
            nameOfPL.setMinWidth(200);
            nameOfPL.setMaxWidth(200);
            nameOfPL.setLayoutX(80);
            nameOfPL.setLayoutY(5);
            curPane.getChildren().add(nameOfPL);

            ChoiceBox < String > accessType = new ChoiceBox< String >();
            accessType.setValue("Public");
            accessType.getItems().add("Private");
            accessType.getItems().add("Public");
            accessType.setLayoutX(290);
            accessType.setLayoutY(5);
            curPane.getChildren().add(accessType);

            Label errorLabel = new Label();
            errorLabel.setLayoutX(85);
            errorLabel.setLayoutY(30);
            errorLabel.setTextFill(Color.web("#dd0e0e", 0.8));
            curPane.getChildren().add(errorLabel);

            Button create = new Button("Create");
            create.setLayoutX(390);
            create.setLayoutY(5);
            create.setOnAction(new createNewPL(errorLabel, nameOfPL, accessType));
            curPane.getChildren().add(create);
        }
    }

    class createNewPL implements EventHandler<ActionEvent> {

        private Label errorLabel;
        private TextField name;
        private ChoiceBox < String > access;
        createNewPL(Label errorLabel, TextField name, ChoiceBox < String > access) {
            this.errorLabel = errorLabel;
            this.access = access;
            this.name = name;
        }

        @Override
        public void handle(ActionEvent actionEvent) {
            try {
                try {
                    name.setText(isSqlFriendly(name.getText(), false));
                } catch (InvalidCharactersException e) {
                        errorLabel.setText("Incorrect characters used");
                    return;
                } catch (EmptyStringException e) {
                    errorLabel.setText("Playlist name cannot be empty");
                    return;
                }
                errorLabel.setText("");
                createNewPlayList(User.UserID, name.getText(), access.getSelectionModel().getSelectedItem());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                goToLibrary();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    class cancelAction implements EventHandler<ActionEvent> {
        cancelAction() {};
        @Override
        public void handle(ActionEvent actionEvent) {
            try {
                goToLibrary();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static public class currentPlaylist {
        public static ArrayList < Integer > songsID = new ArrayList<>(), lastPlayed = new ArrayList<>();
        public static int playingSongNumber = 0;
        public static int playingMode;
        currentPlaylist() {};
        public static void nextSong() {
            if (Objects.equals(pModesChoiceBox.getSelectionModel().getSelectedItem(), "In order")) {
                playingMode = 0;
            } else if (Objects.equals(pModesChoiceBox.getSelectionModel().getSelectedItem(), "Randomly")) {
                playingMode = 1;
            } else {
                playingMode = 2;
            }
            if (playingMode == 0) {
                ++playingSongNumber;
                if (playingSongNumber == songsID.size()) {
                    playingSongNumber = 0;
                }
                return;
            }
            if (playingMode == 1) {
                playingSongNumber = (new Random()).nextInt(songsID.size());
                return;
            }
            if (playingSongNumber == 2) {
                return;
            }
        }

        public static int prevSong() {
            if (lastPlayed.isEmpty()) {
                return currentSongID;
            }
            int result = lastPlayed.get(lastPlayed.size() - 1);
            lastPlayed.remove(lastPlayed.size() - 1);
            return result;
        }

    }

    class playNewSongAction implements EventHandler<ActionEvent> {
        private int albumID;
        private int albumType;
        private int songID;
        playNewSongAction(int albumID, int albumType, int songID) {
            this.albumID = albumID;
            this.albumType = albumType;
            this.songID = songID;
        }
        @Override
        public void handle(ActionEvent actionEvent) {
            if (albumType == 0) {
                try {
                    currentPlaylist.songsID = getSongs(albumID);
                    playingSongNumber = 0;
                    for (int i : currentPlaylist.songsID) {
                        if (i == songID) {
                            break;
                        }
                        ++playingSongNumber;
                    }
                    if (Objects.equals(playButton.getText(), "STOP")) {
                        play();
                    }
                    loadSong(songID);
                    if (Objects.equals(playButton.getText(), "PLAY")) {
                        play();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (albumType == 1) {
                try {
                    currentPlaylist.songsID = getLikedSongs(UserID);
                    playingSongNumber = 0;
                    for (int i : currentPlaylist.songsID) {
                        if (i == songID) {
                            break;
                        }
                        ++playingSongNumber;
                    }
                    if (Objects.equals(playButton.getText(), "STOP")) {
                        play();
                    }
                    loadSong(songID);
                    if (Objects.equals(playButton.getText(), "PLAY")) {
                        play();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (albumType == 2) {
                try {
                    currentPlaylist.songsID = getPlaylistSongs(albumID);
                    playingSongNumber = 0;
                    for (int i : currentPlaylist.songsID) {
                        if (i == songID) {
                            break;
                        }
                        ++playingSongNumber;
                    }
                    if (Objects.equals(playButton.getText(), "STOP")) {
                        play();
                    }
                    loadSong(songID);
                    if (Objects.equals(playButton.getText(), "PLAY")) {
                        play();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (albumType == 3) {
                try {
                    currentPlaylist.songsID = getLikedArtistSongs(UserID, albumID);
                    playingSongNumber = 0;
                    for (int i : currentPlaylist.songsID) {
                        if (i == songID) {
                            break;
                        }
                        ++playingSongNumber;
                    }
                    if (Objects.equals(playButton.getText(), "STOP")) {
                        play();
                    }
                    loadSong(songID);
                    if (Objects.equals(playButton.getText(), "PLAY")) {
                        play();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (albumType == 4) {
                try {
                    currentPlaylist.songsID = new ArrayList<>();
                    currentPlaylist.songsID.add(songID);
                    playingSongNumber = 0;
                    if (Objects.equals(playButton.getText(), "STOP")) {
                        play();
                    }
                    loadSong(songID);
                    if (Objects.equals(playButton.getText(), "PLAY")) {
                        play();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void playNextSong() throws Exception {
        currentPlaylist.nextSong();
        if (Objects.equals(playButton.getText(), "STOP")) {
            play();
        }
        loadSong(currentPlaylist.songsID.get(playingSongNumber));
        if (Objects.equals(playButton.getText(), "PLAY")) {
            play();
        }
    }

    public void playPrevSong() throws Exception {
        if (lastPlayed.size() > 0) {
            lastPlayed.remove(lastPlayed.size() - 1);
        }
        if (Objects.equals(playButton.getText(), "STOP")) {
            play();
        }
        loadSong(currentPlaylist.prevSong());
        if (Objects.equals(playButton.getText(), "PLAY")) {
            play();
        }
    }
}
