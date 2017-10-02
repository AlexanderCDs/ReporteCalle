package com.example.alexander.reportecalle;

import android.media.Image;

/**
 * Created by Alexander on 2/10/2017.
 */

public class ReportInformation
{
    public String direccion;
    public String comentario;
    public String image;

    public ReportInformation()
    {

    }

    public ReportInformation(String direccion, String comentario, String image) {
        this.direccion = direccion;
        this.comentario = comentario;
        this.image = image;
    }
}
