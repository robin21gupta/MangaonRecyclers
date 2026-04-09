package com.mangaon.recycleers.util;

public class GstUtil {

    public static String getEntityTypeFromGST(String gst){

        if(gst == null || gst.length() < 15){
            return "";
        }

        String pan = gst.substring(2,12);

        char typeChar = pan.charAt(3);

        switch(typeChar){

            case 'P': return "Individual / Proprietorship";
            case 'C': return "Company";
            case 'F': return "Partnership Firm / LLP";
            case 'H': return "HUF";
            case 'A': return "Association of Persons";
            case 'T': return "Trust";
            case 'B': return "Body of Individuals";
            case 'G': return "Government";
            case 'J': return "Artificial Juridical Person";
            case 'L': return "Local Authority";

            default: return "Unknown";
        }
    }
}