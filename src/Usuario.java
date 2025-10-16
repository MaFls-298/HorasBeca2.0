package model;

public abstract class Usuario {
    protected int carnetUser;
    protected String nombreUser;
    protected String emailInstitucional;
    protected String passwordUser;

    public Usuario(int carnetUser, String nombreUser, String emailInstitucional, String passwordUser){
        this.carnetUser = carnetUser;
        this.nombreUser = nombreUser;
        this.emailInstitucional = emailInstitucional;
        this.passwordUser = passwordUser;
    }

    public boolean verifLogin(String email, String password) {
        return this.emailInstitucional.equals(email) && this.passwordUser.equals(password);
    }

    public void setPasswordUser(String passwordUser){ 
        this.passwordUser = passwordUser; 
        }

    public int getCarnetUser(){
        return carnetUser;        
    }

    public String getNombreUser(){
        return nombreUser;
    }

    public String getUserEmail(){
        return emailInstitucional;
    }

    public String getPasswordUser(){
        return passwordUser;
    }

    public void resetPassword(String newPassword) {
        this.passwordUser = newPassword;
    }

    

}

