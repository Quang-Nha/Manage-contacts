package controller;


import dao.ContactDAO;
import dao.GroupDAO;
import entity.Contact;
import entity.Group;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;


public class GroupController {

    @FXML
    private Button btnSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnClose;
    @FXML
    private ListView<Group> tblGroup;
    @FXML
    private TextField search;
    @FXML
    private TextField groupName;

    private ContactController contactController;
    private FilteredList<Group> filteredList;
    private ObservableList<Group> groupObservableList;

    public GroupController() {
    }

    /**
     * lấy {@link ContactController} được truyền vào
     *
     * @param contactController
     */
    public void setContactController(ContactController contactController) {
        this.contactController = contactController;

    }

    @FXML
    void initialize(ContactController contactController) {
        // lấy {@link ContactController} được truyền vào
        this.contactController = contactController;

        // lấy list chứa các group
        groupObservableList = contactController.getGroupObservableList();

        // tạo list lọc xóa group all lấy nguồn từ list trên
        FilteredList<Group> notAll = groupObservableList.filtered(new Predicate<Group>() {
            @Override
            public boolean test(Group group) {
                return !group.getName().equalsIgnoreCase("All");
            }
        });

        // tạo list lọc thứ 2 để lọc khi tìm kiếm lấy nguồn từ list trên
        filteredList = notAll.filtered(null);

        // tạo list xắp xếp lấy nguồn từ list trên
        SortedList<Group> sortedList = filteredList.sorted();

        // ràng buộc ListView với list trên
        // chỉ được chọn 1 hàng
        tblGroup.setItems(sortedList);
        tblGroup.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // bắt sự kiện thay đổi chọn hàng thì hiển thị tên group trong hàng vào groupName
        tblGroup.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Group>() {
            @Override
            public void changed(ObservableValue<? extends Group> observableValue, Group group, Group t1) {
                if (t1 != null) {
                    groupName.setText(t1.getName());
                }
            }
        });
        // chọn hàng đầu tiên
        tblGroup.getSelectionModel().selectFirst();

