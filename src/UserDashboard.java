import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class UserDashboard extends JFrame {
    private final JPanel content = new JPanel(new BorderLayout());

    public UserDashboard() {
        setTitle("FitTrack - User Dashboard");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUI();
        showHome();
    }

    private void buildUI() {
        JPanel nav = new JPanel(new GridLayout(0, 1, 5, 5));
        nav.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        nav.setPreferredSize(new Dimension(190, 0));

        JLabel brand = new JLabel("FITTRACK");
        brand.setFont(new Font("SansSerif", Font.BOLD, 18));
        nav.add(brand);

        addNav(nav, "Dashboard", e -> showHome());
        addNav(nav, "Workout Log", e -> showPanel(new WorkoutPanel()));
        addNav(nav, "Progress", e -> showPanel(new ProgressPanel()));
        addNav(nav, "Challenges", e -> showPanel(new ChallengePanel()));
        addNav(nav, "Profile", e -> showPanel(new ProfilePanel()));
        addNav(nav, "Challenge History", e -> showPanel(new ChallengeHistoryPanel()));

        JButton logout = UIUtil.button("Logout");
        logout.addActionListener(e -> logout());
        nav.add(Box.createVerticalStrut(20));
        nav.add(logout);

        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(nav, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    private void addNav(JPanel nav, String text, java.awt.event.ActionListener listener) {
        JButton button = UIUtil.button(text);
        button.addActionListener(listener);
        nav.add(button);
    }

    private void showPanel(JPanel panel) {
        content.removeAll();
        content.add(panel, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private void showHome() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.add(UIUtil.title("Welcome, " + Session.getName()), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 15, 15));
        cards.add(statCard("Total Workouts", "0"));
        cards.add(statCard("Workout Minutes", "0"));
        cards.add(statCard("Calories Burned", "0"));
        panel.add(cards, BorderLayout.CENTER);

        loadStats(cards);
        showPanel(panel);
    }

    private JPanel statCard(String title, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220)),
                BorderFactory.createEmptyBorder(20,20,20,20)
        ));
        JLabel t = new JLabel(title);
        t.setForeground(Color.GRAY);
        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 28));
        p.add(t);
        p.add(v);
        return p;
    }

    private void loadStats(JPanel cards) {
        String sql = "SELECT COUNT(*) workouts, " +
                "COALESCE(SUM(duration),0) minutes, COALESCE(SUM(calories),0) calories " +
                "FROM workouts WHERE user_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Session.getUserId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ((JLabel)((JPanel)cards.getComponent(0)).getComponent(1))
                            .setText(String.valueOf(rs.getInt("workouts")));
                    ((JLabel)((JPanel)cards.getComponent(1)).getComponent(1))
                            .setText(String.valueOf(rs.getInt("minutes")));
                    ((JLabel)((JPanel)cards.getComponent(2)).getComponent(1))
                            .setText(String.valueOf(rs.getInt("calories")));
                }
            }
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }

    private void logout() {
        UIUtil.logActivity("User logged out");
        Session.clear();
        dispose();
        new LoginFrame().setVisible(true);
    }
}
