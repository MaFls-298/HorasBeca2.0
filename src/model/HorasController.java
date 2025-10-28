package model;

import modelo.Alumno;
import java.util.HashMap;
import java.util.Map; 

public class HorasController { 

    private Map<Integer, Integer> horasAcumuladas;

    public HorasController() {
        horasAcumuladas = new HashMap<>();
    }

    public void agregarHoras(Alumno alumno, int horas) {
        if (alumno == null || horas <= 0) return;

        int carnet = alumno.getCarnetUser();
        int total = horasAcumuladas.getOrDefault(carnet, 0) + horas;

        horasAcumuladas.put(carnet, total);
        alumno.setHorasAcumuladas(total);
        alumno.setHorasBecaPendiente(Math.max(0, alumno.getHorasBecaPendiente() - horas));
    }

    public int consultarHoras(Alumno alumno) {
        if (alumno == null) return 0;
        return horasAcumuladas.getOrDefault(alumno.getCarnetUser(), 0);
    }

    public int horasPendientes(Alumno alumno) {
        if (alumno == null) return 0;
        return alumno.getHorasBecaPendiente();
    }
} 