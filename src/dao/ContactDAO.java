package dao;

import entity.Contact;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ContactDAO {

    private static ContactDAO instance;
    private final ObservableList<Contact> contactObservableList;
    private final DateTimeFormatter formatter;
    private final Path CONTACTS_DIR = Paths.get(".", "data");
    private final Path CONTACTS_FILE = Paths.get(CONTACTS_DIR.toAbsolutePath().toString(), "contact.txt");

    private ContactDAO() {
        contactObservableList = FXCollections.observableArrayList();
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public synchronized static ContactDAO getInstance() {
        if (instance == null) {
            instance = new ContactDAO();
        }

        return instance;
    }

    public ObservableList<Contact> getContactObservableList() {
        return contactObservableList;
    }

    /**
     * đọc các contacts từ file vào list
     *
     * @throws IOException
     */
    public void loadContactsFromFile() throws IOException {
        // tạo file và thư mục nếu chưa có
        if (Files.notExists(CONTACTS_DIR)) {
            Files.createDirectory(CONTACTS_DIR);
        }
        if (Files.notExists(CONTACTS_FILE)) {
            Files.createFile(CONTACTS_FILE);
            return;
        }

        contactObservableList.clear();

        BufferedReader reader = Files.newBufferedReader(CONTACTS_FILE);

        Scanner scanner = new Scanner(reader);

        try (scanner) {

            scanner.useDelimiter(":");

            while (scanner.hasNextLine()) {
                String firstName = scanner.next().trim();
                scanner.skip(scanner.delimiter());

                String lastName = scanner.next().trim();
                scanner.skip(scanner.delimiter());

                String phone = scanner.next().trim();
                scanner.skip(scanner.delimiter());

                String email = scanner.next().trim();
                scanner.skip(scanner.delimiter());

                LocalDate dob = LocalDate.parse(scanner.next().trim(), formatter);
                scanner.skip(scanner.delimiter());

                String group = scanner.next().trim();
                scanner.skip(scanner.delimiter());

                boolean active = Boolean.parseBoolean(scanner.nextLine().trim());

                contactObservableList.add(new Contact(firstName, lastName, phone, email, dob, group, active));

            }
        }
    }


    /**
     * lưu List contactObservableList vào file
     *
     * @throws IOException
     */
    public void saveContactsToFile() throws IOException {

        BufferedWriter writer = Files.newBufferedWriter(CONTACTS_FILE);

        try (writer) {
            for (Contact contact : contactObservableList) {
                writer.write(contact.toString());
            }
        }
    }


}
