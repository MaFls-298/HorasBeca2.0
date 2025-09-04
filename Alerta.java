public class Alerta{
    private String mensajeAlerta;
    private boolean leida;
    

    public Alerta(String mensajeAlerta){
        this.mensajeAlerta = mensajeAlerta;
        this.leida = false;

    }


    public void markAsRead(){
        leida = true;
    }

    public void sendAlerta(Actividad actividad){
        
    }
    
}