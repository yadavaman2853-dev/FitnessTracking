import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterFrame extends JFrame {
    private final JFrame loginFrame;
    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JTextField goalField = new JTextField("General Fitness");

    public RegisterFrame(JFrame loginFrame) {
        this.loginFrame = loginFrame;
        setTitle("FitTrack - Register");
        setSize(450, 430);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JPanel heading = new JPanel(new BorderLayout());
        heading.add(UIUtil.title("Create Account"), BorderLayout.WEST);
        root.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(8, 1, 6, 6));
        form.add(new JLabel("Full Name"));
        form.add(nameField);
        form.add(new JLabel("Email"));
        form.add(emailField);
        form.add(new JLabel("Password"));
        form.add(passwordField);
        form.add(new JLabel("Fitness Goal"));
        form.add(goalField);
        root.add(form, BorderLayout.CENTER);

        JButton create = UIUtil.button("Register");
        create.addActionListener(e -> register());
        root.add(create, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void register() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String password = new String(passwordField.getPassword());
        String goal = goalField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.length() < 6) {
            UIUtil.error(this, "Enter valid details. Password must contain at least 6 characters.");
            return;
        }

        String sql = "INSERT INTO users(name,email,password_hash,role,goal) VALUES(?,?,?,?,?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hash(password));
            ps.setString(4, "USER");
            ps.setString(5, goal.isEmpty() ? "General Fitness" : goal);
            ps.executeUpdate();

            UIUtil.info(this, "Account created successfully.");
            dispose();
            loginFrame.setVisible(true);

        } catch (SQLIntegrityConstraintViolationException ex) {
            UIUtil.error(this, "This email is already registered.");
        } catch (SQLException ex) {
            UIUtil.error(this, "Registration failed:\n" + ex.getMessage());
        }
    }
}
