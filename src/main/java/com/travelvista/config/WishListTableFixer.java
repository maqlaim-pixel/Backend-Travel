package com.travelvista.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class WishListTableFixer implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    public WishListTableFixer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        try {
            // Check if wishlists table exists
            var cols = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name='wishlists'"
            );
            
            if (cols.isEmpty()) {
                // Table doesn't exist — create it with correct schema
                System.out.println("🚀 Creating wishlists table...");
                jdbc.execute("""
                    CREATE TABLE wishlists (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL REFERENCES users(id),
                        package_id BIGINT NOT NULL REFERENCES packages(id),
                        created_at TIMESTAMP DEFAULT NOW(),
                        UNIQUE(user_id, package_id)
                    )
                """);
                System.out.println("✅ wishlists table created successfully!");
            } else {
                // Table exists — check if it has old schema
                boolean hasOldSchema = cols.stream()
                    .anyMatch(c -> "entity_id".equals(c.get("column_name")));
                
                if (hasOldSchema) {
                    System.out.println("🔄 Dropping old wishlists table (old schema with entity_id)...");
                    jdbc.execute("DROP TABLE IF EXISTS wishlists CASCADE");
                    // Recreate with correct schema
                    jdbc.execute("""
                        CREATE TABLE wishlists (
                            id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT NOT NULL REFERENCES users(id),
                            package_id BIGINT NOT NULL REFERENCES packages(id),
                            created_at TIMESTAMP DEFAULT NOW(),
                            UNIQUE(user_id, package_id)
                        )
                    """);
                    System.out.println("✅ wishlists table recreated with correct schema!");
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ WishListTableFixer: " + e.getMessage());
        }
    }
}
