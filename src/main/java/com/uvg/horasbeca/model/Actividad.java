package com.uvg.horasbeca.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Actividad {
    public final int id;
    public String titulo;
    public String descripcion;
    public int horasOtorgadas;
    public int cupoMaximo;
    public int cupoUsado;
    public LocalDate fechaActividad;
    public LocalTime horaActividad;
    public final int encargadoId;
    public boolean actividadState;

    public Actividad(){
        this.id = 0;
        this.encargadoId = 0;
    }
    
    //instancia de tabla de la db
    public Actividad(int id, String titulo, String descripcion, int horasOtorgadas, int cupoMaximo, int cupoUsado, LocalDate fechaActividad, LocalTime horaActividad, int encargadoId, boolean actividadState){
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.horasOtorgadas = horasOtorgadas;
        this.cupoMaximo = cupoMaximo;
        this.cupoUsado = cupoUsado;
        this.fechaActividad = fechaActividad;
        this.horaActividad = horaActividad;
        this.encargadoId = encargadoId;
        this.actividadState = actividadState;
    }

    //nueva act
    public Actividad(String titulo, String descripcion, int horasOtorgadas, int cupoMaximo, LocalDate fechaActividad, LocalTime horaActividad, int encargadoId){
        this.id = 0;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.horasOtorgadas = horasOtorgadas;
        this.cupoMaximo = cupoMaximo;
        this.cupoUsado = 0;
        this.fechaActividad = fechaActividad;
        this.horaActividad = horaActividad;
        this.encargadoId = encargadoId;
        this.actividadState = true;
    }
    @Override
    public String toString() {
        return String.format("Actividad{id=%d, titulo='%s', horas=%d, cupo=%d/%d, disponible=%b}", 
            id, titulo, horasOtorgadas, cupoUsado, cupoMaximo, isDisponible());
    }

    public void setTitulo(String titulo){
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion){
        this.descripcion = descripcion;
    }

    public void setHorasOtorgadas(int horasOtorgadas){
        this.horasOtorgadas = horasOtorgadas;
    }

    public void setCupoMaximo(int cupoMaximo) { 
        this.cupoMaximo = cupoMaximo; 
    }

    public void setCupoUsado(int cupoUsado){ 
        this.cupoUsado = cupoUsado; 
    }

    public void setFechaActividad(LocalDate fechaActividad){ 
        this.fechaActividad = fechaActividad; 
    }

    public void setHoraActividad(LocalTime horaActividad) { 
        this.horaActividad = horaActividad; 
    }

    public void setActividadState(boolean actividadState) { 
        this.actividadState = actividadState; 
    }

    public int getIdActividad(){ 
        return id; 
    }

    public String getTitulo(){ 
        return titulo; 
    }

    public String getDescripcion(){ 
        return descripcion;
    }

    public int getHorasOtorgadas() { 
        return horasOtorgadas;
    }
    public int getCupoMaximo() { 
        return cupoMaximo; 
    }

    public int getCupoUsado(){ 
        return cupoUsado; 
    }

    public LocalDate getFechaActividad(){
        return fechaActividad;
    }

    public LocalTime getHoraActividad(){
        return horaActividad;
    }

    public int getEncargadoId(){
        return encargadoId;
    }

    public boolean isDisponible(){ 
        return actividadState && (cupoUsado < cupoMaximo); 
    }

}

