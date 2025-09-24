package com.smartattendance.controller; import com.smartattendance.model.User; import com.smartattendance.repository.InMemoryUserRepository; import com.smartattendance.service.AuthService;
import javafx.fxml.*; import javafx.scene.*; import javafx.scene.control.*; import javafx.stage.Stage;
public class LoginController {
  @FXML private TextField usernameField; @FXML private PasswordField passwordField; @FXML private Label errorLabel;
  private final AuthService authService=new AuthService(new InMemoryUserRepository());
  @FXML private void onLoginButtonClick(){
    User user=authService.authenticate(usernameField.getText(), passwordField.getText());
    if(user!=null){ try{ Parent mainRoot=FXMLLoader.load(getClass().getResource("/view/MainView.fxml")); Stage stage=(Stage)usernameField.getScene().getWindow(); stage.setScene(new Scene(mainRoot)); stage.setTitle("Smart Attendance - "+user.getRole()); }catch(Exception e){ errorLabel.setText("Load UI failed: "+e.getMessage()); } }
    else { errorLabel.setText("Invalid username or password."); }
  }
}
