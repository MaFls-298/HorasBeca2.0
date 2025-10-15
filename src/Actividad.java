import java.util.ArrayList;

public class Actividad {
    private int idActividad;
    private String tituloActividad;
    private String descripcionActividad;
    private int horasOtorgadasActividad;
    private int cupoMaximo;
    private int cupoUsado;
    private ArrayList<Alumno> alumnosInscritosList;
    private boolean stateActividad; //(disponibilidad de la actividad, 0 = no disponibel)

    public Actividad(int idActividad, String tituloActividad, String descripcionActividad, int horasOtorgadasActividad, int cupoMaximo){
        this.idActividad = idActividad;
        this.tituloActividad = tituloActividad;
        this.descripcionActividad = descripcionActividad;
        this.horasOtorgadasActividad = horasOtorgadasActividad;
        this.cupoMaximo = cupoMaximo;
        this.cupoUsado = 0;
        this.stateActividad = true;
        this.alumnosInscritosList = new ArrayList<>();
    }

    public boolean verStateActividad() {
        return cupoUsado < cupoMaximo;
    }

    public void inscribirAlumno(Alumno alumno) {
        if (verStateActividad()) {
            alumnosInscritosList.add(alumno);
            cupoUsado++;
            if (cupoUsado >= cupoMaximo) {
                this.stateActividad = false;
            }
        }
    }


    public int getIdActividad(){ 
        return idActividad; 
    }

    public String getTituloActividad(){ 
        return tituloActividad; 
    }

    public String getDescripcionActividad(){ 
        return descripcionActividad;
    }

    public int getHorasOtorgadasActividad() { 
        return horasOtorgadasActividad;
    }
    public int getCupoMaximo() { 
        return cupoMaximo; 
    }

    public int getCupoUsado(){ 
        return cupoUsado; 
    }

    public boolean isDisponible(){ 
        return stateActividad; 
    }
}

/**
 * Autor: Estefanie 251610
 * Programa: Horas Beca
 * Fecha de creación: 31/08/2025
 */
