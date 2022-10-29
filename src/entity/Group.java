package entity;

import dao.ContactDAO;

import java.util.List;
import java.util.Objects;

public class Group {



    private String name;

    public  Group(String name) {
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

    }

    @Override
    public String toString() {
        return String.format("%-20s\n",name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || name == null) return false;
        return name.equals(((Group) obj).getName());
    }
}
