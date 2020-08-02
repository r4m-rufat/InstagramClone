package empty.folder.instagram.Models;

public class UserSettings {

    private UserAccountSettings userAccountSettings;
    private User user;

    public UserSettings(UserAccountSettings userAccountSettings, User user) {
        this.userAccountSettings = userAccountSettings;
        this.user = user;
    }

    public  UserSettings(){

    }

    public UserAccountSettings getUserAccountSettings() {
        return userAccountSettings;
    }

    public void setUserAccountSettings(UserAccountSettings userAccountSettings) {
        this.userAccountSettings = userAccountSettings;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "userAccountSettings=" + userAccountSettings +
                ", user=" + user +
                '}';
    }
}
