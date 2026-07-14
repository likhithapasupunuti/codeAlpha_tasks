import java.io.*;
import java.util.*;

/**
 * CodeAlpha - Task 4: Hotel Reservation System
 *
 * Console-based system to search, book, and manage hotel rooms.
 * Features:
 *  - Room categorization (Standard, Deluxe, Suite)
 *  - Search available rooms by category
 *  - Make and cancel reservations
 *  - Payment simulation
 *  - Booking details view
 *  - File I/O to persist rooms & reservations between runs
 */
public class HotelReservationSystem {

    // ---------- Room category ----------
    enum RoomCategory {
        STANDARD(100.0),
        DELUXE(180.0),
        SUITE(320.0);

        final double basePrice;

        RoomCategory(double basePrice) {
            this.basePrice = basePrice;
        }
    }

    // ---------- Room model ----------
    static class Room implements Serializable {
        int roomNumber;
        RoomCategory category;
        boolean available;

        Room(int roomNumber, RoomCategory category) {
            this.roomNumber = roomNumber;
            this.category = category;
            this.available = true;
        }

        double pricePerNight() {
            return category.basePrice;
        }

        @Override
        public String toString() {
            return String.format("Room %-4d | %-8s | $%.2f/night | %s",
                    roomNumber, category, pricePerNight(), available ? "Available" : "Booked");
        }
    }

    // ---------- Reservation model ----------
    static class Reservation implements Serializable {
        int reservationId;
        String guestName;
        int roomNumber;
        RoomCategory category;
        int nights;
        double totalAmount;
        boolean paid;
        boolean cancelled;

        Reservation(int reservationId, String guestName, int roomNumber,
                    RoomCategory category, int nights, double totalAmount) {
            this.reservationId = reservationId;
            this.guestName = guestName;
            this.roomNumber = roomNumber;
            this.category = category;
            this.nights = nights;
            this.totalAmount = totalAmount;
            this.paid = false;
            this.cancelled = false;
        }

        @Override
        public String toString() {
            return String.format(
                    "Reservation #%d | Guest: %-12s | Room %d (%s) | %d night(s) | Total: $%.2f | %s%s",
                    reservationId, guestName, roomNumber, category, nights, totalAmount,
                    paid ? "PAID" : "UNPAID",
                    cancelled ? " | CANCELLED" : "");
        }
    }

    // ---------- Hotel (manages rooms & reservations) ----------
    static class Hotel implements Serializable {
        List<Room> rooms = new ArrayList<>();
        List<Reservation> reservations = new ArrayList<>();
        int nextReservationId = 1;

        void initializeRooms() {
            int roomNum = 101;
            for (int i = 0; i < 5; i++) rooms.add(new Room(roomNum++, RoomCategory.STANDARD));
            for (int i = 0; i < 3; i++) rooms.add(new Room(roomNum++, RoomCategory.DELUXE));
            for (int i = 0; i < 2; i++) rooms.add(new Room(roomNum++, RoomCategory.SUITE));
        }

        List<Room> searchAvailable(RoomCategory category) {
            List<Room> result = new ArrayList<>();
            for (Room r : rooms) {
                if (r.available && (category == null || r.category == category)) {
                    result.add(r);
                }
            }
            return result;
        }

        Room findRoom(int roomNumber) {
            for (Room r : rooms) if (r.roomNumber == roomNumber) return r;
            return null;
        }

        Reservation book(String guestName, int roomNumber, int nights) {
            Room room = findRoom(roomNumber);
            if (room == null || !room.available) return null;
            double total = room.pricePerNight() * nights;
            Reservation res = new Reservation(nextReservationId++, guestName, roomNumber,
                    room.category, nights, total);
            room.available = false;
            reservations.add(res);
            return res;
        }

        boolean cancel(int reservationId) {
            for (Reservation res : reservations) {
                if (res.reservationId == reservationId && !res.cancelled) {
                    res.cancelled = true;
                    Room room = findRoom(res.roomNumber);
                    if (room != null) room.available = true;
                    return true;
                }
            }
            return false;
        }

        Reservation findReservation(int id) {
            for (Reservation r : reservations) if (r.reservationId == id) return r;
            return null;
        }

        boolean pay(int reservationId) {
            Reservation res = findReservation(reservationId);
            if (res == null || res.cancelled || res.paid) return false;
            res.paid = true; // payment simulation - no real transaction
            return true;
        }
    }

    // ---------- Persistence ----------
    private static final String SAVE_FILE = "hotel_data.dat";

