package hr.abysalto.hiring.mid.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private boolean dataInitialized = false;

    public boolean isDataInitialized() {
        return this.dataInitialized;
    }

    public void initialize() {
        initTables();
        initData();
        this.dataInitialized = true;
    }

    private void initTables() {
        this.jdbcTemplate.execute("""
                 CREATE TABLE buyer (
                	 buyer_id INT auto_increment PRIMARY KEY,
                	 first_name varchar(100) NOT NULL,
                	 last_name varchar(100) NOT NULL,
                	 title varchar(100) NULL
                 );
                """
        );
        this.jdbcTemplate.execute("""
                CREATE TABLE users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(100) UNIQUE NOT NULL,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL
                );
                
                ALTER TABLE buyer ADD COLUMN user_id INT;
                ALTER TABLE buyer ADD FOREIGN KEY (user_id) REFERENCES users(id);
                """
        );
    }

    private void initData() {
        this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Jabba', 'Hutt', 'the')");
        this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Anakin', 'Skywalker', NULL)");
        this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Jar Jar', 'Binks', NULL)");
        this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Han', 'Solo', NULL)");
        this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Leia', 'Organa', 'Princess')");
    }
}
