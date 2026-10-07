import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ActivityPanel extends JPanel {
    private final DefaultTableModel model =
            UIUtil.model("ID", "User", "Action", "Time");
    private final JTable table = new JTable(model);

    public ActivityPanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.add(UIUtil.title("System Activity Monitoring"), BorderLayout.WEST);
        JButton refresh = UIUtil.button("Refresh");
        top.add(refresh, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        UIUtil.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh.addActionListener(e -> loadData());
        loadData();
    }

    private void loadData() {
        model.setRowCount(0);

        String sql = """
                SELECT a.id, COALESCE(u.name, 'System') AS user_name,
                       a.action, a.created_at
                FROM activity_log a
                LEFT JOIN users u ON u.id = a.user_id
                ORDER BY a.created_at DESC
                LIMIT 200
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("user_name"),
                        rs.getString("action"),
                        rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }
}