    static void saveHotel(Hotel hotel) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(hotel);
            System.out.println("Hotel data saved to " + SAVE_FILE);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    static Hotel loadHotel() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) {
            Hotel hotel = new Hotel();
            hotel.initializeRooms();
            System.out.println("No saved data found. Initialized a new hotel with default rooms.");
            return hotel;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Hotel hotel = (Hotel) ois.readObject();
            System.out.println("Hotel data loaded from " + SAVE_FILE);
            return hotel;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data, starting fresh: " + e.getMessage());
            Hotel hotel = new Hotel();
            hotel.initializeRooms();
            return hotel;
        }
    }

    // ---------- Main program ----------
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("     HOTEL RESERVATION SYSTEM (CodeAlpha)");
        System.out.println("===========================================");

        Hotel hotel = loadHotel();
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    searchRooms(hotel);
                    break;
                case "2":
                    bookRoom(hotel);
                    break;
                case "3":
                    cancelReservation(hotel);
                    break;
                case "4":
                    payForReservation(hotel);
                    break;
                case "5":
                    viewAllReservations(hotel);
                    break;
                case "6":
                    viewAllRooms(hotel);
                    break;
                case "7":
                    saveHotel(hotel);
                    break;
                case "8":
                    saveHotel(hotel);
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1. Search available rooms");
        System.out.println("2. Book a room");
        System.out.println("3. Cancel a reservation");
        System.out.println("4. Pay for a reservation");
        System.out.println("5. View all reservations");
        System.out.println("6. View all rooms");
        System.out.println("7. Save data");
        System.out.println("8. Save & exit");
        System.out.print("Enter choice: ");
    }

    private static void searchRooms(Hotel hotel) {
        System.out.println("Filter by category? (STANDARD / DELUXE / SUITE / ALL): ");
        String input = scanner.nextLine().trim().toUpperCase();
        RoomCategory category = null;
        if (!input.equals("ALL") && !input.isEmpty()) {
            try {
                category = RoomCategory.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown category, showing all.");
            }
        }
        List<Room> available = hotel.searchAvailable(category);
        System.out.println("\n--- Available Rooms ---");
        if (available.isEmpty()) {
            System.out.println("No available rooms match your filter.");
        } else {
            for (Room r : available) System.out.println(r);
        }
    }

    private static void bookRoom(Hotel hotel) {
        System.out.print("Enter guest name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        System.out.print("Enter room number to book: ");
        int roomNumber = readInt();
        if (roomNumber <= 0) return;

        Room room = hotel.findRoom(roomNumber);
        if (room == null) {
            System.out.println("Room not found.");
            return;
        }
        if (!room.available) {
            System.out.println("Room is already booked.");
            return;
        }
        System.out.print("Enter number of nights: ");
        int nights = readInt();
        if (nights <= 0) return;

        Reservation res = hotel.book(name, roomNumber, nights);
        if (res != null) {
            System.out.println("Booking confirmed!");
            System.out.println(res);
        } else {
            System.out.println("Booking failed.");
        }
    }

    private static void cancelReservation(Hotel hotel) {
        System.out.print("Enter reservation ID to cancel: ");
        int id = readInt();
        if (id <= 0) return;
        boolean success = hotel.cancel(id);
        System.out.println(success ? "Reservation cancelled." : "Reservation not found or already cancelled.");
    }

    private static void payForReservation(Hotel hotel) {
        System.out.print("Enter reservation ID to pay for: ");
        int id = readInt();
        if (id <= 0) return;
        Reservation res = hotel.findReservation(id);
        if (res == null) {
            System.out.println("Reservation not found.");
            return;
        }
        if (res.cancelled) {
            System.out.println("This reservation is cancelled.");
            return;
        }
        if (res.paid) {
            System.out.println("This reservation is already paid.");
            return;
        }
        System.out.printf("Amount due: $%.2f%n", res.totalAmount);
        System.out.print("Confirm payment (simulated)? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("y")) {
            hotel.pay(id);
            System.out.println("Payment successful (simulated). Thank you!");
        } else {
            System.out.println("Payment cancelled.");
        }
    }

    private static void viewAllReservations(Hotel hotel) {
        System.out.println("\n--- All Reservations ---");
        if (hotel.reservations.isEmpty()) {
            System.out.println("No reservations yet.");
        } else {
            for (Reservation r : hotel.reservations) System.out.println(r);
        }
    }

    private static void viewAllRooms(Hotel hotel) {
        System.out.println("\n--- All Rooms ---");
        for (Room r : hotel.rooms) System.out.println(r);
    }

    private static int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return -1;
        }
    }
}
