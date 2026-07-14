import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * CodeAlpha - Task 1: Student Grade Tracker
 *
 * Console-based program to input and manage student grades.
 * Stores students and their grades using ArrayLists, calculates
 * average / highest / lowest per student and for the whole class,
 * and displays a full summary report.
 */
public class StudentGradeTracker {

    // ---------- Student model ----------
    static class Student {
        private final String name;
        private final List<Double> grades = new ArrayList<>();

        Student(String name) {
            this.name = name;
        }

        void addGrade(double grade) {
            grades.add(grade);
        }

        String getName() {
            return name;
        }

        List<Double> getGrades() {
            return grades;
        }

        double getAverage() {
            if (grades.isEmpty()) return 0.0;
            double sum = 0;
            for (double g : grades) sum += g;
            return sum / grades.size();
        }

        double getHighest() {
            if (grades.isEmpty()) return 0.0;
            double max = grades.get(0);
            for (double g : grades) if (g > max) max = g;
            return max;
        }

        double getLowest() {
            if (grades.isEmpty()) return 0.0;
            double min = grades.get(0);
            for (double g : grades) if (g < min) min = g;
            return min;
        }

        String getLetterGrade() {
            double avg = getAverage();
            if (avg >= 90) return "A";
            if (avg >= 80) return "B";
            if (avg >= 70) return "C";
            if (avg >= 60) return "D";
            return "F";
        }
    }

    // ---------- Program state ----------
    private static final List<Student> students = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("      STUDENT GRADE TRACKER (CodeAlpha)");
        System.out.println("========================================");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addStudent();
                    break;
                case "2":
                    addGradeToStudent();
                    break;
                case "3":
                    viewStudentReport();
                    break;
                case "4":
                    viewClassSummary();
                    break;
                case "5":
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1. Add new student");
        System.out.println("2. Add grade to a student");
        System.out.println("3. View single student report");
        System.out.println("4. View full class summary report");
        System.out.println("5. Exit");
        System.out.print("Enter choice: ");
    }

    private static void addStudent() {
        System.out.print("Enter student name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        if (findStudent(name) != null) {
            System.out.println("A student with this name already exists.");
            return;
        }
        students.add(new Student(name));
        System.out.println("Student \"" + name + "\" added.");
    }

    private static void addGradeToStudent() {
        if (students.isEmpty()) {
            System.out.println("No students yet. Add a student first.");
            return;
        }
        System.out.print("Enter student name: ");
        String name = scanner.nextLine().trim();
        Student s = findStudent(name);
        if (s == null) {
            System.out.println("Student not found.");
            return;
        }
        System.out.print("Enter grade (0-100): ");
        try {
            double grade = Double.parseDouble(scanner.nextLine().trim());
            if (grade < 0 || grade > 100) {
                System.out.println("Grade must be between 0 and 100.");
                return;
            }
            s.addGrade(grade);
            System.out.println("Grade " + grade + " added to " + name + ".");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
        }
    }

    private static void viewStudentReport() {
        if (students.isEmpty()) {
            System.out.println("No students yet.");
            return;
        }
        System.out.print("Enter student name: ");
        String name = scanner.nextLine().trim();
        Student s = findStudent(name);
        if (s == null) {
            System.out.println("Student not found.");
            return;
        }
        System.out.println("\n--- Report for " + s.getName() + " ---");
        System.out.println("Grades: " + s.getGrades());
        System.out.printf("Average : %.2f%n", s.getAverage());
        System.out.printf("Highest : %.2f%n", s.getHighest());
        System.out.printf("Lowest  : %.2f%n", s.getLowest());
        System.out.println("Letter Grade: " + s.getLetterGrade());
    }

    private static void viewClassSummary() {
        if (students.isEmpty()) {
            System.out.println("No students yet.");
            return;
        }

        System.out.println("\n=========== CLASS SUMMARY REPORT ===========");
        System.out.printf("%-15s %-10s %-10s %-10s %-8s%n",
                "Name", "Average", "Highest", "Lowest", "Grade");
        System.out.println("----------------------------------------------");

        double classSum = 0;
        int gradeCount = 0;
        Double classHighest = null;
        Double classLowest = null;

        for (Student s : students) {
            System.out.printf("%-15s %-10.2f %-10.2f %-10.2f %-8s%n",
                    s.getName(), s.getAverage(), s.getHighest(), s.getLowest(), s.getLetterGrade());

            for (double g : s.getGrades()) {
                classSum += g;
                gradeCount++;
                if (classHighest == null || g > classHighest) classHighest = g;
                if (classLowest == null || g < classLowest) classLowest = g;
            }
        }

        System.out.println("----------------------------------------------");
        if (gradeCount > 0) {
            System.out.printf("Class Average : %.2f%n", classSum / gradeCount);
            System.out.printf("Class Highest : %.2f%n", classHighest);
            System.out.printf("Class Lowest  : %.2f%n", classLowest);
        } else {
            System.out.println("No grades recorded yet.");
        }
        System.out.println("Total Students: " + students.size());
        System.out.println("===============================================");
    }

    private static Student findStudent(String name) {
        for (Student s : students) {
            if (s.getName().equalsIgnoreCase(name)) return s;
        }
        return null;
    }
}
