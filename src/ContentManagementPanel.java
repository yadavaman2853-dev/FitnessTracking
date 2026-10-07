import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ContentManagementPanel extends JPanel {
    private final DefaultTableModel model =
            UIUtil.model("ID", "Title", "Description", "Target", "Unit", "Status");
    private final JTable table = new JTable(model);

    public ContentManagementPanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.add(UIUtil.title("Fitness Content Management"), BorderLayout.WEST);

        JPanel buttons = new JPanel();
        JButton add = UIUtil.button("Create Challenge");
        JButton approve = UIUtil.button("Approve");
        JButton reject = UIUtil.button("Reject");
        buttons.add(add);
        buttons.add(approve);
        buttons.add(reject);
        top.add(buttons, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        UIUtil.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> createChallenge());
        approve.addActionListener(e -> setStatus("APPROVED"));
        reject.addActionListener(e -> setStatus("REJECTED"));

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        String sql = "SELECT id,title,description,target,unit,status FROM challenges ORDER BY id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("target"),
                        rs.getString("unit"),
                        rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }

    private void createChallenge() {
        JTextField title = new JTextField();
        JTextArea description = new JTextArea(4, 20);
        JTextField target = new JTextField();
        JTextField unit = new JTextField();

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Title")); p.add(title);
        p.add(new JLabel("Description")); p.add(new JScrollPane(description));
        p.add(new JLabel("Target")); p.add(target);
        p.add(new JLabel("Unit")); p.add(unit);

        if (JOptionPane.showConfirmDialog(this, p, "Create Challenge",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            int targetValue = Integer.parseInt(target.getText().trim());
            if (targetValue <= 0 || title.getText().trim().isEmpty() ||
                    description.getText().trim().isEmpty() || unit.getText().trim().isEmpty()) {
                throw new IllegalArgumentException();
            }

            String sql = "INSERT INTO challenges(title,description,target,unit,status) VALUES(?,?,?,?,?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, title.getText().trim());
                ps.setString(2, description.getText().trim());
                ps.setInt(3, targetValue);
                ps.setString(4, unit.getText().trim());
                ps.setString(5, "APPROVED");
                ps.executeUpdate();
            }

            UIUtil.logActivity("Admin created fitness challenge");
            loadData();

        } catch (Exception ex) {
            UIUtil.error(this, "Invalid challenge data.");
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        if (row == -1) {
            UIUtil.error(this, "Select a challenge first.");
            return null;
        }
        return Integer.parseInt(model.getValueAt(table.convertRowIndexToModel(row), 0).toString());
    }

    private void setStatus(String status) {
        Integer id = selectedId();
        if (id == null) return;

        String sql = "UPDATE challenges SET status=? WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
            UIUtil.logActivity("Admin changed fitness content status to " + status);
            loadData();
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }
}
