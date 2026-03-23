package com.heallots.mobile.utils;

import com.heallots.mobile.models.Appointment;
import com.heallots.mobile.models.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mock data for testing without backend connection
 */
public class MockData {

    // Services for booking
    public static class Service {
        public String id;
        public String name;
        public String specialist;
        public String description;
        public String icon;

        public Service(String id, String name, String specialist, String description, String icon) {
            this.id = id;
            this.name = name;
            this.specialist = specialist;
            this.description = description;
            this.icon = icon;
        }
    }

    public static List<Service> getServices() {
        return Arrays.asList(
            new Service("1", "Traditional Hilot", "Manang Rosa", "Most popular hilot session", "\uD83E\uDD32\uD83C\uDFFB"),
            new Service("2", "Herbal Compress", "Mang Berting", "Best for pain relief", "\uD83C\uDF3F"),
            new Service("3", "Head & Neck Relief", "Ate Cora", "Stress relief for upper body tension", "\uD83D\uDC86\uD83C\uDFFB\u200D\u2640\uFE0F"),
            new Service("4", "Foot Reflexology", "Manang Lourdes", "Relaxing foot pressure therapy", "\uD83E\uDDB6\uD83C\uDFFC"),
            new Service("5", "Hot Oil Massage", "Mang Totoy", "Warm oil massage for deep relaxation", "\uD83E\uDED9"),
            new Service("6", "Whole-Body Hilot", "Ate Nena", "Premium full-body treatment", "\uD83E\uDDD8\uD83C\uDFFB")
        );
    }

    public static List<String> getMorningTimeSlots() {
        return Arrays.asList(
            "08:00 AM",
            "09:00 AM",
            "10:00 AM",
            "11:00 AM",
            "12:00 PM"
        );
    }

    public static List<String> getAfternoonTimeSlots() {
        return Arrays.asList(
            "01:00 PM",
            "02:00 PM",
            "03:00 PM",
            "04:00 PM",
            "05:00 PM"
        );
    }

    public static List<String> getAllTimeSlots() {
        List<String> slots = new ArrayList<>();
        slots.addAll(getMorningTimeSlots());
        slots.addAll(getAfternoonTimeSlots());
        return slots;
    }

    // Mock appointments
    public static List<Appointment> getUpcomingAppointments() {
        List<Appointment> appointments = new ArrayList<>();

        Appointment apt1 = new Appointment();
        apt1.setId("1");
        apt1.setPatientName("Lia Santos");
        apt1.setPatientEmail("lia.santos@example.com");
        apt1.setPatientPhone("+63 917 100 1001");
        apt1.setServiceName("Traditional Hilot");
        apt1.setSpecialistName("Maria Santos");
        apt1.setAppointmentDate("2026-03-25");
        apt1.setTimeSlot("10:00 AM");
        apt1.setStatus("scheduled");
        apt1.setReason("Body pain and muscle aches");
        apt1.setNotes("Prefers a gentle but focused pressure session.");
        apt1.setReviewed(false);
        appointments.add(apt1);

        Appointment apt2 = new Appointment();
        apt2.setId("2");
        apt2.setPatientName("Marco Reyes");
        apt2.setPatientEmail("marco.reyes@example.com");
        apt2.setPatientPhone("+63 917 100 1002");
        apt2.setServiceName("Head & Neck Relief");
        apt2.setSpecialistName("Dr. Jose Cruz");
        apt2.setAppointmentDate("2026-03-28");
        apt2.setTimeSlot("02:00 PM");
        apt2.setStatus("scheduled");
        apt2.setReason("Stress and fatigue relief");
        apt2.setNotes("Would like extra attention on neck and shoulders.");
        apt2.setReviewed(false);
        appointments.add(apt2);

        Appointment apt3 = new Appointment();
        apt3.setId("3");
        apt3.setPatientName("Ana Cruz");
        apt3.setPatientEmail("ana.cruz@example.com");
        apt3.setPatientPhone("+63 917 100 1003");
        apt3.setServiceName("Foot Reflexology");
        apt3.setSpecialistName("Ana Maria");
        apt3.setAppointmentDate("2026-04-01");
        apt3.setTimeSlot("09:00 AM");
        apt3.setStatus("scheduled");
        apt3.setReason("Regular wellness session");
        apt3.setNotes("Morning schedule preferred.");
        apt3.setReviewed(false);
        appointments.add(apt3);

        return appointments;
    }

