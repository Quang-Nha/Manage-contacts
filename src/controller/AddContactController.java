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
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class AddContactController{

    @FXML
    private ComboBox<Boolean> cbActive;
    @FXML
    private Label lblReport;
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
    private Button btnAdd;
    @FXML
    private Button btnClose;

    public ComboBox<Group> getCbGroup() {
        return cbGroup;
    }

    FilteredList<Group> groupFilteredList;
    private ContactController contactController;

    /**
     * bắt buộc bắt sự kiện trong hàm này ko phải hàm khởi tạo vì cần hàm có tham số được gọi từ controller sau khi
     * đã mở cửa sổ thì cbActive.getScene().getWindow(); mới ko bị null
     * @param contactController
     */
    public void setContactController(ContactController contactController) {
        this.contactController = contactController;
        // lấy Stage của cửa sổ bắt sự kiện đóng cửa sổ thì
        // gọi hàm close xử lý các sự kiện cần thiết trước khi đóng
        Stage stage = (Stage) cbActive.getScene().getWindow();
        stage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                // gọi hàm xử lý sự kiện trước khi đóng
                closeAdd();
            }
        });
    }

    @FXML
    void initialize() throws Exception {
        // xóa các giá trị mặc định trên các label
        this.lblFirstName.setText("");
        this.lblLastName.setText("");
        this.lblEmail.setText("");
        this.lblPhone.setText("");
        this.lblDob.setText("");

        if (GroupDAO.getInstance().getGroupObservableList().size() > 0) {

        }
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

        // cho cbGroup chọn phần tử đầu tiên
        this.cbGroup.getSelectionModel().selectFirst();
        this.dob.setValue(LocalDate.now());

        // thêm true, false cho cbActive và mặc định chọn true
        this.cbActive.getItems().addAll(true, false);
        this.cbActive.getSelectionModel().select(true);

        // bắt sự kiện thay đổi ngày sinh để gọi hàm xác thực
        dob.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observableValue, LocalDate localDate, LocalDate t1) {
                check();
            }
        });

        // gọi hàm check để in thông báo các ô chưa hợp lệ
        check();

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
        } else {
            lblDob.setText("Wrong date of birth");
            check = false;
        }

        btnAdd.setDisable(!check);

        if (check) {
            return new Contact(fname, lname, mobile, mail, birthdate, group, active);
        }

        return null;
    }

    /**
     * thêm Contact vào list và hiển thị trên {@link TableView} của contactController
     * kiểm tra xem có trùng với contact đang có trong list ko, nếu có thì ko thêm
     * và ghi list vào file
     */
    private void saveContact() throws IOException {

        Contact addContact;
        ObservableList<Contact> list = ContactDAO.getInstance().getContactObservableList();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");

        // lấy contact từ hàm kiểm tra check
        // nếu contact ko null thì kiểm tra xem có trùng với contact trong list ko
        // nếu có thì ko thêm, ko có thì thêm vào list
        if ((addContact = check()) != null) {

            // kiểm tra trùng bằng hàm contains() nhưng cần Override hàm equals của các đối tượng list chứa
            if (list.contains(addContact)) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Information of contact is existed");
                alert.setTitle("Error");
                alert.show();
            } else {
                list.add(addContact);
                ContactDAO.getInstance().saveContactsToFile();//ghi list vào file
                contactController.getTblContact().getSelectionModel().select(addContact);
                alert.setContentText("New Contact has been added");
                alert.show();
            }

        }
    }

    /**
     * kiểm tra control gọi sự kiện để thực hiện lệnh
     *
     * @param evt sự kiện
     */
    public void saveContact(ActionEvent evt) throws IOException {
        if (evt.getSource() == this.btnAdd) {
            this.saveContact();
        } else if (evt.getSource() == this.btnClose) {

            // lấy node của event rồi lấy scene, Stage của node và đóng cửa sổ Stage này
            Node source = (Node) evt.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }

    }

    /**
     * xóa controller của cửa sổ này khỏi các list nhớ chứa danh sách đang add trước khi đóng cửa sổ
     * áp dụng cả trường hợp nhấn nút tắt "x" và gọi lệnh đóng stage.close();
     */
    private void closeAdd() {
        contactController.getAddContactControllerList().remove(this);
    }

}
