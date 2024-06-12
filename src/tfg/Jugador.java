/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfg;

import java.io.File;

/**
 *
 * @author migui
 */
public class Jugador {

    private long id;
    private final String nombre;
    private String elemento, posición, curso;
    private String img;
    private int dorsal;
    private long idEquipo;

    public Jugador(long id, String nombre, String elemento, String posición, String curso, String img, int dorsal, long idEquipo) {
        this.id = id;
        this.nombre = nombre;
        this.elemento = elemento;
        this.posición = posición;
        this.curso = curso;
        this.dorsal = dorsal;
        this.idEquipo = idEquipo;
        this.img = img;
    }

    public Jugador(String nombre, String elemento, String posición, String curso, String img, int dorsal, long idEquipo) {
        this.id = 0;
        this.nombre = nombre;
        this.elemento = elemento;
        this.posición = posición;
        this.curso = curso;
        this.dorsal = dorsal;
        this.idEquipo = idEquipo;
        this.img = img;

    }

    public long getId() {
        return id;
    }

    public void setId(long newId) {
        this.id = newId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getElemento() {
        return elemento;
    }

    public String getPosición() {
        return posición;
    }

    public String getCurso() {
        return curso;
    }

    public int getDorsal() {
        return dorsal;
    }

    public long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(long newIdEquipo) {
        this.idEquipo = newIdEquipo;
    }

    public void setElemento(String elemento) {
        this.elemento = elemento;
    }

    public void setPosición(String posición) {
        this.posición = posición;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public void setDorsal(int dorsal) {
        this.dorsal = dorsal;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        Jugador j = (Jugador) obj;
        return this.nombre.equals(j.nombre)
                && this.dorsal == j.dorsal
                && this.elemento.equals(j.elemento)
                && this.curso.equals(j.curso)
                && this.posición.equals(j.posición)
                && this.idEquipo == j.idEquipo;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s", this.nombre, this.elemento, this.posición);
    }

}
