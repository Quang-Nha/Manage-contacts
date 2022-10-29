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
        // khởi tạo list các contacts, groups đang update
        // khởi tạo list chứa các controller của các cửa sổ add và update đang mở
        updatingContacts = new ArrayList<>();
        updatingGroup = new ArrayList<>();
        updateContactControllerList = FXCollections.observableArrayList();
        addContactControllerList = FXCollections.observableArrayList();

        // gán cột với thuộc tính tương ứng của Contact
        fname.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lname.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
        dob.setCellValueFactory(new PropertyValueFactory<>("dob"));
        group.setCellValueFactory(new PropertyValueFactory<>("group"));
        active.setCellValueFactory(new PropertyValueFactory<>("active"));

        // chỉ cho tblContact chọn 1 hàng
        this.tblContact.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        contactObservableList = ContactDAO.getInstance().getContactObservableList();

        // ràng buộc filteredList với ObservableList<Contact> đã được load từ file
        filteredList = new FilteredList<>(contactObservableList);

        // ràng buộc sortedList vớifilteredList, xắp xếp theo active
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

        // ràng buộc tblContact với sortedList
        tblContact.setItems(sortedList);
        // chọn hàng đầu tiên
        tblContact.getSelectionModel().selectFirst();

        groupObservableList = GroupDAO.getInstance().getGroupObservableList();
        // ràng buộc cbGroup với ObservableList<Group> đã được load từ file
        cbGroup.setItems(groupObservableList);
        // cho cbGroup chọn hàng đầu tiên
        cbGroup.getSelectionModel().selectFirst();

        // bắt sự kiện thay đổi group thì gọi hàm tìm kiếm
        cbGroup.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Group>() {
            @Override
            public void changed(ObservableValue<? extends Group> observableValue, Group group, Group t1) {

                searchContact();
            }
        });

        // bắt sự kiện khi nhấn bàn phím trên ô tìm kiếm thì gọi hàm tìm kiếm
        search.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                searchContact();
            }
        });

        // taoj contextMenu chứa 3 nút thêm, update, xóa và thêm sự kiện cho 3 nút đó gọi các hàm tương ứng
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

        // bắt sự kiện nhấn nút delete trên tblContact thì gọi hàm xóa
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
        // setRowFactory, các hàng gắn thêm ContextMenu
        tblContact.setRowFactory(new Callback<TableView<Contact>, TableRow<Contact>>() {
            @Override
            // trả về cài đặt mới cho hàng
            public TableRow<Contact> call(TableView<Contact> contactTableView) {
                // tạo mới object của hàng
                TableRow<Contact> tableRow = new TableRow<>() {
                    @Override
                    // sửa lại định dạng các cell/hàng theo giá trị các Contact nó chứa
                    // nếu Contact có disable = true thì đổi màu nền, không thì để màu mặc định
                    protected void updateItem(Contact contact, boolean empty) {
                        super.updateItem(contact, empty);
                        // nếu các ô rỗng thì để màu nền mặc định
                        if (empty) {
                            setStyle("");
                        }
                        // nếu Contact có disable = false và chưa chọn trên bảng thì đổi màu nền, còn đang chọn
                        // thì để màu nền đỏ 50%
                        // tất cả các trường hợp dưới cỡ chữ 16
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

                // bắt sự kiện rỗng cho hàng, nếu hàng chuyển sang rỗng thì xóa ContextMenu ngược lại thì thêm vào
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
     * tìm kiếm và hiển thị những contact theo từ tìm kiếm và group
     */
    public void searchContact() {
        // nếu chưa nhập gì và chọn group all thì trả về true tức là Contact OK sẽ thêm vào filter List
        // nếu chỉ chọn group all thì trả về true với những contact.toString2 chữ thường có chứa từ cần tìm
        // nếu chỉ chưa nhập gì thì trả về true với những contact có group trùng với group đang chọn
        // còn lại thì lọc theo cả 2 tiêu chí

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

                    // nếu chưa nhập gì và chọn group all thì trả về true tức là Contact OK sẽ thêm vào filter List
                    if (group.equalsIgnoreCase("All") && searchContact.isBlank()) {
                        return true;
                    }
                    // nếu chỉ chọn group all thì trả về true với những contact.toString2 chữ thường có chứa từ cần tìm
                    else if (group.equalsIgnoreCase("All") && contactToString.contains(searchContact)) {
                        return true;
                    }
                    // nếu chỉ chưa nhập gì thì trả về true với những contact có group trùng với group đang chọn
                    else if (searchContact.isBlank() && contactGroup.equals(group)) {
                        return true;
                    }
                    // còn lại thì lọc theo cả 2 tiêu chí
                    else return contactToString.contains(searchContact) && group.equals(contactGroup);
                }
            };

            // set bộ lọc cho filter List
            filteredList.setPredicate(searchPredicate);
        }

    }

    /**
     * mở cửa sổ thêm contact và truyền controller này cho nó
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void addContact(ActionEvent event) throws IOException {
        // gọi hàm mở của sổ và trả về controller của cửa sổ
        // đường dẫn phần đầu chứa "/" là tính từ thư mục màu xanh chứa tất cả code java tên "src"
        AddContactController controller = (AddContactController) OpenWindow.open("/ui/addContact.fxml",
                "Add new Contact");
        // truyền controller của cả sổ này cho cửa sổ
        controller.setContactController(this);

        // thêm controller này vào list đang các controller đang add
        addContactControllerList.add(controller);
    }


    /**
     * mở cửa sổ quản lý groups
     * lấy controller của cửa sổ và truyền controller của class này cho controller của cửa sổ đó
     *
     * @throws Exception
     */
    @FXML
    private void groupPanel(ActionEvent event) throws IOException {
        // gọi hàm mở của sổ và trả về controller của cửa sổ
        GroupController controller = (GroupController) OpenWindow.open("/ui/group.fxml",
                "Group a Management");
        // truyền controller của cả sổ này cho cửa sổ
        controller.initialize(this);
    }

    /**
     * chỉnh sửa giá trị Contact
     *
     * @throws Exception
     */
    @FXML
    private void updateContact(ActionEvent event) throws Exception {
        Contact updateContact = tblContact.getSelectionModel().getSelectedItem();
        if (updateContact != null) {
            // thêm updateContact vào list các contacts đang update để nó ko bị xóa
            updatingContacts.add(updateContact);

            // group làm tương tự trên
            updatingGroup.add(updateContact.getGroup());

            // gọi hàm mở của sổ và trả về controller của cửa sổ
            UpdateContactController controller = (UpdateContactController) OpenWindow.open("/ui/updateContact.fxml",
                    "Update Contact");

            // thêm controller này vào list đang các controller đang update
            updateContactControllerList.add(controller);

            // truyền controller của cả sổ này cho cửa sổ
            controller.initialize(this);

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Report");
            alert.setContentText("Select a Contact to update");
            alert.show();
        }

    }

    /**
     * xóa contact trong tblContact
     *
     * @throws Exception
     */
    @FXML
    private void deleteContact() throws Exception {
        // lấy contact đang chọn
        Contact selectedContact = tblContact.getSelectionModel().getSelectedItem();

        Alert alert;

        //nếu đã chọn thì xem contact này có trong list các contacts đang update ko,
        // nếu chưa chọn thì yêu cầu chọn lại
        if (selectedContact != null) {

            // nếu contact đang chọn có trong list contacts đang update thì thông báo hủy
            // nếu không thì yêu cầu xác nhận xóa đồng ý thì xóa
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
     * lưu các contact của nhóm đang chọn vào file
     *
     * @param event
     * @throws IOException
     * @throws InterruptedException
     */
    @FXML
    private void saveToFile(ActionEvent event) throws IOException, InterruptedException {

        // lấy tên group đang chọn
        String group = cbGroup.getSelectionModel().getSelectedItem().getName();

        // nếu nhóm là all thì thông báo ko lưu và thoát
        if (group.equalsIgnoreCase("all")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("INFORMATION");
            alert.setContentText("Select a group to save");
            alert.show();
            return;
        }

        // yêu cầu xác nhận lưu với link thông báo
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("CONFIRMATION");
        alert.setHeaderText("Save all contacts in the group " + group + " to a file " + group + ".txt?");
        Optional<ButtonType> result = alert.showAndWait();

        // nếu đồng ý thì tiến hành lưu
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // tạo tên file là tên group + txt
            String fileName = group + ".txt";

            // tạo trình chọn thư mục
            DirectoryChooser directoryChooser = new DirectoryChooser();
            // set nơi mở đầu tiên của thư mục là nơi chứa dự án
            directoryChooser.setInitialDirectory(new File(Paths.get(".").toString()));
            // set title cho cửa sổ chọn file
            directoryChooser.setTitle("Select the folder where you want to save the file " + group + ".txt");

            // lấy địa chỉ thư mục cần lưu người dùng chọn gán vào File
            // truyền vào window của cửa sổ này để khóa cửa sổ này ko thể thao tác đến khi đóng cửa sổ chọn thư mục
            File folder = directoryChooser.showDialog(tblContact.getScene().getWindow());

            // nếu địa chỉ ko null tức người dùng đã chọn mà ko ấn hủy
            if (folder != null) {
                // tạo file1 cộng thêm tên file vào địa chỉ thư mục làm địa chỉ của file cần lưu
                File file1 = new File(folder.toString(), fileName);

                // tạo bộ ghi ghi tất cả các contacts trong nhóm đang hiển thị vào file nhờ filteredList
                BufferedWriter writer = new BufferedWriter(new FileWriter(file1));
                try (writer) {
                    for (Contact contact : filteredList) {
                        writer.write(contact.toString());
                    }
                }

                // mở thư mục và file vừa ghi vào
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
