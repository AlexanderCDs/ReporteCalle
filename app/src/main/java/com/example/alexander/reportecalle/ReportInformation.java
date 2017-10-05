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

    public ReportInformation()
    {

    }

    public ReportInformation(String correo, String direccion, String comentario, String image, String estado) {
        this.correo = correo;
        this.direccion = direccion;
        this.comentario = comentario;
        this.image = image;
        this.estado = estado;
    }
}
