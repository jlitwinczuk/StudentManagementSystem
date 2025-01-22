import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

// Klasa reprezentująca studenta
class Student {
    private String name;
    private int age;
    private double grade;
    private String studentID;

    public Student(String name, int age, double grade, String studentID) {
        this.name = name;
        this.age = age;
        this.grade = grade;
        this.studentID = studentID;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getGrade() { return grade; }
    public void setGrade(double grade) { this.grade = grade; }

    public String getStudentID() { return studentID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }

    public void displayInfo() {
        System.out.println("Student ID: " + studentID);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Grade: " + grade);
    }
}

// Interfejs zarządzania studentami
interface StudentManager {
    void addStudent(Student student);
    void removeStudent(String studentID);
    void updateStudent(String studentID, Student updatedStudent);
    ArrayList<Student> displayAllStudents();
    double calculateAverageGrade();
}

// Implementacja interfejsu StudentManager z użyciem bazy danych SQLite
class StudentManagerImpl implements StudentManager {
    private Connection connection;

    public StudentManagerImpl() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:students.db");
            createTableIfNotExists();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Tworzenie tabeli w bazie danych, jeśli ta nie istnieje
    private void createTableIfNotExists() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS students (" +
                "name TEXT, " +
                "age INTEGER, " +
                "grade REAL, " +
                "studentID TEXT PRIMARY KEY)";
        Statement stmt = connection.createStatement();
        stmt.execute(createTableSQL);
    }

    @Override
    public void addStudent(Student student) {
        String insertSQL = "INSERT INTO students (name, age, grade, studentID) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, student.getName());
            pstmt.setInt(2, student.getAge());
            pstmt.setDouble(3, student.getGrade());
            pstmt.setString(4, student.getStudentID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeStudent(String studentID) {
        String deleteSQL = "DELETE FROM students WHERE studentID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {
            pstmt.setString(1, studentID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateStudent(String studentID, Student updatedStudent) {
        String updateSQL = "UPDATE students SET name = ?, age = ?, grade = ? WHERE studentID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, updatedStudent.getName());
            pstmt.setInt(2, updatedStudent.getAge());
            pstmt.setDouble(3, updatedStudent.getGrade());
            pstmt.setString(4, studentID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<Student> displayAllStudents() {
        ArrayList<Student> students = new ArrayList<>();
        String selectSQL = "SELECT * FROM students";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                String name = rs.getString("name");
                int age = rs.getInt("age");
                double grade = rs.getDouble("grade");
                String studentID = rs.getString("studentID");
                students.add(new Student(name, age, grade, studentID));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    @Override
    public double calculateAverageGrade() {
        String avgSQL = "SELECT AVG(grade) AS averageGrade FROM students";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(avgSQL)) {
            if (rs.next()) {
                return rs.getDouble("averageGrade");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}

public class StudentManagementSystem {
    private JFrame frame;
    private JTextField nameField, ageField, gradeField, studentIDField;
    private JTextArea outputArea;
    private final StudentManagerImpl manager;

    public StudentManagementSystem() {
        manager = new StudentManagerImpl();
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Student Management System");
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Panel z lewej strony dla wprowadzania danych
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        // Panel do wprowadzania danych studenta
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2, 5, 5));

        inputPanel.add(new JLabel("Student ID:"));
        studentIDField = new JTextField();
        inputPanel.add(studentIDField);

        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Age:"));
        ageField = new JTextField();
        inputPanel.add(ageField);

        inputPanel.add(new JLabel("Grade:"));
        gradeField = new JTextField();
        inputPanel.add(gradeField);

        leftPanel.add(inputPanel, BorderLayout.NORTH);

        // Panel z przyciskami
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 1, 5, 5));

        JButton addButton = new JButton("Add Student");
        JButton removeButton = new JButton("Remove Student");
        JButton updateButton = new JButton("Update Student");
        JButton displayButton = new JButton("Display All Students");
        JButton averageButton = new JButton("Calculate Average");

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(displayButton);
        buttonPanel.add(averageButton);

        leftPanel.add(buttonPanel, BorderLayout.CENTER);

        // Obsługa przycisków
        addButton.addActionListener(e -> addStudent());
        removeButton.addActionListener(e -> removeStudent());
        updateButton.addActionListener(e -> updateStudent());
        displayButton.addActionListener(e -> displayAllStudents());
        averageButton.addActionListener(e -> calculateAverage());

        // Panel z prawej strony do wyświetlania informacji
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        rightPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // Podział paneli na lewy i prawy
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(300);

        frame.add(splitPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }
    private void addStudent() {
        try {
            String name = nameField.getText();
            int age = Integer.parseInt(ageField.getText());
            double grade = Double.parseDouble(gradeField.getText());
            String studentID = studentIDField.getText();

            if (age <= 0 || grade < 0 || grade > 100) {
                outputArea.setText("Invalid input: Age must be > 0 and Grade between 0-100.");
                return;
            }

            Student student = new Student(name, age, grade, studentID);
            manager.addStudent(student);
            outputArea.setText("Student added successfully.");
        } catch (Exception e) {
            outputArea.setText("Error: " + e.getMessage());
        }
    }

    private void removeStudent() {
        String studentID = studentIDField.getText();
        manager.removeStudent(studentID);
        outputArea.setText("Student removed successfully.");
    }

    private void updateStudent() {
        try {
            String name = nameField.getText();
            int age = Integer.parseInt(ageField.getText());
            double grade = Double.parseDouble(gradeField.getText());
            String studentID = studentIDField.getText();

            Student updatedStudent = new Student(name, age, grade, studentID);

            // Sprawdzenie czy student istnieje
            ArrayList<Student> students = manager.displayAllStudents();
            boolean studentExists = students.stream().anyMatch(s -> s.getStudentID().equals(studentID));

            if (studentExists) {
                manager.updateStudent(studentID, updatedStudent);
                outputArea.setText("Student updated successfully.");
            } else {
                outputArea.setText("No student with the given ID found");
            }
        } catch (Exception e) {
            outputArea.setText("Error: " + e.getMessage());
        }
    }

    private void displayAllStudents() {
        ArrayList<Student> students = manager.displayAllStudents();
        StringBuilder sb = new StringBuilder();
        for (Student student : students) {
            sb.append("ID: ").append(student.getStudentID()).append(", Name: ")
                    .append(student.getName()).append(", Age: ").append(student.getAge())
                    .append(", Grade: ").append(student.getGrade()).append("\n");
        }
        outputArea.setText(sb.toString());
    }

    private void calculateAverage() {
        double average = manager.calculateAverageGrade();
        outputArea.setText("Average Grade: " + average);
    }

    public static void main(String[] args) {
        new StudentManagementSystem();
    }
}
