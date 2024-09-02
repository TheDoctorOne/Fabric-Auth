package net.mahmutkocas;

public interface IAuthHandler {

    boolean userExists(String username);
    boolean registerUpdate(String username, String password, boolean isUpdate);
    boolean login(String username, String password);
    boolean changePassword(String username, String oldPassword, String newPassword);
    boolean reloadUsers();
    boolean save();

}
