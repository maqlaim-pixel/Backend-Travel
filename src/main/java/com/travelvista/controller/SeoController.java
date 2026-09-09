package com.travelvista.controller;

import com.travelvista.model.Destination;
import com.travelvista.model.TravelPackage;
import com.travelvista.repository.DestinationRepository;
import com.travelvista.repository.PackageRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seo")
public class SeoController {

    private final DestinationRepository destinationRepo;
    private final PackageRepository packageRepo;

    public SeoController(DestinationRepository destinationRepo, PackageRepository packageRepo) {
        this.destinationRepo = destinationRepo;
        this.packageRepo = packageRepo;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // Home page
        sb.append("  <url><loc>https://travelvista.com/</loc><changefreq>daily</changefreq><priority>1.0</priority></url>\n");

        // Static pages
        String[] staticPages = {"/about", "/contact", "/packages", "/hotels", "/activities", "/blog"};
        for (String page : staticPages) {
            sb.append("  <url><loc>https://travelvista.com").append(page).append("</loc><changefreq>weekly</changefreq><priority>0.8</priority></url>\n");
        }

        // Destinations
        List<Destination> destinations = destinationRepo.findAll();
        for (Destination d : destinations) {
            if (!"published".equals(d.getStatus())) continue;
            if (Boolean.TRUE.equals(d.getNoIndex())) continue;
            sb.append("  <url><loc>https://travelvista.com/").append(d.getSlug()).append("</loc>");
            sb.append("<changefreq>weekly</changefreq><priority>0.9</priority></url>\n");
        }

        // Packages
        List<TravelPackage> packages = packageRepo.findAll();
        for (TravelPackage p : packages) {
            if (!"published".equals(p.getStatus())) continue;
            if (p.getSlug() != null && !p.getSlug().isEmpty()) {
                sb.append("  <url><loc>https://travelvista.com/packages/").append(p.getSlug()).append("</loc>");
                sb.append("<changefreq>weekly</changefreq><priority>0.7</priority></url>\n");
            }
        }

        sb.append("</urlset>");
        return sb.toString();
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String robots() {
        return "User-agent: *\n" +
               "Allow: /\n" +
               "Disallow: /admin/\n" +
               "Disallow: /account/\n" +
               "Disallow: /api/\n\n" +
               "Sitemap: https://travelvista.com/api/seo/sitemap.xml\n";
    }
}
