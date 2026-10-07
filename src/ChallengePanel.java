import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ChallengePanel extends JPanel {
    private final DefaultTableModel model =
            UIUtil.model("ID", "Challenge", "Description", "Target", "Unit", "Status", "My Progress");
    private final JTable table = new JTable(model);

    public ChallengePanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.add(UIUtil.title("Fitness Challenges"), BorderLayout.WEST);

        JButton join = UIUtil.button("Join Selected");
        JButton update = UIUtil.button("Update Progress");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(join);
        actions.add(update);
        top.add(actions, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        UIUtil.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        join.addActionListener(e -> joinChallenge());
        update.addActionListener(e -> updateProgress());

        loadData();
        
    }

    private void loadData() {
        model.setRowCount(0);

        String sql = """
                SELECT c.id, c.title, c.description, c.target, c.unit, c.status,
                       COALESCE(cp.progress, 0) AS my_progress
                FROM challenges c
                LEFT JOIN challenge_participants cp
                  ON c.id = cp.challenge_id AND cp.user_id = ?
                WHERE c.status = 'APPROVED'
                ORDER BY c.id DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Session.getUserId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("target"),
                            rs.getString("unit"),
                            rs.getString("status"),
                            rs.getInt("my_progress")
                    });
                }
            }
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }

    private int selectedId() {
        int row = table.getSelectedRow();
        if (row == -1) {
            UIUtil.error(this, "Select a challenge first.");
            return -1;
        }
        return Integer.parseInt(model.getValueAt(table.convertRowIndexToModel(row), 0).toString());
    }

    private void joinChallenge() {
        int challengeId = selectedId();
        if (challengeId == -1) return;

        String sql = "INSERT INTO challenge_participants(user_id, challenge_id) VALUES(?,?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Session.getUserId());
            ps.setInt(2, challengeId);
            ps.executeUpdate();

            UIUtil.logActivity("Joined fitness challenge");
            UIUtil.info(this, "Challenge joined successfully.");
            loadData();

        } catch (SQLIntegrityConstraintViolationException ex) {
            UIUtil.info(this, "You have already joined this challenge.");
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }

    private void updateProgress() {
        int row = table.getSelectedRow();
        if (row == -1) {
            UIUtil.error(this, "Select a challenge first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        int challengeId = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
        int target = Integer.parseInt(model.getValueAt(modelRow, 3).toString());

        String input = JOptionPane.showInputDialog(
                this,
                "Enter your progress (maximum " + target + "):",
                model.getValueAt(modelRow, 6)
        );

        if (input == null) return;

        try {
            int progress = Integer.parseInt(input);
            if (progress < 0 || progress > target) throw new NumberFormatException();

            String sql = "UPDATE challenge_participants SET progress=? " +
                    "WHERE user_id=? AND challenge_id=?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, progress);
                ps.setInt(2, Session.getUserId());
                ps.setInt(3, challengeId);

                if (ps.executeUpdate() == 0) {
                    UIUtil.error(this, "Join the challenge before updating progress.");
                    return;
                }
            }

            UIUtil.logActivity("Updated challenge progress");
            loadData();

        } catch (NumberFormatException ex) {
            UIUtil.error(this, "Enter a valid number between 0 and " + target + ".");
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }
}