    public static List<Appointment> getPastAppointments() {
        List<Appointment> appointments = new ArrayList<>();

        Appointment apt4 = new Appointment();
        apt4.setId("4");
        apt4.setPatientName("Lianne Gomez");
        apt4.setPatientEmail("lianne.gomez@example.com");
        apt4.setPatientPhone("+63 917 100 1004");
        apt4.setServiceName("Traditional Hilot");
        apt4.setSpecialistName("Maria Santos");
        apt4.setAppointmentDate("2026-03-10");
        apt4.setTimeSlot("10:00 AM");
        apt4.setStatus("completed");
        apt4.setReason("Post-injury recovery");
        apt4.setNotes("Felt much better after the session.");
        apt4.setReviewed(true);
        appointments.add(apt4);

        Appointment apt5 = new Appointment();
        apt5.setId("5");
        apt5.setPatientName("Paolo David");
        apt5.setPatientEmail("paolo.david@example.com");
        apt5.setPatientPhone("+63 917 100 1005");
        apt5.setServiceName("Hot Oil Massage");
        apt5.setSpecialistName("Dr. Jose Cruz");
        apt5.setAppointmentDate("2026-03-05");
        apt5.setTimeSlot("02:00 PM");
        apt5.setStatus("completed");
        apt5.setReason("Sleep improvement");
        apt5.setNotes("Warm oil session for full relaxation.");
        apt5.setReviewed(true);
        appointments.add(apt5);

        Appointment apt6 = new Appointment();
        apt6.setId("6");
        apt6.setPatientName("Mika dela Cruz");
        apt6.setPatientEmail("mika.delacruz@example.com");
        apt6.setPatientPhone("+63 917 100 1006");
        apt6.setServiceName("Herbal Compress");
        apt6.setSpecialistName("Ana Maria");
        apt6.setAppointmentDate("2026-02-28");
        apt6.setTimeSlot("09:00 AM");
        apt6.setStatus("completed");
        apt6.setReason("Headache and body tension");
        apt6.setNotes("Requested extra focus on upper back.");
        apt6.setReviewed(false);
        appointments.add(apt6);

        return appointments;
    }

    public static List<Appointment> getCancelledAppointments() {
        List<Appointment> appointments = new ArrayList<>();

        Appointment apt7 = new Appointment();
        apt7.setId("7");
        apt7.setPatientName("Jasmine Flores");
        apt7.setPatientEmail("jasmine.flores@example.com");
        apt7.setPatientPhone("+63 917 100 1007");
        apt7.setServiceName("Head & Neck Relief");
        apt7.setSpecialistName("Maria Santos");
        apt7.setAppointmentDate("2026-03-20");
        apt7.setTimeSlot("10:00 AM");
        apt7.setStatus("cancelled");
        apt7.setReason("Stress and fatigue relief");
        apt7.setCancellationReason("Conflict with work schedule.");
        apt7.setReviewed(false);
        appointments.add(apt7);

        Appointment apt8 = new Appointment();
        apt8.setId("8");
        apt8.setPatientName("Rico Mendoza");
        apt8.setPatientEmail("rico.mendoza@example.com");
        apt8.setPatientPhone("+63 917 100 1008");
        apt8.setServiceName("Foot Reflexology");
        apt8.setSpecialistName("Dr. Jose Cruz");
        apt8.setAppointmentDate("2026-03-18");
        apt8.setTimeSlot("02:00 PM");
        apt8.setStatus("cancelled");
        apt8.setReason("Foot pain after long walks");
        apt8.setCancellationReason("Recovered early and no longer needed the session.");
        apt8.setReviewed(false);
        appointments.add(apt8);

        return appointments;
    }

    // Mock admin statistics
    public static class AdminStats {
        public int totalAppointments;
        public int completedAppointments;
        public int pendingAppointments;
        public int totalPatients;
        public int totalSpecialists;
        public double averageRating;

        public AdminStats(int total, int completed, int pending, int patients, int specialists, double rating) {
            this.totalAppointments = total;
            this.completedAppointments = completed;
            this.pendingAppointments = pending;
            this.totalPatients = patients;
            this.totalSpecialists = specialists;
            this.averageRating = rating;
        }
    }

    public static AdminStats getAdminStats() {
        return new AdminStats(156, 128, 28, 85, 12, 4.7);
    }

    public static List<Appointment> getAllAppointmentsForAdmin() {
        List<Appointment> all = new ArrayList<>();
        all.addAll(getUpcomingAppointments());
        all.addAll(getPastAppointments());
        all.addAll(getCancelledAppointments());
        return all;
    }

    public static List<User> getUsersForAdmin() {
        List<User> users = new ArrayList<>();

        users.add(buildUser("1", "lia.santos@example.com", "Lia Santos", "USER", "+63 917 100 1001", "2026-01-12"));
        users.add(buildUser("2", "marco.reyes@example.com", "Marco Reyes", "USER", "+63 917 100 1002", "2026-01-13"));
        users.add(buildUser("3", "ana.cruz@example.com", "Ana Cruz", "USER", "+63 917 100 1003", "2026-01-14"));
        users.add(buildUser("4", "lianne.gomez@example.com", "Lianne Gomez", "USER", "+63 917 100 1004", "2026-01-18"));
        users.add(buildUser("5", "paolo.david@example.com", "Paolo David", "USER", "+63 917 100 1005", "2026-01-21"));
        users.add(buildUser("6", "mika.delacruz@example.com", "Mika dela Cruz", "USER", "+63 917 100 1006", "2026-01-25"));
        users.add(buildUser("7", "jasmine.flores@example.com", "Jasmine Flores", "USER", "+63 917 100 1007", "2026-02-02"));
        users.add(buildUser("8", "rico.mendoza@example.com", "Rico Mendoza", "USER", "+63 917 100 1008", "2026-02-08"));
        users.add(buildUser("9", "admin@heallots.com", "Admin Rosa", "ADMIN", "+63 917 100 1999", "2025-12-20"));

        return users;
    }

    private static User buildUser(String id, String email, String fullName, String role, String phone, String createdAt) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role);
        user.setPhoneNumber(phone);
        user.setCreatedAt(createdAt);
        return user;
    }

    public static List<Appointment> getRecentReviewsForAdmin() {
        return getPastAppointments();
    }
}
