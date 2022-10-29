//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package controller;

import dao.ContactDAO;
import dao.GroupDAO;
import entity.Contact;
import entity.Group;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateContactController {
    @FXML
    private Label lblReport;
    @FXML
    private ComboBox<Boolean> cbActive;
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private TextField phone;
    @FXML
    private TextField email;
    @FXML
    private DatePicker dob;
    @FXML
    private ComboBox<Group> cbGroup;
    @FXML
    private Label lblFirstName;
    @FXML
    private Label lblLastName;
    @FXML
    private Label lblPhone;
    @FXML
    private Label lblEmail;
    @FXML
    private Label lblDob;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnClose;
    private ContactController contactController;
    private Contact updatedContact;
    private FilteredList<Group> groupFilteredList;

    public ComboBox<Group> getCbGroup() {
        return cbGroup;
    }

    @FXML
    protected void initialize(ContactController contactController) {

        //lấy contactController đang quản lý
        this.contactController = contactController;

        // lấy Contact đang được chọn trên TableView
        this.updatedContact = contactController.getTblContact().getSelectionModel().getSelectedItem();

        // set giá trị các controls theo contact trên
        this.firstName.setText(updatedContact.getFirstName());
        this.lastName.setText(updatedContact.getLastName());
        this.email.setText(updatedContact.getEmail());
        this.phone.setText(updatedContact.getPhone());

        // tạo bộ lọc bỏ group All
        groupFilteredList = new FilteredList<>(GroupDAO.getInstance().getGroupObservableList(),
                new Predicate<Group>() {
                    @Override
                    public boolean test(Group group) {
                        return !group.getName().equalsIgnoreCase("All");
                    }
                });
        // ràng buộc bộ lọc với cbGroup
        cbGroup.setItems(groupFilteredList);

        // cho cbGroup chọn group của updateContact
        this.cbGroup.getSelectionModel().select(new Group(updatedContact.getGroup()));

        // set date cho DatePicker dob
        this.dob.setValue(updatedContact.getDob());

        // set active
        this.cbActive.getItems().addAll(true, false);
        this.cbActive.getSelectionModel().select(updatedContact.isActive());

        // bắt sự kiện thay đổi ngày sinh để gọi hàm xác thực
        dob.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observableValue, LocalDate localDate, LocalDate t1) {
                check();
            }
        });

        check();

        // lấy Stage của cửa sổ bắt sự kiện đóng cửa sổ thì
        // gọi hàm close xử lý các sự kiện cần thiết trước khi đóng
        Stage stage = (Stage) firstName.getScene().getWindow();
        stage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                closeUpdate();
            }
        });
    }

    /**
     * kiểm tra tính hợp lệ các giá trị nhập vào các controls
     * đúng trả về {@link Contact} theo các giá trị trên và cho nút btnAdd ko bị vô hiệu
     */
    @FXML
    private Contact check() {
        boolean check = true;

        // reset giá trị cho các thông báo
        this.lblFirstName.setText("");
        this.lblFirstName.setStyle("-fx-text-fill: red");

        this.lblLastName.setText("");
        this.lblLastName.setStyle("-fx-text-fill: red");

        this.lblEmail.setText("");
        this.lblEmail.setStyle("-fx-text-fill: red");

        this.lblPhone.setText("");
        this.lblPhone.setStyle("-fx-text-fill: red");

        this.lblDob.setText("");
        this.lblDob.setStyle("-fx-text-fill: red");

        this.lblReport.setText("");
        this.lblReport.setStyle("-fx-text-fill: green");

        // lấy các thông tin nhập trên các controls để kiểm tra
        String fname = this.firstName.getText().trim();
        String lname = this.lastName.getText().trim();
        String mobile = this.phone.getText().trim();
        String mail = this.email.getText().trim();
        // kiểm tra xem ngày có nhập đúng ko
        LocalDate birthdate = null;
        try {
            birthdate = this.dob.getValue();
        } catch (DateTimeException e) {
            lblDob.setText("Wrong date of birth");
            lblReport.setText("");
            check = false;
        }

        // cho group của combobox chọn group tại index đã chọn trước đó tránh lỗi group đang chọn bị xóa
        cbGroup.getSelectionModel().select(cbGroup.getSelectionModel().getSelectedIndex());

        //kiểm tra xem số lượng group có != 0 ko thì mới lấy
        String group = "";
        if (groupFilteredList.size() != 0) {
            group = this.cbGroup.getValue().getName();
        }
        boolean active = this.cbActive.getValue();

        // kiểm tra hợp lệ các controls được điền vào
        if (fname.isBlank()) {
            lblFirstName.setText("First Name can not be empty");
            check = false;
        }

        if (lname.isBlank()) {
            lblLastName.setText("Last Name can not be empty");
            check = false;
        }

        if (mobile.isBlank()) {
            lblPhone.setText("Phone can not be empty");
            check = false;
        } else if (!mobile.matches("\\d+")) {
            lblPhone.setText("Phone contains digit only");
            check = false;
        }

        if (mail.isBlank()) {
            lblEmail.setText("Email can not be empty");
            check = false;
        } else if (!mail.matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
            lblEmail.setText("Email is invalid");
            check = false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if (birthdate != null) {
            String date = birthdate.format(formatter);
            // hiện ngày khi giá trị không hiển thị trên DatePicker
            dob.setPromptText(date);
            // thông báo ngày đang được chọn khi ô chọn không hiển thị đầy đủ
            lblReport.setText("Date is: " + date);

            // nếu ngày chọn lớn hơn ngày hiện tại thì báo lỗi
            if (birthdate.isAfter(LocalDate.now())) {
                lblReport.setText("");
                lblDob.setText("Wrong date of birth");
                check = false;
            }
        }else {
            lblDob.setText("Wrong date of birth");
            check = false;
        }

        btnUpdate.setDisable(!check);

        if (check) {
            return new Contact(fname, lname, mobile, mail, birthdate, group, active);
        }

        return null;
    }

    /**
     * set lại giá trị contact cần update theo giá trị các control nhập vào
     * contact gán từ list chứa nên nó với contact trong list là 1, cả 2 cùng tham chiếu đến 1 đối tượng
     * nên thay đổi giá trị contact này cũng là thay đổi contact trong list chứa
     * @throws Exception
     */
    private void setUpdatedContact() throws IOException {
        Alert alert;

        // lấy contact do người dùng nhập vào
        Contact setContact = check();
        // nếu nó ko null tức là các giá trị hợp lệ
        // set lại giá trị các thuộc tính của contact cần update giống với contact nhập vào nhờ hàm set
        // ghi lại giá trị mới vào file
        // làm mới lại bảng TableView để nó hiển thị lại cho đúng(nó ko tự làm mới)
        if (setContact != null) {
            // nếu contact mới giống 1 contact trong list thì thông báo nó đã tồn tại
            // nếu ko giống thì mới update
            if (ContactDAO.getInstance().getContactObservableList().contains(setContact)) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Information");
                alert.setContentText("Information of contact is existed");
                alert.show();

            } else {
                this.updatedContact.setFirstName(setContact.getFirstName());
                this.updatedContact.setLastName(setContact.getLastName());
                this.updatedContact.setPhone(setContact.getPhone());
                this.updatedContact.setEmail(setContact.getEmail());
                this.updatedContact.setDob(setContact.getDob());
                this.updatedContact.setGroup(setContact.getGroup());
                this.updatedContact.setActive(setContact.isActive());
                // cho TableView chọn contact vừa update
                contactController.getTblContact().getSelectionModel().select(updatedContact);
                ContactDAO.getInstance().saveContactsToFile();// ghi lại vào file
                contactController.searchContact();// gọi lại hàm này để nó lọc làm mới xắp xếp TableView
                contactController.getTblContact().refresh();// làm mới lại TableView

                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setContentText("Contact has been updated");
                alert.show();
            }
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Information");
            alert.setContentText("Re-enter invalid information");
            alert.show();
        }
    }

    /**
     * xác định control gọi sự kiện và thực hiện tương ứng
     * @param evt
     * @throws Exception
     */
    public void updateContact(ActionEvent evt) throws Exception {
        // nếu là nút btnUpdate thì gọi hàm update nếu ko thì đóng cửa sổ
        if (evt.getSource() == this.btnUpdate) {
            this.setUpdatedContact();
        } else if (evt.getSource() == this.btnClose) {

            Node source = (Node)evt.getSource();
            Stage stage = (Stage)source.getScene().getWindow();
            stage.close();
        }

    }

    /**
     * xóa contact, group, controller của cửa sổ này khỏi các list nhớ chứa danh sách đang update trước khi đóng cửa sổ
     * áp dụng cả trường hợp nhấn nút tắt "x" và gọi lệnh đóng stage.close();
     */
    private void closeUpdate() {
        // ko update nữa thì xóa contact khỏi list các contacts đang update, group làm tương tự
        contactController.getUpdatingContacts().remove(updatedContact);
        contactController.getUpdatingGroup().remove(updatedContact.getGroup());
        // xóa controller này khỏi list controller đang update
        contactController.getUpdateContactControllerList().remove(this);

    }
}
