package com.streamverse.config;

import com.streamverse.entity.*;
import com.streamverse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding initial data...");
            seedUsers();
            seedContent();
            log.info("Data seeding complete!");
        }
    }

    private void seedUsers() {
        // Admin
        User admin = User.builder()
            .firstName("Admin").lastName("User")
            .email("admin@streamverse.com").username("admin")
            .password(passwordEncoder.encode("Admin@123"))
            .role(User.Role.ADMIN).status(User.UserStatus.ACTIVE).build();
        userRepository.save(admin);

        // Creator
        User creator = User.builder()
            .firstName("John").lastName("Creator")
            .email("creator@streamverse.com").username("johncreator")
            .password(passwordEncoder.encode("Creator@123"))
            .role(User.Role.CREATOR).status(User.UserStatus.ACTIVE).build();
        userRepository.save(creator);

        // Publisher
        User publisher = User.builder()
            .firstName("Jane").lastName("Publisher")
            .email("publisher@streamverse.com").username("janepublisher")
            .password(passwordEncoder.encode("Publisher@123"))
            .role(User.Role.PUBLISHER).status(User.UserStatus.ACTIVE).build();
        userRepository.save(publisher);

        // Viewers
        String[] names = {"Alice Smith", "Bob Jones", "Charlie Brown", "Diana Prince", "Edward King"};
        for (int i = 0; i < names.length; i++) {
            String[] parts = names[i].split(" ");
            User viewer = User.builder()
                .firstName(parts[0]).lastName(parts[1])
                .email(parts[0].toLowerCase() + "@example.com")
                .username(parts[0].toLowerCase() + parts[1].toLowerCase())
                .password(passwordEncoder.encode("Viewer@123"))
                .role(User.Role.VIEWER).status(User.UserStatus.ACTIVE).build();
            userRepository.save(viewer);
        }

        log.info("Created {} users", userRepository.count());
    }

    private void seedContent() {
        User creator = userRepository.findByEmail("creator@streamverse.com").orElseThrow();
        User publisher = userRepository.findByEmail("publisher@streamverse.com").orElseThrow();

        Object[][] movies = {
            {"Galactic Odyssey", "MOVIE", "Sci-Fi", "An epic journey through the cosmos where humanity faces its greatest challenge.", 2024, 7500L, 3200L, 4.8},
            {"Dark Chronicles", "SERIES", "Thriller", "A gripping mystery series that keeps you on the edge of your seat.", 2023, 12000L, 5800L, 4.5},
            {"Jazz & Blues Live", "MUSIC", "Pop", "Live performance from the world's greatest jazz musicians.", 2024, 3200L, 8900L, 4.7},
            {"Tech Talks Daily", "PODCAST", "Technology", "Daily insights from Silicon Valley's top minds.", 2024, 6700L, 4100L, 4.3},
            {"The Last Kingdom", "MOVIE", "Fantasy", "An epic fantasy adventure set in a mythical land.", 2023, 9800L, 4500L, 4.6},
            {"Comedy Central Live", "LIVE", "Comedy", "Real-time comedy show streaming live every Friday.", 2024, 2100L, 900L, 4.2},
            {"Nature's Wonders", "MOVIE", "Documentary", "Stunning documentary exploring Earth's most beautiful locations.", 2023, 5400L, 2300L, 4.9},
            {"Pop Hits Collection", "MUSIC", "Pop", "The best pop hits from 2024, all in one place.", 2024, 8900L, 15000L, 4.4},
        };

        for (Object[] m : movies) {
            User c = (Math.random() > 0.5) ? creator : publisher;
            Content content = Content.builder()
                .title((String) m[0])
                .type(Content.ContentType.valueOf((String) m[1]))
                .genre((String) m[2])
                .description((String) m[3])
                .releaseYear((Integer) m[4])
                .viewCount((Long) m[5])
                .playCount((Long) m[6])
                .averageRating((Double) m[7])
                .totalEarnings(((Long) m[5]) * 0.01)
                .status(Content.ContentStatus.PUBLISHED)
                .monetizationType(Content.MonetizationType.FREE)
                .ageRating("PG-13")
                .creator(c)
                .build();
            contentRepository.save(content);
        }

        // A few pending
        Content pending = Content.builder()
            .title("Untitled Drama Series")
            .type(Content.ContentType.SERIES)
            .genre("Drama")
            .description("A brand new drama series currently under review.")
            .releaseYear(2025)
            .viewCount(0L).playCount(0L).averageRating(0.0).totalEarnings(0.0)
            .status(Content.ContentStatus.PENDING_REVIEW)
            .monetizationType(Content.MonetizationType.SUBSCRIPTION)
            .creator(creator)
            .build();
        contentRepository.save(pending);

        log.info("Created {} content items", contentRepository.count());
    }
}
