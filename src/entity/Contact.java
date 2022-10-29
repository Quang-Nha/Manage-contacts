package entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Contact{

    private String firstName, lastName;
    private String phone, email;
    private LocalDate dob;
    private String group;
    private boolean active;

    public Contact(String firstName, String lastName, String phone, String email, LocalDate dob, String group, boolean active) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.dob = dob;
        this.group = group;
        this.active = active;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {

        return String.format("%-15s:%-15s:%-15s:%-25s:%-15s:%-15s:%-10s\n", firstName, lastName, phone, email,
                dob.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                group, active);
    }

    public String toString2() {
        return String.format("%-15s:%-15s:%-15s:%-25s:%-15s:%-15s:%-10s\n", firstName, lastName, phone, email, dob, group, active);
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }
}

