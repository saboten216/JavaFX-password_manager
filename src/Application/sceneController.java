package Application;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

public class sceneController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private Label newaccpwd_hint; //新增頁面的提示
    @FXML
    private Label login_hint; //登入提示
    @FXML
    private Label register_hint; //註冊提示
    @FXML
    private TextField register_acc; //註冊帳號變數
    @FXML
    private PasswordField register_pwd; //註冊密碼變數
    @FXML
    private TextField account; //登入帳號
    @FXML
    private PasswordField pwd; //登入密碼
    @FXML
    private TextField new_acc; //新增帳號 
    @FXML
    private PasswordField new_pwd; //新增密碼
    @FXML
    private TextField new_net; //新增網站
    @FXML
    private Label listview_result; //list顯示
    @FXML
    private ListView<String> accpwd_listview;
    @FXML
    private TextField edit_acc; //修改帳號 
    @FXML
    private TextField edit_pwd; //修改密碼
    @FXML
    private TextField edit_net; //修改網站
    @FXML
    private Label editaccpwd_hint; //修改頁面的提示


    /*
    ================================================================
     */
    PreparedStatement state = null;
    ResultSet result = null; //存放結果
    String sql = null; //存放查詢語言
    String url = "jdbc:sqlserver://[ IP address ]:1433;DatabaseName=[     name     ];trustServerCertificate=true";
    String username = "[ sa ]";
    String password = "[ password ]";
    
    static Connection conn = null;//保持連線
    static int count = 0;
    static boolean edit_flag = false;
    static String id;
    static String edit_acc_tmp;
    static String edit_pwd_tmp;
    static String edit_url_tmp;

    ArrayList<Integer> num = new ArrayList<Integer>();
    static int list_index = 0;

    /*
    ======================SHA512=====================================
     */
    public static String getSHA512(String input) {
        String toReturn = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            digest.update(input.getBytes("utf8"));
            toReturn = String.format("%0128x", new BigInteger(1, digest.digest()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return toReturn.toUpperCase();
    }

    /*
    ======================初始化=====================================
     */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        accpwd_listview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                list_index = accpwd_listview.getSelectionModel().getSelectedIndex();
            }
        });

        if (count == 0) {
            try {
                conn = DriverManager.getConnection(url , username , password );
                System.out.println("資料庫連線成功!!");
            } catch (SQLException e) {
                System.err.println("資料庫連線失敗");
                e.printStackTrace();
            }
        }
        count = count + 1; //連線一次

        try {
            sql = "select  *  from PWData where id = " + id; //依照id取資料
            state = conn.prepareStatement(sql);
            result = state.executeQuery();
            System.err.print("init");
            while (result.next()) {
                num.add(result.getInt(5)); //存入num
                accpwd_listview.getItems().addAll("網站：" + result.getString(2) + "\n\n" + "帳號：" + result.getString(3) + "\n\n" + "密碼：" + result.getString(4) + "\n\n");
                accpwd_listview.refresh();
            }
        } catch (SQLException e) {
            System.err.println("擷取資料失敗");
            e.printStackTrace();
        }

        if (edit_flag) {
            edit_acc.setText(edit_acc_tmp);
            edit_pwd.setText(edit_pwd_tmp);
            edit_net.setText(edit_url_tmp);
            edit_flag = false;
        }

    }


    /*
    ============================功能==================================
     */
    @FXML //登入(login) >> 內頁(home)
    public void switchToScene_login(ActionEvent event) throws IOException, SQLException {
        sql = "select  id , account , password from LoginData";
        state = conn.prepareStatement(sql);
        result = state.executeQuery();

        while (result.next()) {
            System.out.printf("DBpw = %s\n", result.getString(3));
            System.out.printf("input pw = %s\n", getSHA512(pwd.getText()));
            if (result.getString(2).equals(account.getText()) && result.getString(3).equals(getSHA512(pwd.getText()))) {
                id = result.getString(1);
                System.out.printf("id get : %s \n", result.getString(1));
                root = FXMLLoader.load(getClass().getResource("scene_home.fxml"));
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

                break;
            } else {
                login_hint.setText("帳號或密碼錯誤 請再試一次");
            }
        }
    }

    @FXML
    private void register(ActionEvent event) throws IOException, SQLException { //註冊 寫入帳號密碼
        //1. 先判斷有沒有註冊過
        sql = "select  id , account  from LoginData";
        state = conn.prepareStatement(sql);
        result = state.executeQuery();
        ResultSet tmp = null;
        boolean flag = true;
        int max_id = 0;
        System.out.print(result);
        while (result.next()) {
            System.out.printf("DB id = %s \nDB account = %s \n 有無重複 : %b \n", result.getString(1), result.getString(2), result.getString(2).equals(register_acc.getText()));
            if (register_acc.getText().trim().isEmpty() || register_pwd.getText().trim().isEmpty()) {//帳號密碼皆不可輸入空格
                register_hint.setText("請輸入帳號密碼");
                flag = false;
                break;
            } else if (validateLegalString(register_acc.getText())) {
                register_hint.setText("帳號不可使用特殊字元");
                flag = false;
                break;
            } else if (result.getString(2).equals(register_acc.getText())) {
                register_hint.setText("此帳號名稱已經被註冊過");
                flag = false;
                break;
            }

            if (max_id < Integer.parseInt(result.getString(1))) { //我要新增到資料庫的 id (唯一值)
                max_id = Integer.parseInt(result.getString(1));
            }

        }
        //2. 只要沒有重複 -- > 可以註冊 (id = 最後一個號碼) //
        if (flag) {
            register_hint.setText("註冊成功 ! 返回主頁登入");
            sql = "insert into  LoginData OUTPUT INSERTED.ID values(" + Integer.toString(max_id + 1) + ",'" + register_acc.getText() + "','" + getSHA512(register_pwd.getText()) + "'); ";
//            System.out.print(sql);
            state = conn.prepareStatement(sql);
            result = state.executeQuery();
        }
    }

    @FXML
    private void newAccPwd_new(ActionEvent event) throws IOException, SQLException { //寫入要新增的帳號密碼到資料庫
        if (new_acc.getText().trim().isEmpty() || new_pwd.getText().trim().isEmpty() || new_net.getText().trim().isEmpty()) {
            newaccpwd_hint.setText("請輸入要新增的內容");
        } else {
            sql = "insert into  PWData( id , web , account , password) OUTPUT INSERTED.ID values(" + id + ",'" + new_net.getText() + "','" + new_acc.getText() + "','" + new_pwd.getText() + "')";
            state = conn.prepareStatement(sql);
            result = state.executeQuery();
            newaccpwd_hint.setText("新增成功!");
        }

    }

    @FXML
    public void Edit(ActionEvent event) throws IOException, SQLException { //編輯按鈕
        if (edit_acc.getText().trim().isEmpty() || edit_pwd.getText().trim().isEmpty() || edit_net.getText().trim().isEmpty()) {
            editaccpwd_hint.setText("請輸入要修改的內容");
        } else {
            sql = "update PWData set web = '" + edit_net.getText() + "' ,account = '" + edit_acc.getText() + "',password = '" + edit_pwd.getText() + "' OUTPUT INSERTED.Id  where num = " + num.get(list_index);
            System.out.print(list_index);
            state = conn.prepareStatement(sql);
            result = state.executeQuery();
            editaccpwd_hint.setText("修改成功!");
            try {
                sql = "select  *  from PWData where id = " + id; //依照id找資料
                state = conn.prepareStatement(sql);
                result = state.executeQuery();
                num.clear(); //清空num array
                accpwd_listview.getItems().clear();//清空listview
                while (result.next()) {
                    num.add(result.getInt(5)); //存入num
                    accpwd_listview.getItems().addAll("網站：" + result.getString(2) + "\n\n" + "帳號：" + result.getString(3) + "\n\n" + "密碼：" + result.getString(4) + "\n\n");
                    accpwd_listview.refresh();
                }
            } catch (SQLException e) {
                System.err.println("擷取資料失敗");
                e.printStackTrace();
            }
        }

    }

    @FXML
    public void delete(ActionEvent event) throws IOException, SQLException { //刪除按鈕
        sql = "delete from PWData OUTPUT [DELETED].ID where num = " + num.get(list_index);
        System.out.print(sql);
        state = conn.prepareStatement(sql);
        result = state.executeQuery();

        try {
            sql = "select  *  from PWData where id = " + id; //依照id找資料
            state = conn.prepareStatement(sql);
            result = state.executeQuery();
            num.clear(); //清空num array
            accpwd_listview.getItems().clear();//清空listview
            while (result.next()) {
                num.add(result.getInt(5)); //存入num
                accpwd_listview.getItems().addAll("網站：" + result.getString(2) + "\n\n" + "帳號：" + result.getString(3) + "\n\n" + "密碼：" + result.getString(4) + "\n\n");
                accpwd_listview.refresh();
            }
        } catch (SQLException e) {
            System.err.println("擷取資料失敗");
            e.printStackTrace();
        }

    }

    public static boolean validateLegalString(String content) { //檢查註冊帳號有無特殊字元
        String illegal = "`~!#%^&*= \\|{@};:'\",<>/?○●★☆☉♀♂※¤╬の〆";
        for (int ii = 0; ii < content.length(); ii++) {
            for (int jj = 0; jj < illegal.length(); jj++) {
                if (content.charAt(ii) == illegal.charAt(jj)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
    ===========================按鈕事件=================================
     */
    @FXML//跳轉頁面到登入畫面
    public void switchToScene_init(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("scene_login.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML//跳轉頁面到註冊畫面
    public void switchToScene2(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("scene_register.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    @FXML//跳轉頁面到密碼庫內頁
    private void switchToScene_main(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("scene_home.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML//跳轉頁面到新增畫面
    private void new_accpwd(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("scene_newAccPwd.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    @FXML//跳轉到修改頁面
    public void Edit_scene(ActionEvent event) throws IOException, SQLException {

        sql = "select  *  from PWData where num = " + Integer.toString(num.get(list_index));
        state = conn.prepareStatement(sql);
        result = state.executeQuery();

        while (result.next()) {
            edit_acc_tmp = result.getString(3);
            edit_pwd_tmp = result.getString(4);
            edit_url_tmp = result.getString(2);
        }

        edit_flag = true;

        root = FXMLLoader.load(getClass().getResource("scene_editAccPwd.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

}
