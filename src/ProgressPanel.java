import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProgressPanel extends JPanel {
    private final DefaultTableModel model =
            UIUtil.model("ID", "Date", "Weight (kg)", "Steps", "Workout Minutes");
    private final JTable table = new JTable(model);

    public ProgressPanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.add(UIUtil.title("Progress Tracking"), BorderLayout.WEST);
        JButton add = UIUtil.button("Record Progress");
        top.add(add, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        UIUtil.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> addProgress());
        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        String sql = "SELECT id, recorded_at, weight, steps, workout_minutes " +
                "FROM progress WHERE user_id=? ORDER BY recorded_at DESC, id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Session.getUserId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getDate("recorded_at"),
                            rs.getObject("weight"),
                            rs.getInt("steps"),
                            rs.getInt("workout_minutes")
                    });
                }
            }
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }

    private void addProgress() {
        JTextField date = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        JTextField weight = new JTextField();
        JTextField steps = new JTextField("0");
        JTextField minutes = new JTextField("0");

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Date"));
        p.add(date);
        p.add(new JLabel("Weight (kg)"));
        p.add(weight);
        p.add(new JLabel("Steps"));
        p.add(steps);
        p.add(new JLabel("Workout Minutes"));
        p.add(minutes);

        if (JOptionPane.showConfirmDialog(this, p, "Record Progress",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            Date d = java.sql.Date.valueOf(date.getText().trim());
            double w = weight.getText().trim().isEmpty()
                    ? 0 : Double.parseDouble(weight.getText().trim());
            int s = Integer.parseInt(steps.getText().trim());
            int m = Integer.parseInt(minutes.getText().trim());

            if (s < 0 || m < 0 || w < 0) throw new NumberFormatException();

            String sql = "INSERT INTO progress(user_id, weight, steps, workout_minutes, recorded_at) " +
                    "VALUES(?,?,?,?,?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, Session.getUserId());
                if (weight.getText().trim().isEmpty()) ps.setNull(2, Types.DECIMAL);
                else ps.setDouble(2, w);
                ps.setInt(3, s);
                ps.setInt(4, m);
                ps.setDate(5, (java.sql.Date) d);
                ps.executeUpdate();
            }

            UIUtil.logActivity("Recorded fitness progress");
            UIUtil.info(this, "Progress saved successfully.");
            loadData();

        } catch (Exception ex) {
            UIUtil.error(this, "Invalid progress data.");
        }
    }
}
