package hr.fer.rassus.lab;

import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String type;
    private String content;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public Message(int id, String type, String content) {
        this.id = id;
        this.type = type;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Id: " + id + "   Message: " + content;
    }
}
