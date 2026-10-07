import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ProfilePanel extends JPanel {
    private final JTextField name = new JTextField();
    private final JTextField email = new JTextField();
    private final JTextField goal = new JTextField();
    private final JPasswordField password = new JPasswordField();

    public ProfilePanel() {
        setLayout(new BorderLayout());
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        wrapper.add(UIUtil.title("Profile Management"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(8, 1, 8, 8));
        form.add(new JLabel("Name"));
        form.add(name);
        form.add(new JLabel("Email"));
        form.add(email);
        form.add(new JLabel("Fitness Goal"));
        form.add(goal);
        form.add(new JLabel("New Password (optional)"));
        form.add(password);

        wrapper.add(form, BorderLayout.CENTER);

        JButton save = UIUtil.button("Update Profile");
        save.addActionListener(e -> saveProfile());
        wrapper.add(save, BorderLayout.SOUTH);

        add(wrapper);
        loadProfile();
    }

    private void loadProfile() {
        String sql = "SELECT name,email,goal FROM users WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Session.getUserId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    name.setText(rs.getString("name"));
                    email.setText(rs.getString("email"));
                    goal.setText(rs.getString("goal"));
                    email.setEditable(false);
                }
            }
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }

    private void saveProfile() {
        String newName = name.getText().trim();
        String newGoal = goal.getText().trim();
        String newPassword = new String(password.getPassword());

        if (newName.isEmpty()) {
            UIUtil.error(this, "Name cannot be empty.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (newPassword.isEmpty()) {
                String sql = "UPDATE users SET name=?, goal=? WHERE id=?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, newName);
                    ps.setString(2, newGoal);
                    ps.setInt(3, Session.getUserId());
                    ps.executeUpdate();
                }
            } else {
                if (newPassword.length() < 6) {
                    UIUtil.error(this, "Password must contain at least 6 characters.");
                    return;
                }

                String sql = "UPDATE users SET name=?, goal=?, password_hash=? WHERE id=?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, newName);
                    ps.setString(2, newGoal);
                    ps.setString(3, PasswordUtil.hash(newPassword));
                    ps.setInt(4, Session.getUserId());
                    ps.executeUpdate();
                }
            }

            Session.start(Session.getUserId(), newName, Session.getEmail(), Session.getRole());
            UIUtil.logActivity("Updated profile");
            UIUtil.info(this, "Profile updated successfully.");

        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }
}
