package com.example.alexander.reportecalle;

/**
 * Created by Alexander on 2/10/2017.
 */

public class ReportInformation
{
    public String id;
    public String correo;
    public String direccion;
    public String comentario;
    public String image;
    public String estado;
    public String fecha;
    public String hora;
    public String token;

    public ReportInformation(String id, String correo, String direccion, String comentario, String image, String estado, String fecha, String hora, String token)
    {
        this.id = id;
        this.correo = correo;
        this.direccion = direccion;
        this.comentario = comentario;
        this.image = image;
        this.estado = estado;
        this.fecha = fecha;
        this.hora = hora;
        this.token = token;
    }

    public ReportInformation()
    {
        super();
    }

    @Override
    public String toString() {
        return "[" +
                "\nId: " + this.id +
                "\nCorreo: " + this.correo +
                "\nDirección: " + this.direccion +
                "\nComentario: " + this.comentario +
                "\nImagen: " + this.image +
                "\nEstado: " + this.estado +
                "\nFecha: " + this.fecha +
                "\nHora: " + this.hora +
                "\nToken: " + this.token +
                "\n]";
    }
}
