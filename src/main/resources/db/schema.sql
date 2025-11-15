-- Drop tables in correct order (reverse of dependencies)
DROP TABLE IF EXISTS attendance CASCADE;

DROP TABLE IF EXISTS verification CASCADE;

DROP TABLE IF EXISTS face_data CASCADE;

DROP TABLE IF EXISTS enrollments CASCADE;

DROP TABLE IF EXISTS profile CASCADE;

DROP TABLE IF EXISTS sessions CASCADE;

DROP TABLE IF EXISTS courses CASCADE;

DROP TABLE IF EXISTS users CASCADE;

-- Create users table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) DEFAULT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) DEFAULT NULL,
    role VARCHAR(20) NOT NULL CHECK (
        role IN (
            'STUDENT',
            'INSTRUCTOR',
            'ADMIN'
        )
    ),
    is_email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create profile table
CREATE TABLE profile (
    profile_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE,
    first_name VARCHAR(125) NOT NULL,
    last_name VARCHAR(125) NOT NULL,
    phone_number TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- Create verification table
CREATE TABLE verification (
    verification_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    identifier VARCHAR(50) NOT NULL CHECK (
        identifier IN (
            'VERIFICATION',
            'FORGOT_PASSWORD'
        )
    ),
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- Create face_data table
CREATE TABLE face_data (
    face_data_id SERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    avg_embedding VECTOR,
    avg_histogram BYTEA,
    FOREIGN KEY (student_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- Create courses table
CREATE TABLE courses (
    course_id SERIAL PRIMARY KEY,
    course_name VARCHAR(255) NOT NULL,
    course_code VARCHAR(10) NOT NULL UNIQUE
);

-- Create enrollments table
CREATE TABLE enrollments (
    enrollment_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses (course_id) ON DELETE CASCADE,
    UNIQUE (user_id, course_id)
);

-- Create sessions table
CREATE TABLE sessions (
    session_id SERIAL PRIMARY KEY,
    course_id INTEGER NOT NULL,
    late_threshold INTEGER,
    location VARCHAR(255),
    start_time TIMESTAMP WITHOUT TIME ZONE,
    end_time TIMESTAMP WITHOUT TIME ZONE,
    session_date DATE,
    status VARCHAR(7),
    auto_start BOOLEAN DEFAULT FALSE,
    auto_stop BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (course_id) REFERENCES courses (course_id) ON DELETE CASCADE
);

-- Create attendance table
CREATE TABLE attendance (
    user_id INTEGER NOT NULL,
    session_id INTEGER NOT NULL,
    note VARCHAR(255),
    confidence DOUBLE PRECISION,
    marked_at TIMESTAMP WITHOUT TIME ZONE,
    last_seen TIMESTAMP WITHOUT TIME ZONE,
    method VARCHAR(10),
    status VARCHAR(20),
    PRIMARY KEY (user_id, session_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES sessions (session_id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_profile_user ON profile (user_id);

CREATE INDEX idx_verification_user ON verification (user_id);

CREATE INDEX idx_verification_token ON verification (token);

CREATE INDEX idx_face_data_student ON face_data (student_id);

CREATE INDEX idx_enrollments_user ON enrollments (user_id);

CREATE INDEX idx_enrollments_course ON enrollments (course_id);

CREATE INDEX idx_sessions_course ON sessions (course_id);

CREATE INDEX idx_sessions_date ON sessions (session_date);

CREATE INDEX idx_attendance_session ON attendance (session_id);

CREATE INDEX idx_attendance_user ON attendance (user_id);

CREATE INDEX idx_attendance_status ON attendance (status);

CREATE INDEX idx_attendance_marked_at ON attendance (marked_at);

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
-- Insert courses
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