package com.example.alexander.reportecalle;

/**
 * Created by Alexander on 2/10/2017.
 */

public class ReportInformation
{
    public String correo;
    public String direccion;
    public String comentario;
    public String image;
    public String estado;
    public String fecha;
    public String hora;
    public String token;

    public ReportInformation()
    {
        this.correo = "";
        this.direccion = "";
        this.comentario = "";
        this.image = "";
        this.estado = "";
        this.fecha = "";
        this.hora = "";
        this.token = "";
    }

    public ReportInformation(String correo, String direccion, String comentario, String image, String estado, String fecha, String hora, String token) {
        this.correo = correo;
        this.direccion = direccion;
        this.comentario = comentario;
        this.image = image;
        this.estado = estado;
        this.fecha = fecha;
        this.hora = hora;
        this.token = token;
    }
}
