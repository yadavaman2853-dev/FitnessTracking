import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WorkoutPanel extends JPanel {
    private final DefaultTableModel model =
            UIUtil.model("ID", "Date", "Type", "Duration", "Intensity", "Calories", "Notes");
    private final JTable table = new JTable(model);

    public WorkoutPanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.add(UIUtil.title("Workout Log"), BorderLayout.WEST);

        JButton add = UIUtil.button("Add Workout");
        JButton delete = UIUtil.button("Delete Selected");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(add);
        actions.add(delete);
        top.add(actions, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        UIUtil.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> addWorkout());
        delete.addActionListener(e -> deleteWorkout());

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        String sql = "SELECT id, workout_date, workout_type, duration, intensity, calories, notes " +
                "FROM workouts WHERE user_id=? ORDER BY workout_date DESC, id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Session.getUserId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getDate("workout_date"),
                            rs.getString("workout_type"),
                            rs.getInt("duration") + " min",
                            rs.getString("intensity"),
                            rs.getInt("calories"),
                            rs.getString("notes")
                    });
                }
            }
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }

    private void addWorkout() {
        JTextField type = new JTextField();
        JTextField duration = new JTextField();
        JComboBox<String> intensity = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        JTextField calories = new JTextField("0");
        JTextField date = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        JTextArea notes = new JTextArea(3, 20);

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Workout Type"));
        p.add(type);
        p.add(new JLabel("Duration (minutes)"));
        p.add(duration);
        p.add(new JLabel("Intensity"));
        p.add(intensity);
        p.add(new JLabel("Calories"));
        p.add(calories);
        p.add(new JLabel("Date (yyyy-MM-dd)"));
        p.add(date);
        p.add(new JLabel("Notes"));
        p.add(new JScrollPane(notes));

        int result = JOptionPane.showConfirmDialog(
                this, p, "Add Workout", JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        try {
            int minutes = Integer.parseInt(duration.getText().trim());
            int cal = Integer.parseInt(calories.getText().trim());

            if (minutes <= 0 || cal < 0 || type.getText().trim().isEmpty()) {
                throw new NumberFormatException();
            }

            Date sqlDate = java.sql.Date.valueOf(date.getText().trim());

            String sql = "INSERT INTO workouts " +
                    "(user_id, workout_type, duration, intensity, calories, workout_date, notes) " +
                    "VALUES (?,?,?,?,?,?,?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, Session.getUserId());
                ps.setString(2, type.getText().trim());
                ps.setInt(3, minutes);
                ps.setString(4, intensity.getSelectedItem().toString());
                ps.setInt(5, cal);
                ps.setDate(6, (java.sql.Date) sqlDate);
                ps.setString(7, notes.getText().trim());
                ps.executeUpdate();
            }

            UIUtil.logActivity("Added workout: " + type.getText().trim());
            UIUtil.info(this, "Workout logged successfully.");
            loadData();

        } catch (Exception ex) {
            UIUtil.error(this, "Invalid workout data.\n" + ex.getMessage());
        }
    }

    private void deleteWorkout() {
        int row = table.getSelectedRow();
        if (row == -1) {
            UIUtil.error(this, "Select a workout first.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(table.convertRowIndexToModel(row), 0).toString());

        String sql = "DELETE FROM workouts WHERE id=? AND user_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, Session.getUserId());
            ps.executeUpdate();
            UIUtil.logActivity("Deleted workout");
            loadData();
        } catch (SQLException ex) {
            UIUtil.error(this, ex.getMessage());
        }
    }
}
