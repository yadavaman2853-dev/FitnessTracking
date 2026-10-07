import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UserManagementPanel extends JPanel {
    private final DefaultTableModel model =
            UIUtil.model("ID", "Name", "Email", "Role", "Goal", "Created");
    private final JTable table = new JTable(model);

    public UserManagementPanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.add(UIUtil.title("User Management"), BorderLayout.WEST);

        JPanel buttons = new JPanel();
        JButton add = UIUtil.button("Add User");
        JButton role = UIUtil.button("Change Role");
        JButton delete = UIUtil.button("Delete User");
        buttons.add(add);
        buttons.add(role);
        buttons.add(delete);
        top.add(buttons, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        UIUtil.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> addUser());
        role.addActionListener(e -> changeRole());
        delete.addActionListener(e -> deleteUser());

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        String sql = "SELECT id,name,email,role,goal,created_at FROM users ORDER BY id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("goal"),
                        rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }

    private void addUser() {
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JPasswordField password = new JPasswordField();
        JComboBox<String> role = new JComboBox<>(new String[]{"USER", "ADMIN"});

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Name")); p.add(name);
        p.add(new JLabel("Email")); p.add(email);
        p.add(new JLabel("Password")); p.add(password);
        p.add(new JLabel("Role")); p.add(role);

        if (JOptionPane.showConfirmDialog(this, p, "Add User",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        if (name.getText().trim().isEmpty() ||
                email.getText().trim().isEmpty() ||
                new String(password.getPassword()).length() < 6) {
            UIUtil.error(this, "Enter valid user details.");
            return;
        }

        String sql = "INSERT INTO users(name,email,password_hash,role) VALUES(?,?,?,?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name.getText().trim());
            ps.setString(2, email.getText().trim().toLowerCase());
            ps.setString(3, PasswordUtil.hash(new String(password.getPassword())));
            ps.setString(4, role.getSelectedItem().toString());
            ps.executeUpdate();
            UIUtil.logActivity("Admin created a user");
            loadData();
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        if (row == -1) {
            UIUtil.error(this, "Select a user first.");
            return null;
        }
        return Integer.parseInt(model.getValueAt(table.convertRowIndexToModel(row), 0).toString());
    }

    private void changeRole() {
        Integer id = selectedId();
        if (id == null) return;

        String role = JOptionPane.showInputDialog(this,
                "Enter role: USER or ADMIN", "USER");

        if (role == null) return;
        role = role.trim().toUpperCase();

        if (!role.equals("USER") && !role.equals("ADMIN")) {
            UIUtil.error(this, "Role must be USER or ADMIN.");
            return;
        }

        String sql = "UPDATE users SET role=? WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setInt(2, id);
            ps.executeUpdate();
            UIUtil.logActivity("Admin changed user role");
            loadData();
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }

    private void deleteUser() {
        Integer id = selectedId();
        if (id == null) return;

        if (id == Session.getUserId()) {
            UIUtil.error(this, "You cannot delete your own admin account.");
            return;
        }

        if (JOptionPane.showConfirmDialog(this,
                "Delete selected user and related data?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM users WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            UIUtil.logActivity("Admin deleted a user");
            loadData();
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }
}
