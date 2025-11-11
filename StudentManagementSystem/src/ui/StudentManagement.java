/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author asath
 */
package ui;

import db.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentManagement extends JFrame implements ActionListener {
    JTextField tfName, tfAge, tfCourse;
    JButton btnAdd, btnView, btnUpdate, btnDelete;
    JTable table;
    DefaultTableModel model;
    Connection con;

    public StudentManagement() {
        setTitle("Student Management System");
        setSize(700, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        con = DatabaseConnection.getConnection();

        // Top panel for form
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Name:"));
        tfName = new JTextField();
        panel.add(tfName);

        panel.add(new JLabel("Age:"));
        tfAge = new JTextField();
        panel.add(tfAge);

        panel.add(new JLabel("Course:"));
        tfCourse = new JTextField();
        panel.add(tfCourse);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnAdd = new JButton("Add");
        btnView = new JButton("View All");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");

        btnPanel.add(btnAdd);
        btnPanel.add(btnView);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);

        add(panel, BorderLayout.NORTH);
        add(btnPanel, BorderLayout.SOUTH);

        // Table
        model = new DefaultTableModel(new String[]{"ID", "Name", "Age", "Course"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Event listeners
        btnAdd.addActionListener(this);
        btnView.addActionListener(this);
        btnUpdate.addActionListener(this);
        btnDelete.addActionListener(this);

        // When row is clicked, fill text fields
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                tfName.setText(model.getValueAt(row, 1).toString());
                tfAge.setText(model.getValueAt(row, 2).toString());
                tfCourse.setText(model.getValueAt(row, 3).toString());
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdd) addStudent();
        else if (e.getSource() == btnView) viewStudents();
        else if (e.getSource() == btnUpdate) updateStudent();
        else if (e.getSource() == btnDelete) deleteStudent();
    }

    // Add
    void addStudent() {
        try {
            String name = tfName.getText();
            int age = Integer.parseInt(tfAge.getText());
            String course = tfCourse.getText();

            if (name.isEmpty() || course.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO students(name, age, course) VALUES (?, ?, ?)");
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, course);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student Added Successfully!");
            clearFields();
            viewStudents(); // Refresh table
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Age must be a number.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // View
    void viewStudents() {
        try {
            model.setRowCount(0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM students");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("course")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // Update
    void updateStudent() {
        try {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a student to update");
                return;
            }

            int id = (int) model.getValueAt(selectedRow, 0);
            String name = tfName.getText();
            int age = Integer.parseInt(tfAge.getText());
            String course = tfCourse.getText();

            if (name.isEmpty() || course.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                "UPDATE students SET name=?, age=?, course=? WHERE id=?");
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, course);
            ps.setInt(4, id);
            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Student Updated Successfully!");
                clearFields();
                viewStudents();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Age must be a number.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // Delete
    void deleteStudent() {
        try {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a student to delete");
                return;
            }

            int id = (int) model.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to delete this student?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            PreparedStatement ps = con.prepareStatement("DELETE FROM students WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student Deleted Successfully!");
            clearFields();
            viewStudents();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // Clear input fields
    void clearFields() {
        tfName.setText("");
        tfAge.setText("");
        tfCourse.setText("");
    }
}
