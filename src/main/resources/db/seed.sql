-- Seeding

-- Seed admin user and profile
-- Password: adminPass123
INSERT INTO
    users (
        username,
        email,
        password_hash,
        role,
        is_email_verified
    )
VALUES (
        'admin123',
        'tsh.harry.dev@gmail.com',
        'S5ccr9eQPRSL2o+Kp/qle3ac3tUT7LMeEk0eAQqAVV8=',
        'ADMIN',
        TRUE
    );

INSERT INTO
    profile (
        user_id,
        first_name,
        last_name
    )
VALUES (1, 'Admin', 'User');

-- Seed courses
-- Insert 4 courses
INSERT INTO
    courses (course_name, course_code)
VALUES (
        'Programming Fundamentals II',
        'CS102'
    ),
    (
        'Mathematical Foundations of Computing',
        'CS104'
    ),
    (
        'IT Solution Architecture',
        'CS301'
    ),
    ('Operating Systems', 'CS205');