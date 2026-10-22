package com.portal.service;

import com.portal.entity.Achievement;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ExportService {

    // build CSV content with UTF-8 BOM, escaped values, and formula injection defense
    public byte[] generateAchievementsCsv(List<Achievement> achievements) {
        StringBuilder sb = new StringBuilder();

        // 1. UTF-8 Byte Order Mark (BOM) so Microsoft Excel opens it with proper UTF-8 charset
        sb.append('\uFEFF');

        // 2. CSV Header row
        sb.append("Student Name,Roll Number,Branch,Year,Category,Title,Level,Event Date,Points Awarded\r\n");

        // 3. Data rows
        for (Achievement a : achievements) {
            String studentName = "";
            String rollNumber = "";
            String branch = "";
            String year = "";

            if (a.getStudent() != null) {
                if (a.getStudent().getUser() != null) {
                    studentName = a.getStudent().getUser().getFullName();
                }
                rollNumber = a.getStudent().getRollNumber();
                branch = a.getStudent().getBranch();
                year = String.valueOf(a.getStudent().getYear());
            }

            String categoryName = (a.getCategory() != null) ? a.getCategory().getName() : "";
            String title = a.getTitle();
            String level = a.getLevel();
            String eventDate = (a.getEventDate() != null) ? a.getEventDate().toString() : "";
            String points = String.valueOf(a.getPointsAwarded());

            sb.append(escapeCsvCell(studentName)).append(",");
            sb.append(escapeCsvCell(rollNumber)).append(",");
            sb.append(escapeCsvCell(branch)).append(",");
            sb.append(escapeCsvCell(year)).append(",");
            sb.append(escapeCsvCell(categoryName)).append(",");
            sb.append(escapeCsvCell(title)).append(",");
            sb.append(escapeCsvCell(level)).append(",");
            sb.append(escapeCsvCell(eventDate)).append(",");
            sb.append(escapeCsvCell(points)).append("\r\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // escape cell: formula injection defense + quote and comma escaping
    public String escapeCsvCell(String value) {
        if (value == null) {
            return "";
        }

        String cell = value;

        // Formula injection protection: prepend single quote if starting with =, +, -, or @
        if (cell.startsWith("=") || cell.startsWith("+") || cell.startsWith("-") || cell.startsWith("@")) {
            cell = "'" + cell;
        }

        // If cell contains comma, quote, or newline, escape double quotes and wrap in quotes
        if (cell.contains(",") || cell.contains("\"") || cell.contains("\n") || cell.contains("\r")) {
            cell = "\"" + cell.replace("\"", "\"\"") + "\"";
        }

        return cell;
    }
}
