package net.mahmutkocas;

public enum Language {
    TR(
            "Bu komutu sadece oyuncular kullanabilir.",
            "Giriş yapınız!",
            "Şifreler uyuşmuyor.",
            "Şifre değişimi sırasında hata oluştu. Eski şifreni doğru girdin mi?",
            "Şifre başarıyla değişti.",
            "Zaten kayıt olmuşsunuz.",
            "Kayıt sırasında hata oluştu.",
            "Kayıt başarılı! Giriş yapınız.",
            "Önce kayıt olunuz.",
            "Giriş sırasında hata oluştu, şifreyi kontrol ediniz.",
            "Giriş başarılı!"
    ),
    EN(
            "This command is player only",
            "Please login!",
            "Passwords do not match!",
            "Error changing password! Check old password!",
            "Success! Password has been changed!",
            "This username already exists!",
            "Error can not register!",
            "Success! Please login!",
            "You need to register. Use /register",
            "Error! Check your password!",
            "Login success!"
    ),
    ;

    public final String onlyUsedByPlayers;
    public final String pleaseLogin;
    public final String passwordMismatch;
    public final String passwordChangeError;
    public final String passwordChangeSuccess;
    public final String alreadyRegistered;
    public final String registerError;
    public final String registerSuccess;
    public final String registerFirst;
    public final String loginErrorCheckPassword;
    public final String loginSuccess;

    Language(String onlyUsedByPlayers, String pleaseLogin, String passwordMismatch, String passwordChangeError, String passwordChangeSuccess, String alreadyRegistered, String registerError, String registerSuccess, String registerFirst, String loginErrorCheckPassword, String loginSuccess) {
        this.onlyUsedByPlayers = onlyUsedByPlayers;
        this.pleaseLogin = pleaseLogin;
        this.passwordMismatch = passwordMismatch;
        this.passwordChangeError = passwordChangeError;
        this.passwordChangeSuccess = passwordChangeSuccess;
        this.alreadyRegistered = alreadyRegistered;
        this.registerError = registerError;
        this.registerSuccess = registerSuccess;
        this.registerFirst = registerFirst;
        this.loginErrorCheckPassword = loginErrorCheckPassword;
        this.loginSuccess = loginSuccess;
    }
}
