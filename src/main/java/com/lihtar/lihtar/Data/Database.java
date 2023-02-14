package com.lihtar.lihtar.Data;

import com.lihtar.lihtar.Data.SQLBase.SqlCommunicate;
import javafx.application.Platform;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import static com.lihtar.lihtar.Data.SQLBase.SqlCommunicate.update;

public class Database {

    public static class InvalidCharactersException extends Exception {}
    public static class IncorrectUserDataException extends Exception {}
    public static class EmptyStringException extends Exception {}
    public static class UserAlreadyRegisteredException extends Exception {}

    public static String correctChars = ".!,?-+_ ";
    public static String isSqlFriendly(String str, boolean isPass) throws Exception {
        if (!isPass) {
            StringBuilder parsedStr = new StringBuilder(new String());
            boolean ok = false;
            for (int i = 0; i < str.length(); ++i) {
                if (ok || str.charAt(i) != ' ') {
                    ok = true;
                    parsedStr.append(str.charAt(i));
                    continue;
                }
            }
            str = parsedStr.toString();
        }
        if (str.length() == 0) throw new Database.EmptyStringException();
        for (int i = 0; i < str.length(); ++i) {
            if (isPass && str.charAt(i) == ' ') throw new InvalidCharactersException();
            if (str.charAt(i) >= 'A' && str.charAt(i) <= 'Z') continue;
            if (str.charAt(i) >= 'a' && str.charAt(i) <= 'z') continue;
            if (str.charAt(i) >= '0' && str.charAt(i) <= '9') continue;
            if (correctChars.indexOf(str.charAt(i)) != -1) continue;
            throw new Database.InvalidCharactersException();
        }
        return str;
    }
    static public User getUser(String nickname, String password) throws Exception {
        try {
            isSqlFriendly(password, true);
            isSqlFriendly(nickname, true);
        } catch (Exception e) {
            throw e;
        }
        try {
            String query = "select * from users where nickname = '" + nickname + "' and password = '" + password + "';";
            int id = Integer.parseInt(SqlCommunicate.execute(query).get(1).get(0));
            return Database.getUserById(id);
        } catch (Exception e) {
            throw new IncorrectUserDataException();
        }
    }
    static public User getUserById(int id) throws Exception {
        try {
            String query = "select * from users where id = " + id + ";";
            return User.makeUserFromBase(SqlCommunicate.execute(query).get(1).get(1), id);
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public void registerUser(String nickname, String password) throws Exception {
        try {
            isSqlFriendly(password, true);
            isSqlFriendly(nickname, true);
        } catch (Exception e) {
            throw e;
        }
        String query = "select * from users where nickname = '" + nickname + "';";
        if (SqlCommunicate.execute(query).size() - 1 > 0) {
            throw new UserAlreadyRegisteredException();
        }
        int id = SqlCommunicate.execute("select * from users;").size();
        query = "insert into users values(" + id + ", '" + nickname + "', '" + password + "');";
        update(query);
    }

    static public String getSongName(int songID) throws Exception {
        try {
            String query = "select song_name from songs where id = " + songID + ";";
            return SqlCommunicate.execute(query).get(1).get(0);
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public Integer getAlbumID(int songID) throws Exception {
        try {
            String query = "select albumID from songs where id = " + songID + ";";
            return Integer.parseInt(SqlCommunicate.execute(query).get(1).get(0));
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public Integer getArtistID(int albumID) throws Exception {
        try {
            String query = "select artistID from albums where id = " + albumID + ";";
            return Integer.parseInt(SqlCommunicate.execute(query).get(1).get(0));
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public String getArtistName(int artistID) throws Exception {
        try {
            String query = "select artist_name from artists where id = " + artistID + ";";
            return SqlCommunicate.execute(query).get(1).get(0);
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public boolean isFollow(int userID, int artistID) throws Exception {
        try {
            String query = "select userid from followings where userid = " + userID + " and artistid = " + artistID + ";";
            return SqlCommunicate.execute(query).size() == 2;
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public void followArtist(int userID, int artistID) throws Exception {
        try {
            String query = "insert into followings(userid, artistid) values(" + userID + ", " + artistID + ");";
            update(query);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public void unfollowArtist(int userID, int artistID) throws Exception {
        try {
            String query = "delete from followings where userid = " + userID + " and artistid =  " + artistID + ";";
            update(query);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public String getFollowersCount(int artistID, boolean withEnding) throws Exception {
        try {
            String query = "select count(*) from followings where artistID = " + artistID + ";";
            if (withEnding) {
                return SqlCommunicate.execute(query).get(1).get(0) + " followers";
            } else {
                return SqlCommunicate.execute(query).get(1).get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public ArrayList < Integer > getArtistAlbums(int artistID) throws Exception {
        try {
            String query = "select id from albums where artistID = " + artistID + ";";
            ArrayList < ArrayList < String > > query_result = SqlCommunicate.execute(query);
            ArrayList < Integer > parsed_result = new ArrayList<>();
            for (int i = 1; i < query_result.size(); ++i) {
                parsed_result.add(Integer.parseInt(query_result.get(i).get(0)));
            }
            return parsed_result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public ArrayList < Integer > getSongs(int albumID) throws Exception {
        try {
            String query = "select id from songs where albumID = " + albumID + ";";
            ArrayList < ArrayList < String > > query_result = SqlCommunicate.execute(query);
            ArrayList < Integer > parsed_result = new ArrayList<>();
            for (int i = 1; i < query_result.size(); ++i) {
                parsed_result.add(Integer.parseInt(query_result.get(i).get(0)));
            }
            return parsed_result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

     static public ArrayList < Integer > getSongsFromPL(int albumID) throws Exception {
        try {
            String query = "select songID from playlist_songs where playlistID = " + albumID + ";";
            ArrayList < ArrayList < String > > query_result = SqlCommunicate.execute(query);
            ArrayList < Integer > parsed_result = new ArrayList<>();
            for (int i = 1; i < query_result.size(); ++i) {
                parsed_result.add(Integer.parseInt(query_result.get(i).get(0)));
            }
            return parsed_result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public String getAlbumName(int albumID) throws Exception {
        try {
            String query = "select album_name from albums where id = " + albumID + ";";
            return SqlCommunicate.execute(query).get(1).get(0);
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public String getPlaylistName(int albumID) throws Exception {
        try {
            String query = "select playlist_name from playlists where id = " + albumID + ";";
            return SqlCommunicate.execute(query).get(1).get(0);
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public String getAlbumDate(int albumID) throws Exception {
        try {
            String query = "select pub_date from albums where id = " + albumID + ";";
            return SqlCommunicate.execute(query).get(1).get(0);
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public String getPlaylistDate(int albumID) throws Exception {
        try {
            String query = "select create_date from playlists where id = " + albumID + ";";
            return SqlCommunicate.execute(query).get(1).get(0);
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public boolean isLiked(int userID, int songID) throws Exception {
        try {
            String query = "select userID from liked_songs where userID = " + userID + " and songID = " + songID + ";";
            return SqlCommunicate.execute(query).size() == 2;
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public boolean isLikedAlbum(int userID, int albumID) throws Exception {
        try {
            String query = "select id from library where id = " + userID + " and playlistID = " + albumID + ";";
            return SqlCommunicate.execute(query).size() == 2;
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public void likeSong(int userID, int songID) throws Exception {
        try {
            String query = "insert into liked_songs values(" + userID + ", " + songID + ");";
            update(query);
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }
    static public void likeAlbum(int userID, int albumID) throws Exception {
        try {
            albumID += 1e9;
            String query = "insert into library values(" + userID + ", " + albumID + ");";
            update(query);
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }
    static public Integer getSongInPlCnt(int songID, int playlistID) throws Exception {
        try {
            String query = "select COUNT(cnt) from playlist_songs where playlistID = " + playlistID + " and songID = " + songID + ";";
            if (Objects.equals(SqlCommunicate.execute(query).get(1).get(0), "0")) {
                return 0;
            }
            query = "select MAX(cnt) from playlist_songs where playlistID = " + playlistID + " and songID = " + songID + ";";
            return Integer.parseInt(SqlCommunicate.execute(query).get(1).get(0));
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }
    static public void addSongToPlaylist(int songID, int playlistID) throws Exception {
        try {
            String query = "insert into playlist_songs values(" + playlistID + ", " + songID + ", " + (int)(getSongInPlCnt(songID, playlistID) + 1) + ");";
            update(query);
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }
    static public void deleteSongFromLiked(int userID, int songID) throws Exception {
        try {
            String query = "delete from liked_songs where userID = " + userID + " and songID =  " + songID + ";";
            update(query);
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public void deletePlaylist(int albumID) {
        try {
            if (albumID < 1e9) {
                deletePlaylistTrash(albumID);
                String query = "delete from playlists where id = " + albumID + ";";
                update(query);
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    static public void deleteAlbumFromLiked(int userID, int albumID) throws Exception {
        try {
            albumID += 1e9;
            String query = "delete from library where id = " + userID + " and playlistID =  " + albumID + ";";
            update(query);
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }
    static public void deleteSongFromPlaylist(int songID, int playlistID) throws Exception {
        try {
            String query = "delete from playlist_songs where playlistID = " + playlistID + " and songID =  " + songID + " and cnt = " + getSongInPlCnt(songID, playlistID) + ";";
            update(query);
        }catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public ArrayList <  Integer > getLikedSongs(int userID) throws Exception {
        try {
            String query = "select songID from liked_songs where userID = " + userID + ";";
            ArrayList < ArrayList < String > > query_result = SqlCommunicate.execute(query);
            ArrayList < Integer > parsed_result = new ArrayList<>();
            for (int i = 1; i < query_result.size(); ++i) {
                parsed_result.add(Integer.parseInt(query_result.get(i).get(0)));
            }
            Collections.reverse(parsed_result);
            return parsed_result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public ArrayList <  Integer > getLikedArtistSongs(int userID, int artistID) throws Exception {
        try {
            String query = "select songID from liked_songs where userID = " + userID + ";";
            ArrayList<ArrayList<String>> query_result = SqlCommunicate.execute(query);
            ArrayList<Integer> parsed_result = new ArrayList<>(), result = new ArrayList<>();
            for (int i = 1; i < query_result.size(); ++i) {
                result.add(Integer.parseInt(query_result.get(i).get(0)));
            }
            for (Integer curSongID : result) {
                if (artistID == getArtistID(getAlbumID(curSongID))) {
                    parsed_result.add(curSongID);
                }
            }
            Collections.reverse(parsed_result);
            return parsed_result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public ArrayList <  Integer > getPlaylistSongs(int playlistID) throws Exception {
        try {
            String query = "select songID from playlist_songs where playlistID = " + playlistID + ";";
            ArrayList < ArrayList < String > > query_result = SqlCommunicate.execute(query);
            ArrayList < Integer > parsed_result = new ArrayList<>();
            for (int i = 1; i < query_result.size(); ++i) {
                parsed_result.add(Integer.parseInt(query_result.get(i).get(0)));
            }
            return parsed_result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public ArrayList <  Integer > getAllSongs() throws Exception {
        try {
            String query = "select id from songs;";
            ArrayList < ArrayList < String > > query_result = SqlCommunicate.execute(query);
            ArrayList < Integer > parsed_result = new ArrayList<>();
            for (int i = 1; i < query_result.size(); ++i) {
                parsed_result.add(Integer.parseInt(query_result.get(i).get(0)));
            }
            return parsed_result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public ArrayList <  Integer > getAllArtists() throws Exception {
        try {
            String query = "select id from artists;";
            ArrayList < ArrayList < String > > query_result = SqlCommunicate.execute(query);
            ArrayList < Integer > parsed_result = new ArrayList<>();
            for (int i = 1; i < query_result.size(); ++i) {
                parsed_result.add(Integer.parseInt(query_result.get(i).get(0)));
            }
            return parsed_result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public String getPLcnt() throws Exception {
        try {
            String query = "select count(*) from playlists;";
            String result = SqlCommunicate.execute(query).get(1).get(0);
            if (Objects.equals(result, "0")) {
                return result;
            }
            query = "select max(id) + 1 from playlists;";
            result = SqlCommunicate.execute(query).get(1).get(0);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }
    static public void createNewPlayList(int userID, String name, String access) throws Exception {
        try {
            String id = getPLcnt();
            deletePlaylistTrash(Integer.parseInt(id));
            String query = "insert into playlists values(" + id + ", " + userID + ", '" + name;
            if (Objects.equals(access, "Private")) {
                query += "', 0);";
            } else {
                query += "', 1);";
            }
            addToLibrary(userID, id);
            update(query);
        } catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public void deletePlaylistTrash(int id) throws Exception {
        try {
            String query = "delete from playlist_songs where playlistID = " + id + ";";
            update(query);
        } catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public void addToLibrary(int userID, String plID) throws Exception {
        try {
            String query = "insert into library values(" + userID + ", " + plID + ");";
            update(query);
        } catch(Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    static public ArrayList < Integer > getUsersPlaylists(int userID) throws Exception {
        try {
            String query = "select playlistID from library where id = " + userID + ";";
            ArrayList < ArrayList < String > > query_result = SqlCommunicate.execute(query);


            ArrayList < Integer > parsed_result = new ArrayList<>();
            for (int i = 1; i < query_result.size(); ++i) {
                parsed_result.add(Integer.parseInt(query_result.get(i).get(0)));
            }

            query = "select id from playlists where userID = " + userID + ";";
            ArrayList < ArrayList < String > > qr = SqlCommunicate.execute(query);
            for (int i = 1; i < qr.size(); ++i) {
                if (!parsed_result.contains(Integer.parseInt(qr.get(i).get(0)))) {
                    deletePlaylist(Integer.parseInt(qr.get(i).get(0)));
                }
            }

            Collections.reverse(parsed_result);
            return parsed_result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }
}
