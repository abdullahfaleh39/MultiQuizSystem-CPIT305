package model;

import java.io.Serializable;

public class Quiz implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String doctorUsername;

    public Quiz(int id, String title, String doctorUsername) {
        this.id = id;
        this.title = title;
        this.doctorUsername = doctorUsername;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDoctorUsername() {
        return doctorUsername;
    }

    @Override
    public String toString() {
        return id + " - " + title + " | Doctor: " + doctorUsername;
    }
}