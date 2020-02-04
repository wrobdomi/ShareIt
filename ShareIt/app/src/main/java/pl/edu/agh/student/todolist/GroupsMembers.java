package pl.edu.agh.student.todolist;

public class GroupsMembers {

    private String groupId;
    private String userId;
    private String groupName;
    private boolean favorite;

    public GroupsMembers() {
    }

    public GroupsMembers(String groupId, String userId, String groupName, boolean favorite) {
        this.groupId = groupId;
        this.userId = userId;
        this.groupName = groupName;
        this.favorite = favorite;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getUserId() {
        return userId;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
