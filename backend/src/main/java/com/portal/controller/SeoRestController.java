package com.portal.controller;

import com.portal.entity.Student;
import com.portal.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeoRestController {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final StudentRepository studentRepository;

    public SeoRestController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // robots.txt allows public portfolios and sitemap, blocks private and protected routes
    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getRobotsTxt() {
        String cleanBase = frontendUrl != null ? frontendUrl.replaceAll("/$", "") : "http://localhost:5173";
        StringBuilder sb = new StringBuilder();
        sb.append("User-agent: *\n");
        sb.append("Allow: /\n");
        sb.append("Allow: /u/\n");
        sb.append("Disallow: /student/\n");
        sb.append("Disallow: /faculty/\n");
        sb.append("Disallow: /admin/\n");
        sb.append("Disallow: /search\n");
        sb.append("Disallow: /export\n");
        sb.append("Disallow: /notifications\n\n");
        sb.append("Sitemap: ").append(cleanBase).append("/sitemap.xml\n");
        return sb.toString();
    }

    // sitemap.xml dynamically lists only students with PUBLIC visibility and >= 1 approved achievement
    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String getSitemapXml() {
        String cleanBase = frontendUrl != null ? frontendUrl.replaceAll("/$", "") : "http://localhost:5173";
        List<Student> students = studentRepository.findPublicStudentsWithApprovedAchievements();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // site homepage
        sb.append("  <url>\n");
        sb.append("    <loc>").append(cleanBase).append("/</loc>\n");
        sb.append("    <changefreq>daily</changefreq>\n");
        sb.append("    <priority>1.0</priority>\n");
        sb.append("  </url>\n");

        // individual public profiles
        for (Student s : students) {
            sb.append("  <url>\n");
            sb.append("    <loc>").append(cleanBase).append("/u/").append(s.getProfileSlug()).append("</loc>\n");
            sb.append("    <changefreq>weekly</changefreq>\n");
            sb.append("    <priority>0.8</priority>\n");
            sb.append("  </url>\n");
        }

        sb.append("</urlset>");
        return sb.toString();
    }
}
