import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UIUtil {
    private UIUtil() {}

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        return b;
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        return label;
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "FitTrack", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void logActivity(String action) {
        String sql = "INSERT INTO activity_log(user_id, action) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Session.getUserId() == 0 ? 0 : Session.getUserId());
            ps.setString(2, action);
            ps.executeUpdate();
        } catch (SQLException ignored) {
            // Activity logging should not stop the main operation.
        }
    }

    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setAutoCreateRowSorter(true);
    }
}
