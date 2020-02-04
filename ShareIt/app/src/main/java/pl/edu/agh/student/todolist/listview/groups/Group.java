package pl.edu.agh.student.todolist.listview.groups;

public class Group {
    private String userId;
    private String name;
    private boolean isFavorite;

    public Group(String id, String name) {
        this.userId = id;
        this.name = name;
        this.isFavorite = false;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String id) {
        this.userId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
