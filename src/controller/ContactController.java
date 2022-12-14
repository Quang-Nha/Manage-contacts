package controller;

import dao.ContactDAO;
import dao.GroupDAO;
import entity.Contact;
import entity.Group;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;

public class ContactController {
    @FXML
    public Button btnSaveToFile;
    @FXML
    private TextField search;
    @FXML
    private ComboBox<Group> cbGroup;
    @FXML
    private TableView<Contact> tblContact;
    @FXML
    private TableColumn<String, Contact> fname;
    @FXML
    private TableColumn<String, Contact> lname;
    @FXML
    private TableColumn<String, Contact> phone;
    @FXML
    private TableColumn<String, Contact> email;
    @FXML
    private TableColumn<String, Contact> dob;
    @FXML
    private TableColumn<String, Contact> group;
    @FXML
    private TableColumn<Boolean, Contact> active;

    private FilteredList<Contact> filteredList;

    private List<Contact> updatingContacts;
    private List<String> updatingGroup;
    private ObservableList<UpdateContactController> updateContactControllerList;
    private ObservableList<AddContactController> addContactControllerList;

    private ObservableList<Contact> contactObservableList;
    private ObservableList<Group> groupObservableList;
    private ContextMenu contextMenu;

    public List<Contact> getUpdatingContacts() {
        return updatingContacts;
    }

    public List<String> getUpdatingGroup() {
        return updatingGroup;
    }

    public ObservableList<Group> getGroupObservableList() {
        return groupObservableList;
    }

    public ObservableList<UpdateContactController> getUpdateContactControllerList() {
        return updateContactControllerList;
    }

    public ObservableList<AddContactController> getAddContactControllerList() {
        return addContactControllerList;
    }

    public TableView<Contact> getTblContact() {
        return tblContact;
    }

    public ComboBox<Group> getCbGroup() {
        return cbGroup;
    }

