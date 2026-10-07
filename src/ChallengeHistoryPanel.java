import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ChallengeHistoryPanel extends JPanel {
    private final DefaultTableModel model =
            UIUtil.model("Challenge", "Target", "Unit", "My Progress", "Joined On");
    private final JTable table = new JTable(model);

    public ChallengeHistoryPanel() {
        setLayout(new BorderLayout(10, 10));
        add(UIUtil.title("Challenge History"), BorderLayout.NORTH);
        UIUtil.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);
        loadData();
    }

    private void loadData() {
        model.setRowCount(0);

        String sql = """
                SELECT c.title, c.target, c.unit, cp.progress, cp.joined_at
                FROM challenge_participants cp
                JOIN challenges c ON c.id = cp.challenge_id
                WHERE cp.user_id=?
                ORDER BY cp.joined_at DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Session.getUserId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("title"),
                            rs.getInt("target"),
                            rs.getString("unit"),
                            rs.getInt("progress"),
                            rs.getTimestamp("joined_at")
                    });
                }
            }
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }
}
