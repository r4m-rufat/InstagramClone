package empty.folder.instagram.Models;

import java.util.List;

public class Comment {

    private String user_id;
    private String comment;
    private List<Like> likes;
    private String date;

    public Comment(){

    }

    public Comment(String user_id, String comment, List<Like> likes, String date) {
        this.user_id = user_id;
        this.comment = comment;
        this.likes = likes;
        this.date = date;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "user_id='" + user_id + '\'' +
                ", comment='" + comment + '\'' +
                ", likes=" + likes +
                ", date='" + date + '\'' +
                '}';
    }
}