    @FXML
    private void initialize() {
        // kh???i t???o list c??c contacts, groups ??ang update
        // kh???i t???o list ch???a c??c controller c???a c??c c???a s??? add v?? update ??ang m???
        updatingContacts = new ArrayList<>();
        updatingGroup = new ArrayList<>();
        updateContactControllerList = FXCollections.observableArrayList();
        addContactControllerList = FXCollections.observableArrayList();

        // g??n c???t v???i thu???c t??nh t????ng ???ng c???a Contact
        fname.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lname.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
        dob.setCellValueFactory(new PropertyValueFactory<>("dob"));
        group.setCellValueFactory(new PropertyValueFactory<>("group"));
        active.setCellValueFactory(new PropertyValueFactory<>("active"));

        // ch??? cho tblContact ch???n 1 h??ng
        this.tblContact.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        contactObservableList = ContactDAO.getInstance().getContactObservableList();

        // r??ng bu???c filteredList v???i ObservableList<Contact> ???? ???????c load t??? file
        filteredList = new FilteredList<>(contactObservableList);

        // r??ng bu???c sortedList v???ifilteredList, x???p x???p theo active
        SortedList<Contact> sortedList = new SortedList<>(filteredList, new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                if (o1.isActive() && !o2.isActive()) {
                    return -1;
                } else if (!o1.isActive() && o2.isActive()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        // r??ng bu???c tblContact v???i sortedList
        tblContact.setItems(sortedList);
        // ch???n h??ng ?????u ti??n
        tblContact.getSelectionModel().selectFirst();

        groupObservableList = GroupDAO.getInstance().getGroupObservableList();
        // r??ng bu???c cbGroup v???i ObservableList<Group> ???? ???????c load t??? file
        cbGroup.setItems(groupObservableList);
        // cho cbGroup ch???n h??ng ?????u ti??n
        cbGroup.getSelectionModel().selectFirst();

        // b???t s??? ki???n thay ?????i group th?? g???i h??m t??m ki???m
        cbGroup.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Group>() {
            @Override
            public void changed(ObservableValue<? extends Group> observableValue, Group group, Group t1) {

                searchContact();
            }
        });

        // b???t s??? ki???n khi nh???n b??n ph??m tr??n ?? t??m ki???m th?? g???i h??m t??m ki???m
        search.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                searchContact();
            }
        });

        // taoj contextMenu ch???a 3 n??t th??m, update, x??a v?? th??m s??? ki???n cho 3 n??t ???? g???i c??c h??m t????ng ???ng
        contextMenu = new ContextMenu();
        MenuItem addItem = new MenuItem("Add");
        addItem.setOnAction(event -> {
            try {
                addContact(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        MenuItem updateItem = new MenuItem("Update");
        updateItem.setOnAction(event -> {
            try {
                updateContact(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            try {
                deleteContact();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        contextMenu.getItems().addAll(addItem, updateItem, deleteItem);

        setRowFactory();

        // b???t s??? ki???n nh???n n??t delete tr??n tblContact th?? g???i h??m x??a
        tblContact.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                    try {
                        deleteContact();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void setRowFactory() {
        // setRowFactory, c??c h??ng g???n th??m ContextMenu
        tblContact.setRowFactory(new Callback<TableView<Contact>, TableRow<Contact>>() {
            @Override
            // tr??? v??? c??i ?????t m???i cho h??ng
            public TableRow<Contact> call(TableView<Contact> contactTableView) {
                // t???o m???i object c???a h??ng
                TableRow<Contact> tableRow = new TableRow<>() {
                    @Override
                    // s???a l???i ?????nh d???ng c??c cell/h??ng theo gi?? tr??? c??c Contact n?? ch???a
                    // n???u Contact c?? disable = true th?? ?????i m??u n???n, kh??ng th?? ????? m??u m???c ?????nh
                    protected void updateItem(Contact contact, boolean empty) {
                        super.updateItem(contact, empty);
                        // n???u c??c ?? r???ng th?? ????? m??u n???n m???c ?????nh
                        if (empty) {
                            setStyle("");
                        }
                        // n???u Contact c?? disable = false v?? ch??a ch???n tr??n b???ng th?? ?????i m??u n???n, c??n ??ang ch???n
                        // th?? ????? m??u n???n ????? 50%
                        // t???t c??? c??c tr?????ng h???p d?????i c??? ch??? 16
                        else if (!contact.isActive()) {
                            if (isSelected()) {
                                setStyle("-fx-background-color: rgba(255,0,0,0.5); -fx-font-size: 16");
                            } else {
                                setStyle("-fx-background-color: pink;-fx-font-size: 16");
                            }
                        } else {
                            setStyle("-fx-font-size: 16");
                        }

                    }
                };

                // b???t s??? ki???n r???ng cho h??ng, n???u h??ng chuy???n sang r???ng th?? x??a ContextMenu ng?????c l???i th?? th??m v??o
                tableRow.emptyProperty().addListener((observableValue, wasEmpty, isNowEmpty) -> {
                    if (wasEmpty) {
                        tableRow.setContextMenu(contextMenu);
                    } else {
                        tableRow.setContextMenu(null);
                    }
                });

                return tableRow;
            }
        });
    }

    /**
     * t??m ki???m v?? hi???n th??? nh???ng contact theo t??? t??m ki???m v?? group
     */
    public void searchContact() {
        // n???u ch??a nh???p g?? v?? ch???n group all th?? tr??? v??? true t???c l?? Contact OK s??? th??m v??o filter List
        // n???u ch??? ch???n group all th?? tr??? v??? true v???i nh???ng contact.toString2 ch??? th?????ng c?? ch???a t??? c???n t??m
        // n???u ch??? ch??a nh???p g?? th?? tr??? v??? true v???i nh???ng contact c?? group tr??ng v???i group ??ang ch???n
        // c??n l???i th?? l???c theo c??? 2 ti??u ch??

        if (groupObservableList.size() > 0) {
            Predicate<Contact> searchPredicate = new Predicate<Contact>() {

                @Override
                public boolean test(Contact contact) {
                    String group = "All";
                    if (cbGroup.getSelectionModel().getSelectedItem() != null) {
                        group = cbGroup.getSelectionModel().getSelectedItem().getName();
                    }

                    final String searchContact = search.getText().toLowerCase(Locale.ROOT);

                    String contactGroup = contact.getGroup();
                    String contactToString = contact.toString2().toLowerCase(Locale.ROOT);

                    // n???u ch??a nh???p g?? v?? ch???n group all th?? tr??? v??? true t???c l?? Contact OK s??? th??m v??o filter List
                    if (group.equalsIgnoreCase("All") && searchContact.isBlank()) {
                        return true;
                    }
                    // n???u ch??? ch???n group all th?? tr??? v??? true v???i nh???ng contact.toString2 ch??? th?????ng c?? ch???a t??? c???n t??m
                    else if (group.equalsIgnoreCase("All") && contactToString.contains(searchContact)) {
                        return true;
                    }
                    // n???u ch??? ch??a nh???p g?? th?? tr??? v??? true v???i nh???ng contact c?? group tr??ng v???i group ??ang ch???n
                    else if (searchContact.isBlank() && contactGroup.equals(group)) {
                        return true;
                    }
                    // c??n l???i th?? l???c theo c??? 2 ti??u ch??
                    else return contactToString.contains(searchContact) && group.equals(contactGroup);
                }
            };

            // set b??? l???c cho filter List
            filteredList.setPredicate(searchPredicate);
        }

    }

    /**
     * m??? c???a s??? th??m contact v?? truy???n controller n??y cho n??
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void addContact(ActionEvent event) throws IOException {
        // g???i h??m m??? c???a s??? v?? tr??? v??? controller c???a c???a s???
        // ???????ng d???n ph???n ?????u ch???a "/" l?? t??nh t??? th?? m???c m??u xanh ch???a t???t c??? code java t??n "src"
        AddContactController controller = (AddContactController) OpenWindow.open("/ui/addContact.fxml",
                "Add new Contact");
        // truy???n controller c???a c??? s??? n??y cho c???a s???
        controller.setContactController(this);

        // th??m controller n??y v??o list ??ang c??c controller ??ang add
        addContactControllerList.add(controller);
    }


    /**
     * m??? c???a s??? qu???n l?? groups
     * l???y controller c???a c???a s??? v?? truy???n controller c???a class n??y cho controller c???a c???a s??? ????
     *
     * @throws Exception
     */
    @FXML
    private void groupPanel(ActionEvent event) throws IOException {
        // g???i h??m m??? c???a s??? v?? tr??? v??? controller c???a c???a s???
        GroupController controller = (GroupController) OpenWindow.open("/ui/group.fxml",
                "Group a Management");
        // truy???n controller c???a c??? s??? n??y cho c???a s???
        controller.initialize(this);
    }

    /**
     * ch???nh s???a gi?? tr??? Contact
     *
     * @throws Exception
     */
    @FXML
    private void updateContact(ActionEvent event) throws Exception {
        Contact updateContact = tblContact.getSelectionModel().getSelectedItem();
        if (updateContact != null) {
            // th??m updateContact v??o list c??c contacts ??ang update ????? n?? ko b??? x??a
            updatingContacts.add(updateContact);

            // group l??m t????ng t??? tr??n
            updatingGroup.add(updateContact.getGroup());

            // g???i h??m m??? c???a s??? v?? tr??? v??? controller c???a c???a s???
            UpdateContactController controller = (UpdateContactController) OpenWindow.open("/ui/updateContact.fxml",
                    "Update Contact");

            // th??m controller n??y v??o list ??ang c??c controller ??ang update
            updateContactControllerList.add(controller);

            // truy???n controller c???a c??? s??? n??y cho c???a s???
            controller.initialize(this);

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Report");
            alert.setContentText("Select a Contact to update");
            alert.show();
        }

    }

    /**
     * x??a contact trong tblContact
     *
     * @throws Exception
     */
    @FXML
    private void deleteContact() throws Exception {
        // l???y contact ??ang ch???n
        Contact selectedContact = tblContact.getSelectionModel().getSelectedItem();

        Alert alert;

        //n???u ???? ch???n th?? xem contact n??y c?? trong list c??c contacts ??ang update ko,
        // n???u ch??a ch???n th?? y??u c???u ch???n l???i
        if (selectedContact != null) {

            // n???u contact ??ang ch???n c?? trong list contacts ??ang update th?? th??ng b??o h???y
            // n???u kh??ng th?? y??u c???u x??c nh???n x??a ?????ng ?? th?? x??a
            if (updatingContacts.contains(selectedContact)) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Report");
                alert.setContentText("Updating contact, can't delete");
                alert.show();
                return;
            }

            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setContentText("Do you wanna delete selected contact?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                contactObservableList.remove(selectedContact);
                ContactDAO.getInstance().saveContactsToFile();
            }
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Report");
            alert.setContentText("Select a Contact to delete");
            alert.show();
        }


    }

    /**
     * l??u c??c contact c???a nh??m ??ang ch???n v??o file
     *
     * @param event
     * @throws IOException
     * @throws InterruptedException
     */
    @FXML
    private void saveToFile(ActionEvent event) throws IOException, InterruptedException {

        // l???y t??n group ??ang ch???n
        String group = cbGroup.getSelectionModel().getSelectedItem().getName();

        // n???u nh??m l?? all th?? th??ng b??o ko l??u v?? tho??t
        if (group.equalsIgnoreCase("all")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("INFORMATION");
            alert.setContentText("Select a group to save");
            alert.show();
            return;
        }

        // y??u c???u x??c nh???n l??u v???i link th??ng b??o
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("CONFIRMATION");
        alert.setHeaderText("Save all contacts in the group " + group + " to a file " + group + ".txt?");
        Optional<ButtonType> result = alert.showAndWait();

        // n???u ?????ng ?? th?? ti???n h??nh l??u
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // t???o t??n file l?? t??n group + txt
            String fileName = group + ".txt";

            // t???o tr??nh ch???n th?? m???c
            DirectoryChooser directoryChooser = new DirectoryChooser();
            // set n??i m??? ?????u ti??n c???a th?? m???c l?? n??i ch???a d??? ??n
            directoryChooser.setInitialDirectory(new File(Paths.get(".").toString()));
            // set title cho c???a s??? ch???n file
            directoryChooser.setTitle("Select the folder where you want to save the file " + group + ".txt");

            // l???y ?????a ch??? th?? m???c c???n l??u ng?????i d??ng ch???n g??n v??o File
            // truy???n v??o window c???a c???a s??? n??y ????? kh??a c???a s??? n??y ko th??? thao t??c ?????n khi ????ng c???a s??? ch???n th?? m???c
            File folder = directoryChooser.showDialog(tblContact.getScene().getWindow());

            // n???u ?????a ch??? ko null t???c ng?????i d??ng ???? ch???n m?? ko ???n h???y
            if (folder != null) {
                // t???o file1 c???ng th??m t??n file v??o ?????a ch??? th?? m???c l??m ?????a ch??? c???a file c???n l??u
                File file1 = new File(folder.toString(), fileName);

                // t???o b??? ghi ghi t???t c??? c??c contacts trong nh??m ??ang hi???n th??? v??o file nh??? filteredList
                BufferedWriter writer = new BufferedWriter(new FileWriter(file1));
                try (writer) {
                    for (Contact contact : filteredList) {
                        writer.write(contact.toString());
                    }
                }

                // m??? th?? m???c v?? file v???a ghi v??o
                Desktop.getDesktop().open(folder);
                Thread.sleep(1000);
                Desktop.getDesktop().open(file1);
                Thread.sleep(1000);
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("INFORMATION");
                alert.setHeaderText("saved to address: \n" + file1);
                alert.show();
            }

        }

    }
}
