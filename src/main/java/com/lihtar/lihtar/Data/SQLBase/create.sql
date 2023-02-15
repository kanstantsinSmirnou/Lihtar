create table users (
        id integer primary key,
        nickname varchar(100) not null,
        password varchar(100) not null
);


create table artists(
        id integer primary key,
        artist_name varchar(100) not null
);


create table albums(
        id integer primary key,
        artistID integer references artists(id),
        album_name varchar(100) not null,
        pub_date date
);


create table songs(
        id integer primary key,
        albumID integer references albums(id),
        song_name varchar(100) not null
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


insert into artists values(0, 'Joy Division');
insert into artists values(1, 'Nurnberg');
insert into artists values(2, 'SONIC DEATH');

insert into albums values(0, 0, 'The Best Of', '2008-04-24');
insert into albums values(1, 1, 'U nikudy', '2018-05-13');
insert into albums values(2, 1, 'Skryvaj', '2019-09-23');
insert into albums values(3, 1, 'Ahida', '2023-01-11');
insert into albums values(4, 2, 'Punks Against Mafia', '2018-05-14');

insert into songs values(0, 0, 'Digital');
insert into songs values(1, 0, 'Transmission');
insert into songs values(2, 0, 'Shes lost control');
insert into songs values(3, 0, 'Shadowplay');
insert into songs values(4, 0, 'Love Will Tear Us Apart');
insert into songs values(5, 0, 'Disorder');

insert into songs values(6, 1, 'Intro');
insert into songs values(7, 1, 'Los');
insert into songs values(8, 1, 'U nikudy');
insert into songs values(9, 1, 'Patanuc');
insert into songs values(10, 1, 'Outro');

insert into songs values(11, 2, 'Biessensounasc');
insert into songs values(12, 2, 'Niemahcyma');
insert into songs values(13, 2, 'Rasplyvajecca');
insert into songs values(14, 2, 'Spac');
insert into songs values(15, 2, 'Staracca');
insert into songs values(16, 2, 'Usio rouna');
insert into songs values(17, 2, 'Valasy');
insert into songs values(18, 2, 'Zorstka');

insert into songs values(19, 3, 'Himn');
insert into songs values(20, 3, 'Maska');
insert into songs values(21, 3, 'Pacatak');
insert into songs values(22, 3, 'Pytanni');

insert into songs values(23, 4, 'Belyj Musor');
insert into songs values(24, 4, 'Golos Iz Temnoty');
insert into songs values(25, 4, 'Home Skin');
insert into songs values(26, 4, 'Mir Machine');