        // bắt sự kiện nhấn phím gọi hàm lọc list theo tìm kiếm
        search.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                search();
            }
        });

        // cài vô hiệu hóa 2 nút nết groupName rỗng
        btnAdd.setDisable(groupName.getText().isBlank());
        btnUpdate.setDisable(groupName.getText().isBlank());

        // bắt sự kiện nhấn phím groupName cài lại điều kiện vô hiệu 2 nút
        groupName.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                btnAdd.setDisable(groupName.getText().isBlank());
                btnUpdate.setDisable(groupName.getText().isBlank());
            }
        });

    }

    /**
     * hiển thị những groups phù hợp với từ tìm kiếm
     */
    private void search() {
        Group selected = tblGroup.getSelectionModel().getSelectedItem();
        Predicate<Group> predicate = new Predicate<Group>() {
            @Override
            public boolean test(Group group) {
                // nếu ô tìm kiếm rỗng thì trả về true
                if (search.getText().isBlank()) {
                    return true;
                }

                // trả về kết quả tên group có chứa từ tìm kiếm ko
                return group.getName().toLowerCase(Locale.ROOT)
                        .contains(search.getText().toLowerCase(Locale.ROOT));
            }
        };

        // setPredicate cho filteredList để lọc
        filteredList.setPredicate(predicate);

        // nếu sau khi lọc list của tblGroup > 0 thì xem list này còn chứa group trước khi lọc ko
        // nếu còn thì tiếp tục chọn
        if (tblGroup.getItems().size() > 0) {
            if (tblGroup.getItems().contains(selected)) {
                tblGroup.getSelectionModel().select(selected);
            }
        }
    }

    /**
     * thêm group vào list groups và thêm list vào file rồi hiển thị list
     *
     * @throws Exception
     */
    public void addAction() throws Exception {
        String groupName = this.groupName.getText().trim();

        Alert alert;
        // nếu tên group cần thêm trùng tên group trong list thì hủy thêm
        for (Group group1 : groupObservableList) {
            if (group1.getName().equalsIgnoreCase(groupName)) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setContentText("Group name exists already, choose another name");
                alert.show();
                return;
            }
        }

        // nếu ko trùng thì thêm, cho tblGroup chọn group vừa thêm và ghi vào file
        Group addGroup = new Group(groupName);
        groupObservableList.add(addGroup);
        tblGroup.getSelectionModel().select(addGroup);
        GroupDAO.getInstance().saveGroupsToFile();
        // in alert thông báo thành công
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText("A new group has been added");
        alert.show();

    }

    /**
     * update giá trị group trong list groups tại hàng đang được chọn trong ListView
     */
    public void updateAction() throws Exception {
        // lấy tên group do người dùng nhập
        String groupName = this.groupName.getText().trim();
        Alert alert;

        // lấy group đang chọn trên tblGroup
        Group selectedGroup = tblGroup.getSelectionModel().getSelectedItem();

        // nếu group ko null tức đã có group được chọn
        if (selectedGroup != null) {
            // nếu tên group cần thêm trùng tên group trong list thì hủy update
            for (Group group1 : groupObservableList) {
                if (group1.getName().equalsIgnoreCase(groupName)) {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("ERROR");
                    alert.setContentText("Group name exists already, choose another name");
                    alert.show();
                    return;
                }
            }
            // nếu ko trùng thì bắt đầu update
            // lấy list các contacts
            ObservableList<Contact> contacts = ContactDAO.getInstance().getContactObservableList();
            // duyệt qua list contacts đổi tên group của tất cả các contacts có group trùng với tên group
            // đang được chọn trên tblGroup bằng tên group mới do người dùng nhập là groupName
            contacts.forEach(contact -> {
                if (contact.getGroup().equalsIgnoreCase(selectedGroup.getName())) {
                    contact.setGroup(groupName);
                }
            });

            // set lại tên group đang chọn bằng tên mới do người dùng nhập
            selectedGroup.setName(groupName);
            GroupDAO.getInstance().saveGroupsToFile();// lưu lại vào file group
            ContactDAO.getInstance().saveContactsToFile();// lưu lại vào file contact
            tblGroup.refresh();// làm mới lại ListView
            contactController.getTblContact().refresh();// làm mới lại TableView của cửa sổ contacts

            // tại comboBox của cửa sổ contacts thực hiện thay đổi lựa chọn rồi cho về lựa chọn ban đầu
            // để nó làm mới lại tên group vừa đổi vì nó ko thể tự làm mới
            ComboBox<Group> comboBox = contactController.getCbGroup();
            int select = comboBox.getSelectionModel().getSelectedIndex();
//            comboBox.getSelectionModel().selectFirst();
//            comboBox.getSelectionModel().selectLast();
            comboBox.getSelectionModel().clearSelection();
            comboBox.getSelectionModel().select(select);

            // lấy list các controller của các cửa sổ đang update
            ObservableList<UpdateContactController> updateContactControllerList =
                    contactController.getUpdateContactControllerList();
            // lấy list các controller của các cửa sổ đang add
            ObservableList<AddContactController> addContactControllerList =
                    contactController.getAddContactControllerList();

            // nếu 2 list trên có phần tử tức size > 0
            // duyệt qua list, tại mỗi phần tử controller lấy ra ComboBox<Group> của nó và
            // thực hiện bỏ lựa chọn sau đó cho về lựa chọn ban đầu như trên để làm mới nó và hiển thị phần tử đúng
            if (updateContactControllerList.size() > 0) {
                updateContactControllerList.forEach(updateContactController -> {
                    ComboBox<Group> groupComboBox = updateContactController.getCbGroup();
                    int index = groupComboBox.getSelectionModel().getSelectedIndex();
//                    groupComboBox.getSelectionModel().selectFirst();
//                    groupComboBox.getSelectionModel().selectLast();
                    groupComboBox.getSelectionModel().clearSelection();
                    groupComboBox.getSelectionModel().select(index);

                });
            }
            if (addContactControllerList.size() > 0) {
                addContactControllerList.forEach(addContactController -> {
                    ComboBox<Group> groupComboBox = addContactController.getCbGroup();
                    int index = groupComboBox.getSelectionModel().getSelectedIndex();
//                    groupComboBox.getSelectionModel().selectFirst();
//                    groupComboBox.getSelectionModel().selectLast();
                    groupComboBox.getSelectionModel().clearSelection();
                    groupComboBox.getSelectionModel().select(index);

                });
            }

            // thông báo update thành công
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setContentText("A Group has been updated");
            alert.show();
        } else {
            // thông báo chọn group để update
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Information");
            alert.setContentText("Select a Contact to update");
            alert.show();
        }


    }

    /**
     * xóa group khỏi List, ListView và file
     *
     * @throws Exception
     */
    public void deleteAction() throws Exception {
        Group selectedGroup = tblGroup.getSelectionModel().getSelectedItem();
        Alert alert;

        if (selectedGroup != null) {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setContentText("Do you wanna delete selected group?");
            Optional<ButtonType> result = alert.showAndWait();

            // nếu đồng ý xóa
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // duyệt qua list chứa các contacts
                ObservableList<Contact> contactObservableList = ContactDAO.getInstance().getContactObservableList();
                for (Contact contact : contactObservableList) {
                    // nếu trong list có contact chứa group giống với group đang chọn
                    if (contact.getGroup().equals(selectedGroup.getName())) {
                        // lấy list các groups đang update
                        List<String> updatingGroupList = contactController.getUpdatingGroup();

                        // nếu groups đang udate có chứa contact cần xóa này thì báo ko thể xóa và thoát luôn hàm
                        if (updatingGroupList.contains(contact.getGroup())) {
                            Alert alert1;
                            alert1 = new Alert(Alert.AlertType.ERROR);
                            alert1.setTitle("ERROR");
                            alert1.setContentText("Cannot be deleted because the contact containing the group is being updated");
                            alert1.show();
                            return;
                        }

                        // nếu không thì hỏi xem đồng ý xóa các group có trong các contacts
                        // đồng thời xóa luôn các contact này luôn ko
                        Alert alert2;
                        alert2 = new Alert(Alert.AlertType.WARNING);
                        alert2.setTitle("Confirmation");
                        alert2.setHeaderText("Confirmation");
                        alert2.setContentText("Group has some contacts, do you wanna delete selected group?");
                        alert2.initStyle(StageStyle.UTILITY);
                        alert2.getButtonTypes().add(ButtonType.CANCEL);
                        Optional<ButtonType> result2 = alert2.showAndWait();

                        // nếu đồng ý thì xóa các contacts có group cần xóa trong list các contacts
                        // đồng thời thoát vòng lặp để tiếp tục thực hiện xóa group
                        // nếu ko đồng ý thì thoát luôn hàm
                        if (result2.isPresent() && result2.get() == ButtonType.OK) {

                            contactObservableList.removeIf(c ->
                                    c.getGroup().equalsIgnoreCase(selectedGroup.getName()));

                            break;
                        } else {
                            return;
                        }

                    }
                }

                // nếu tất cả điều kiện trên thỏa mãn và đồng ý thì xóa group
                // các điều kiện trên nếu ko đồng ý sẽ thoát luôn hàm nên câu lệnh này ko thực hiện
                groupObservableList.remove(selectedGroup);
                ContactDAO.getInstance().saveContactsToFile();
                GroupDAO.getInstance().saveGroupsToFile();

            }

        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setContentText("Select a group to delete");
            alert.show();
        }

    }

    /**
     * xác định control gọi sự kiện thì gọi hàm xử lý tương ứng
     *
     * @param evt
     * @throws Exception
     */
    public void groupAction(ActionEvent evt) throws Exception {
        if (evt.getSource() == this.btnSearch) {// dấu == kiểm tra cùng nơi lưu đối tượng ko
//            this.searchAction();
        } else if (evt.getSource() == this.btnAdd) {
            this.addAction();
        } else if (evt.getSource() == this.btnUpdate) {
            this.updateAction();
        } else if (evt.getSource() == this.btnDelete) {
            this.deleteAction();
        } else if (evt.getSource() == this.btnClose) {
            Node source = (Node) evt.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }
    }

}
