import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class StatisticsPanel extends JPanel {
    private final JLabel users = valueLabel();
    private final JLabel workouts = valueLabel();
    private final JLabel minutes = valueLabel();
    private final JLabel calories = valueLabel();
    private final JLabel participants = valueLabel();

    public StatisticsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(UIUtil.title("Fitness Statistics"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 15, 15));
        grid.add(card("Registered Users", users));
        grid.add(card("Workout Logs", workouts));
        grid.add(card("Workout Minutes", minutes));
        grid.add(card("Calories Burned", calories));
        grid.add(card("Challenge Participants", participants));

        add(grid, BorderLayout.CENTER);

        loadStats();
    }

    private JLabel valueLabel() {
        JLabel l = new JLabel("0", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 28));
        return l;
    }

    private JPanel card(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(5, 10));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220)),
                BorderFactory.createEmptyBorder(20,20,20,20)
        ));
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setForeground(Color.GRAY);
        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private void loadStats() {
        try (Connection con = DBConnection.getConnection()) {
            users.setText(String.valueOf(count(con, "SELECT COUNT(*) FROM users")));
            workouts.setText(String.valueOf(count(con, "SELECT COUNT(*) FROM workouts")));
            minutes.setText(String.valueOf(count(con, "SELECT COALESCE(SUM(duration),0) FROM workouts")));
            calories.setText(String.valueOf(count(con, "SELECT COALESCE(SUM(calories),0) FROM workouts")));
            participants.setText(String.valueOf(count(con, "SELECT COUNT(*) FROM challenge_participants")));
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }

    private long count(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
