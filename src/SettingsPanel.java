import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SettingsPanel extends JPanel {
    private final DefaultTableModel model =
            UIUtil.model("Setting", "Value");
    private final JTable table = new JTable(model);

    public SettingsPanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.add(UIUtil.title("System Settings"), BorderLayout.WEST);
        JButton update = UIUtil.button("Update Selected");
        top.add(update, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        UIUtil.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        update.addActionListener(e -> updateSetting());
        loadData();
    }

    private void loadData() {
        model.setRowCount(0);

        String sql = "SELECT setting_key,setting_value FROM system_settings ORDER BY setting_key";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("setting_key"),
                        rs.getString("setting_value")
                });
            }
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }

    private void updateSetting() {
        int row = table.getSelectedRow();
        if (row == -1) {
            UIUtil.error(this, "Select a setting first.");
            return;
        }

        int r = table.convertRowIndexToModel(row);
        String key = model.getValueAt(r, 0).toString();
        String current = model.getValueAt(r, 1).toString();

        String value = JOptionPane.showInputDialog(this,
                "Enter new value for " + key, current);

        if (value == null) return;

        String sql = "UPDATE system_settings SET setting_value=? WHERE setting_key=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value.trim());
            ps.setString(2, key);
            ps.executeUpdate();

            UIUtil.logActivity("Admin updated setting: " + key);
            loadData();

        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }
}
