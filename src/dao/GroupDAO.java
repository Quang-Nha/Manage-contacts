package dao;


import entity.Group;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;


public class GroupDAO {

    private static GroupDAO instance;
    private final ObservableList<Group> groupObservableList;
    private final Path GROUPS_DIR = FileSystems.getDefault().getPath("data");
    private final Path GROUPS_FILE = FileSystems.getDefault().getPath(GROUPS_DIR.toAbsolutePath().toString(), "group.txt");

    private GroupDAO() {
        groupObservableList = FXCollections.observableArrayList();
    }

    public ObservableList<Group> getGroupObservableList() {
        return groupObservableList;
    }

    public synchronized static GroupDAO getInstance() {
        if (instance == null) {
            instance = new GroupDAO();
        }

        return instance;
    }

    /**
     * đọc các groups từ file vào list
     * @throws IOException
     */
    public void loadGroupsFromFile() throws IOException {
        // nếu trong list chưa có group tên All thì cần thêm nó vào ô đầu tiên để khi lọc theo group
        // có thể lấy tất cả các group
        if (!groupObservableList.contains(new Group("All"))) {
            groupObservableList.add(0, new Group("All"));
        }

        // tạo file và thư mục nếu chưa có
        if (Files.notExists(GROUPS_DIR)) {
            Files.createDirectory(GROUPS_DIR);
            return;
        }
        if (Files.notExists(GROUPS_FILE)) {
            Files.createFile(GROUPS_FILE);
            return;
        }

        groupObservableList.clear();

        BufferedReader reader = Files.newBufferedReader(GROUPS_FILE);

        Scanner scanner = new Scanner(reader);

        try (scanner){

            while (scanner.hasNextLine()) {

                String group = scanner.nextLine().trim();

                groupObservableList.add(new Group(group));

            }
        }


    }

    /**
     * lưu List groupObservableList vào file
     * @throws IOException
     */
    public void saveGroupsToFile() throws IOException {

        BufferedWriter writer = Files.newBufferedWriter(GROUPS_FILE);

        try(writer) {
            for (Group group : groupObservableList) {
                writer.write(group.toString());
            }
        }
    }
}
