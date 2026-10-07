import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    private final JPanel content = new JPanel(new BorderLayout());

    public AdminDashboard() {
        setTitle("FitTrack - Admin Dashboard");
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUI();
        showHome();
    }

    private void buildUI() {
        JPanel nav = new JPanel(new GridLayout(0, 1, 5, 5));
        nav.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        nav.setPreferredSize(new Dimension(210, 0));

        JLabel brand = new JLabel("FITTRACK ADMIN");
        brand.setFont(new Font("SansSerif", Font.BOLD, 17));
        nav.add(brand);

        addNav(nav, "Dashboard", e -> showHome());
        addNav(nav, "User Management", e -> showPanel(new UserManagementPanel()));
        addNav(nav, "Fitness Content", e -> showPanel(new ContentManagementPanel()));
        addNav(nav, "System Settings", e -> showPanel(new SettingsPanel()));
        addNav(nav, "Statistics", e -> showPanel(new StatisticsPanel()));
        addNav(nav, "Activity Monitoring", e -> showPanel(new ActivityPanel()));

        JButton logout = UIUtil.button("Logout");
        logout.addActionListener(e -> {
            UIUtil.logActivity("Admin logged out");
            Session.clear();
            dispose();
            new LoginFrame().setVisible(true);
        });

        nav.add(Box.createVerticalStrut(20));
        nav.add(logout);

        add(nav, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    private void addNav(JPanel nav, String text, java.awt.event.ActionListener action) {
        JButton b = UIUtil.button(text);
        b.addActionListener(action);
        nav.add(b);
    }

    private void showPanel(JPanel p) {
        content.removeAll();
        content.add(p, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private void showHome() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.add(UIUtil.title("Admin Dashboard"), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 12));
        JLabel users = new JLabel("0");
        JLabel workouts = new JLabel("0");
        JLabel challenges = new JLabel("0");
        JLabel participants = new JLabel("0");

        cards.add(card("Users", users));
        cards.add(card("Workouts", workouts));
        cards.add(card("Challenges", challenges));
        cards.add(card("Participants", participants));

        p.add(cards, BorderLayout.CENTER);

        String sqlUsers = "SELECT COUNT(*) FROM users";
        String sqlWorkouts = "SELECT COUNT(*) FROM workouts";
        String sqlChallenges = "SELECT COUNT(*) FROM challenges";
        String sqlParticipants = "SELECT COUNT(*) FROM challenge_participants";

        try (var con = DBConnection.getConnection()) {
            users.setText(String.valueOf(singleCount(con, sqlUsers)));
            workouts.setText(String.valueOf(singleCount(con, sqlWorkouts)));
            challenges.setText(String.valueOf(singleCount(con, sqlChallenges)));
            participants.setText(String.valueOf(singleCount(con, sqlParticipants)));
        } catch (Exception ex) {
            UIUtil.error(this, ex.getMessage());
        }

        showPanel(p);
    }

    private long singleCount(java.sql.Connection con, String sql) throws java.sql.SQLException {
        try (var ps = con.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private JPanel card(String title, JLabel value) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220)),
                BorderFactory.createEmptyBorder(18,18,18,18)
        ));
        JLabel t = new JLabel(title);
        t.setForeground(Color.GRAY);
        value.setFont(new Font("SansSerif", Font.BOLD, 27));
        p.add(t);
        p.add(value);
        return p;
    }
}
