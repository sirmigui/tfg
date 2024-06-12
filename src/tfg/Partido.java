/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfg;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 *
 * @author Miguel
 */
public class Partido {

    private Equipo local, visitante;
    private Date fechaJugado;
    private int golesLocal, golesVisit;
    private String formaciónLocal, formaciónVisit;
    private String resumen;

    public Partido(Equipo local, Equipo visitante, Date fechaJugado, int golesLocal, int golesVisit, String formaciónLocal, String formaciónVisit) {
        this.local = local;
        this.visitante = visitante;
        this.fechaJugado = fechaJugado;
        this.golesLocal = golesLocal;
        this.golesVisit = golesVisit;
        this.formaciónLocal = formaciónLocal;
        this.formaciónVisit = formaciónVisit;
    }

    public Partido(Equipo local, Equipo visitante, Date fechaJugado, int golesLocal, int golesVisit, String formaciónLocal, String formaciónVisit, String resumen) {
        this.local = local;
        this.visitante = visitante;
        this.fechaJugado = fechaJugado;
        this.golesLocal = golesLocal;
        this.golesVisit = golesVisit;
        this.formaciónLocal = formaciónLocal;
        this.formaciónVisit = formaciónVisit;
        this.resumen = resumen;
    }

    public Equipo getLocal() {
        return local;
    }

    public Equipo getVisitante() {
        return visitante;
    }

    public Date getFechaJugado() {
        return fechaJugado;
    }

    public int getGolesLocal() {
        return golesLocal;
    }

    public int getGolesVisit() {
        return golesVisit;
    }

    public String getFormaciónLocal() {
        return formaciónLocal;
    }

    public String getFormaciónVisit() {
        return formaciónVisit;
    }

    public String getResumen() {
        return resumen;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        Partido p = (Partido) obj;
        return this.local.equals(p.local)
                && this.visitante.equals(p.visitante)
                && this.fechaJugado.equals(p.fechaJugado)
                && this.golesLocal == p.golesLocal
                && this.golesVisit == p.golesVisit
                && this.formaciónLocal.equals(p.formaciónLocal)
                && this.formaciónVisit.equals(p.formaciónVisit);
    }

    @Override
    public String toString() {
        LocalDateTime ldt = fechaJugado.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        DateTimeFormatter dtt = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");

        return String.format("%s %d vs. %d %s | %s", local.getNombre(), golesLocal, golesVisit, visitante.getNombre(), ldt.format(dtt));
    }

}
