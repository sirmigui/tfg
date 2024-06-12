/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfg;

import java.util.Date;
import java.util.Objects;

/**
 *
 * @author migui
 */
public class Equipo {

    private long id;
    private final String nombre;
    private String ciudad;
    private String urlImg;
    private Date fechaFundado;

    public Equipo(long id, String nombre, String ciudad, String urlImg, Date fechaFundado) {
        this.id = id;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.urlImg = urlImg;
        this.fechaFundado = fechaFundado;
    }

    public Equipo(String nombre, String ciudad, String urlImg, Date fechaFundado) {
        this.id = 0;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.urlImg = urlImg;
        this.fechaFundado = fechaFundado;
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

    public String getCiudad() {
        return ciudad;
    }

    public String getUrlImg() {
        return urlImg;
    }

    public Date getFechaFundado() {
        return fechaFundado;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        Equipo e = (Equipo) obj;
        return this.nombre.equals(e.nombre)
                && this.ciudad.equals(e.ciudad)
                && this.fechaFundado.equals(e.fechaFundado);

    }

    @Override
    public String toString() {
        return this.nombre;
    }

}
