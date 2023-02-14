drop table users;

create table users (
        id integer primary key,
        nickname varchar(100) not null,
        password varchar(100) not null
);

create table songs(
        id integer primary key,
        albumID integer references albums(id),
        song_name varchar(100) not null
);


create table albums(
        id integer primary key,
        artistID integer references artists(id),
        album_name varchar(100) not null,
        pub_date date
);

create table artists(
        id integer primary key,
        artist_name varchar(100) not null
);

create table followings(
    artistID integer references artists(id),
    userID integer references users(id),
    CONSTRAINT uniq UNIQUE(artistID, userID)
);

create table playlists (
    id integer primary key,
    userID integer references users(id),
    playlist_name varchar(100) not null,
    access integer default 0,
    create_date date NOT NULL
        DEFAULT CURRENT_DATE
);

create table playlist_songs (
    playlistID integer references playlists(id),
    songID integer references songs(id),
    cnt integer not null
);

create table liked_songs(
    userID integer references users(id),
    songID integer references songs(id),
    CONSTRAINT uniqLS UNIQUE(songID, userID)
);

create table library(
    id integer references users(id),
    playlistID integer not null
);