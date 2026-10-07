import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginFrame() {
        setTitle("FitTrack - Login");
        setSize(430, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel title = UIUtil.title("FitTrack");
        JLabel subtitle = new JLabel("Online Fitness Tracking Application");
        subtitle.setForeground(Color.GRAY);

        JPanel heading = new JPanel(new GridLayout(2, 1));
        heading.add(title);
        heading.add(subtitle);
        root.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 1, 8, 8));
        form.add(new JLabel("Email"));
        form.add(emailField);
        form.add(new JLabel("Password"));
        form.add(passwordField);
        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton login = UIUtil.button("Login");
        JButton register = UIUtil.button("Create Account");

        login.addActionListener(e -> login());
        register.addActionListener(e -> {
            new RegisterFrame(this).setVisible(true);
            setVisible(false);
        });

        buttons.add(login);
        buttons.add(register);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void login() {
        String email = emailField.getText().trim().toLowerCase();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            UIUtil.error(this, "Enter email and password.");
            return;
        }

        String sql = "SELECT id, name, email, role FROM users WHERE email=? AND password_hash=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, PasswordUtil.hash(password));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Session.start(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("role")
                    );

                    UIUtil.logActivity("User logged in");
                    dispose();

                    if (Session.isAdmin()) {
                        new AdminDashboard().setVisible(true);
                    } else {
                        new UserDashboard().setVisible(true);
                    }
                } else {
                    UIUtil.error(this, "Invalid email or password.");
                }
            }
        } catch (SQLException ex) {
            UIUtil.error(this, "Database connection failed:\n" + ex.getMessage());
        }
    }
}
