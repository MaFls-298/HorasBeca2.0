public class Encargado extends Usuario{
    private String departamento;
    
    public Encargado(int carnetUser, String nombreUser, String emailInstitucional, String passwordUser, String departamento){
        super(carnetUser, nombreUser, emailInstitucional, passwordUser);
        this.departamento = departamento; 
        
    }

    
    public String getDepartamento(){
        return departamento;
    } 

    public void publicarActividad(Actividad actividad){

    }

    public void validadAsistencia(Alumno alumno, Actividad actividad){

    }



    /*public void publicarActividad(String titulo) {
        System.out.println("Actividad publicada: " + titulo);
    }

    public void validarAsistencia(Alumno alumno, int horas) {
        alumno.agregarHoras(horas);
        System.out.println("Se agregaron " + horas + " horas a " + alumno.getUserInfo());
    }*/
}