# Smart Attendance System

A comprehensive JavaFX-based attendance management system with facial recognition capabilities. This system enables instructors and administrators to manage student attendance, courses, and generate detailed reports with support for multiple export formats.

## Tech Stack

- **Frontend Framework**: JavaFX 21.0.3 (controls, FXML, Swing interop)
- **Language**: Java 17
- **Build Tool**: Maven (with `javafx-maven-plugin` 0.0.8)
- **Database**: PostgreSQL (JDBC driver `org.postgresql:postgresql:42.6.0`)
- **Computer Vision**: OpenCV 4.9.0 (`org.openpnp:opencv:4.9.0-0`)
- **Algorithm models**: Histogram, OpenFace v1 (`nn4.small2.v1.t7`)
- **PDF Generation**: Apache PDFBox 2.0.30 + iText 5.5.13.3
- **Excel Export**: Apache POI (poi-ooxml 5.2.5)
- **Email Service**: Jakarta Mail 2.0.1 + Jakarta Activation 2.0.1
- **UI Components**: ControlsFX 11.2.1
- **Logging**: SLF4J Simple 2.0.13
- **Configuration**: dotenv-java 2.3.2

## Folder Structure

```
smart-attendance-system/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/smartattendance/
│       │       ├── config/                  # Configuration and database setup
│       │       ├── controller/              # JavaFX UI controllers
│       │       │   ├── admin/               # Administrator-specific controllers
│       │       │   ├── auth/                # Authentication-related controllers
│       │       │   └── student/             # Student-specific controllers
│       │       ├── model/                   # Data models
│       │       │   ├── dto/                 # Data Transfer Objects
│       │       │   ├── entity/              # Entity models
│       │       │   └── enums/               # Enumerations (status, roles, etc.)
│       │       ├── repository/              # Data access layer (JDBC)
│       │       ├── service/                 # Business logic layer
│       │       │   ├── recognition/         # Face recognition services
│       │       │   └── rules/               # Business rules / validation services
│       │       ├── util/                    # Utility classes
│       │       │   ├── chart/               # Chart styling and export helpers
│       │       │   ├── report/              # PDF/CSV/XLSX report utilities
│       │       │   ├── security/            # Security-related utilities
│       │       │   └── validation/          # Input validation utilities
│       │       ├── ApplicationContext.java  # Dependency injection container
│       │       └── MainApp.java             # Application entry point
│       └── resources/
│           ├── haarscascades/               # Haar cascade XMLs for face detection
│           ├── icons/                       # Application icons
│           ├── openFaceModels/              # OpenFace model files
│           └── view/                        # JavaFX FXML views and styles
│               └── styles/                  # CSS stylesheets
├── .env.example                             # Sample environment configuration
├── .env                                     # Local environment configuration (gitignored)
├── compile.bat                              # Helper script to build via Maven wrapper
├── run.bat                                  # Helper script to launch the JavaFX app
├── pom.xml                                  # Maven configuration
└── README.md                                # Project documentation
```

## Folder Descriptions

### config/

Handles application configuration and database connectivity.

- **Config.java**: Application-wide configuration settings (paths, thresholds, flags).
- **DatabaseUtil.java**: Shared PostgreSQL connection helper used by legacy / util code.
- **ENV.java**: Helper for reading environment variables and `.env` configuration safely.

---

### controller/

Contains all JavaFX controllers for the user interface. Controllers handle user interactions and coordinate between the UI and business logic.

#### controller/

- **AttendanceController.java**: Admin view controller for listing and managing attendance for a specific session.
- **AttendanceFormController.java**: Dialog/form controller for creating or editing individual attendance records.
- **ControllerRegistry.java**: Keeps references to key controllers so they can refresh each other when data changes.
- **DashboardController.java**: Main admin dashboard controller; wires KPIs, filters, and charts.
- **ProfileController.java**: Admin profile page controller for updating personal details.
- **RecognitionController.java**: High-level controller that coordinates recognition-related actions (e.g. from dashboard to live recognition).
- **ReportController.java**: Reports tab controller; handles filters, export buttons, and email sending of reports.
- **RootController.java**: Root window controller managing the main layout and navigation between tabs.
- **SessionController.java**: Sessions tab controller; lists sessions, handles create/delete/start/stop/auto actions.
- **SessionFormController.java**: Dialog controller for creating new sessions or updating their configuration.
- **SettingsController.java**: Settings tab controller; binds config fields (camera index, thresholds, algorithm) to the underlying config.
- **StudentController.java**: Admin student management view; lists students and opens dialogs for details.
- **TabRefreshable.java**: Interface for tabs that can be programmatically refreshed when data changes.

#### controller/admin/

Admin-only dialogs and supporting controllers.

- **AddStudentDialog.java**: Dialog for adding a new student via the admin interface.
- **EnrollCourseDialog.java**: Dialog for enrolling a student in one or more courses.
- **StudentListController.java**: Admin controller for listing students with actions like view/edit.
- **StudentProfileDialog.java**: Dialog for viewing and editing detailed student information.

#### controller/auth/

Authentication- and verification-related controllers.

- **ForgotPasswordController.java**: Controller for the “Forgot Password” page/flow.
- **ForgotPasswordDialog.java**: Dialog prompting for email and confirmation when resetting passwords.
- **LoginController.java**: Login screen controller; validates credentials and routes to the correct portal.
- **RegisterController.java**: Registration controller for new users (subject to verification and admin invite rules).
- **VerificationDialog.java**: Dialog shown for verification steps (e.g. verification email sent / token processed).

#### controller/student/

Student-facing controllers and helper view models for the student portal.

- **AttendanceRow.java**: Simple DTO-like helper representing a single attendance row for the student UI.
- **DemoAttendanceData.java**: Demo data generator for populating the student attendance view during testing.
- **EnrollmentController.java**: Student-side controller for viewing course enrolments.
- **StudentAttendanceController.java**: Main student “My Attendance” controller (filters + table + summary).
- **StudentAttendanceLegendBuilder.java**: Helper for building the legend explaining colours/statuses in the attendance view.
- **StudentAttendanceRow.java**: View model that wraps raw attendance data with extra formatting flags for the table.
- **StudentAttendanceRowStyler.java**: Applies styling rules (e.g. colours, fonts) to rows based on status.
- **StudentCourseSummary.java**: Holds aggregated details for a course (attended, total, percentage) for the summary panel.
- **StudentSessionContext.java**: Stores session-related context for the currently logged-in student (used across student views).

---

### model/

Defines the data structures used throughout the application.

#### model/entity/

Core entity classes representing database tables.

- **AbstractEntity.java**: Base class for entities (common id and audit fields).
- **AttendanceRecord.java**: Entity mapping to an attendance record (student, session, status, method, timestamps, lastSeen, notes).
- **AuthSession.java**: Entity for tracking active authentication sessions (e.g. user session tokens).
- **Course.java**: Entity representing a course (code, name, group, etc.).
- **FaceData.java**: Entity storing face-related data (e.g. embeddings or references to images).
- **Image.java**: Entity for image metadata (paths, types, usage).
- **Profile.java**: Profile entity holding extended user information (names, contact details).
- **Session.java**: Session entity (course, date/time, location, late threshold, status, auto flags).
- **Student.java**: Student entity linking user, profile, and course/group attributes.
- **User.java**: System user entity (login credentials, role, status).
- **Verification.java**: Entity for verification tokens (email verification, password reset) with expiry and linkage to users.

#### model/enums/

Enumerations for fixed states and types.

- **AttendanceStatus.java**: Attendance statuses such as PRESENT, ABSENT, LATE, PENDING.
- **AuthVerification.java**: Types of verification (e.g. REGISTRATION, PASSWORD_RESET).
- **MarkMethod.java**: Methods of marking attendance (AUTO, MANUAL, QR, NONE).
- **RecognitionAlgorithm.java**: Available recognition algorithms (e.g. HISTOGRAM, OPEN_FACE).
- **Role.java**: User roles such as ADMIN and STUDENT.
- **Status.java**: Generic status enum used for entities (e.g. ACTIVE, INACTIVE).
- **ToastType.java**: Toast/notification types (INFO, SUCCESS, WARNING, ERROR) for UI feedback.

#### model/dto/

Data Transfer Objects for efficient data exchange between layers.

##### model/dto/student/

- **StudentDTO.java**: Basic student DTO (id, full name, course name) for lists and tables.
- **StudentProfileDTO.java**: DTO for student profile forms (names, contact details, etc.).

##### model/dto/user/

- **UserListDTO.java**: DTO optimized for listing users in tables (id, email, role, status).
- **UserProfileDTO.java**: DTO for updating and displaying user profile information.

##### model/dto/dashboard/

DTOs used to transport dashboard filter state and chart data.

- **AttendanceRecord.java**: Lightweight dashboard attendance record DTO used for chart preparation.
- **AttendanceRow.java**: Row-level DTO used to populate dashboard-related tables.
- **DashboardFilter.java**: Encapsulates dashboard filter selections (date range, course, group, status flags).
- **DashboardTopCards.java**: Holds KPI values for dashboard cards (total students, sessions, present today).

##### model/dto/report/

DTOs used by the reporting subsystem.

- **AttendanceReportFilter.java**: Represents report filter criteria (date range, course, session, status, method, confidence) before building a `ReportSpec`.

---

### repository/

Data access layer implementing database operations using JDBC.

- **AttendanceRecordRepository.java**: CRUD and query operations for `AttendanceRecord` entities.
- **AttendanceRepository.java**: Higher-level attendance queries (e.g. aggregations / filters across sessions).
- **AuthRepository.java**: Data access for authentication and verification entities (`AuthSession`, `Verification`).
- **CourseRepository.java**: Data access for `Course` plus enrollment-related queries.
- **DashboardRepository.java**: Queries for dashboard metrics and chart data.
- **ImageRepository.java**: Data access for `Image` and face-related assets.
- **JdbcDashboardRepository.java**: JDBC-based implementation of `DashboardRepository`.
- **PostgresUserRepository.java**: PostgreSQL-specific implementation for user lookups and authentication.
- **ProfileRepository.java**: Data access for `Profile` entities.
- **SessionRepository.java**: CRUD and search operations for `Session` entities.
- **StudentRepository.java**: CRUD and query operations for `Student` entities.
- **UserRepository.java**: Generic user data access (used by higher-level services).
- **VerificationRepository.java**: Data access for verification tokens.

---

### service/

Business logic layer providing services to controllers. Services handle validation, orchestration, and business rules.

- **AttendanceMarker.java**: Strategy interface for marking attendance (auto vs manual implementations).
- **AttendanceObserver.java**: Observer interface to be notified when attendance changes.
- **AttendanceReportService.java**: Applies filters, builds `AttendanceReportRow` lists, and delegates to CSV/XLSX/PDF generators and email sending.
- **AttendanceService.java**: Core service for attendance operations (create/update statuses, interact with repositories and markers).
- **AuthService.java**: Authentication and authorization logic (login, registration, verification, password reset).
- **AutoAttendanceMarker.java**: `AttendanceMarker` implementation that marks attendance automatically (e.g. via recognition).
- **CourseService.java**: Course management service, including enrollment-related logic.
- **DashboardService.java**: Aggregates counts/statistics and prepares structured data for dashboard charts and KPIs.
- **EmailService.java**: Wraps Jakarta Mail logic to send emails (verification, password reset, report attachments).
- **FaceDetectionService.java**: Service responsible for detecting faces in camera frames using OpenCV.
- **FaceProcessingService.java**: Handles preprocessing of face images (cropping, normalization, etc.).
- **FaceRecognitionService.java**: Connects recognition services with entities; maps recognition results to students and attendance actions.
- **ImageService.java**: Manages image data (saving/loading/associating with students and face data).
- **ManualAttendanceMarker.java**: `AttendanceMarker` implementation used when admins mark attendance manually.
- **ProfileService.java**: Manages creation and updates of user and student profiles.
- **RecognitionService.java**: High-level façade for calling specific recognizers and returning a `RecognitionResult`.
- **RecognitionObserver.java**: Observer interface that inherits AttendanceObserver, to be notified when attendance changes via auto marking through face recognition.
- **SessionService.java**: Handles creation and management of sessions, including application of auto-session rules.
- **StudentAttendanceService.java**: Student-specific view of attendance (fetching summaries and filtered records for a student).
- **StudentService.java**: Student domain service (CRUD, linking to users/courses, higher-level operations).
- **UserService.java**: User domain service (managing accounts, roles, and status changes).

#### service/recognition/

Face recognition services that integrate with OpenCV and model files.

- **HistogramRecognizer.java**: Recognizer implementation using histogram-based comparison.
- **OpenFaceRecognizer.java**: Recognizer implementation using the OpenFace model (embeddings).
- **RecognitionResult.java**: Result object for recognition attempts (matched student, confidence, algorithm, flags).
- **Recognizer.java**: Interface for pluggable recognizers (histogram, OpenFace, or future implementations).

#### service/rules/

Rules engine for automatic session behaviour.

- **AutoSessionRule.java**: Core rule interface; each implementation decides if a session may auto-start/auto-stop.
- **ConflictPreventionRule.java**: Prevents starting sessions that would conflict (e.g. overlap) with existing sessions.
- **SessionEndedRule.java**: Ensures sessions that are already ended or closed cannot be auto-started.
- **StatusValidationRule.java**: Checks session status (e.g. scheduled vs running) before allowing auto changes.
- **TimeRule.java**: Validates current time against session start/end times for auto start/stop.

---

### util/

Utility classes for common operations.

#### util/ (root)

General-purpose utilities used across the application.

- **AttendanceTimeUtils**: Utility for calculating time differences and cooldown checks related to attendance marking.
- **AutoAttendanceUpdater.java**: Periodic updater that checks and updates attendance automatically based on rules.
- **CameraUtils.java**: Helper functions for interacting with camera devices via OpenCV.
- **CheckBoxTableCell.java**: Custom JavaFX table cell implementation with embedded checkboxes.
- **EmailService.java**: Utility wrapper for email sending in contexts where services are not injected.
- **EmailSettings.java**: Utility container for email-related configuration values.
- **EmailTemplates.java**: Provides text/HTML templates for emails (verification, reset, report).
- **FileLoader.java**: Utility for loading files/resources (e.g. models, cascades, templates).
- **OpenCVUtils.java**: Helper functions for OpenCV initialisation and image conversion.
- **TestConnection.java**: Simple utility to test database connectivity.

#### util/chart/

Chart styling and export helpers shared across dashboard charts.

- **AbstractChartStyler.java**: Base class with shared palette and axis/legend styling logic.
- **ChartExporter.java**: Interface for exporting charts to image files.
- **ChartStyler.java**: Interface describing styling behaviour for bar/stacked-bar/pie charts.
- **DefaultChartStyler.java**: Concrete chart styler applying project-wide colours and fonts.
- **DefaultLegendBuilder.java**: Default implementation for building chart legends from series/statuses.
- **LegendBuilder.java**: Interface for constructing chart legends in a flexible way.
- **PngChartExporter.java**: Implementation of `ChartExporter` that snapshots JavaFX charts to PNG files.

#### util/dashboard/

Dashboard-specific helpers.

- **DashboardCharts.java**: Helper for wiring dashboard chart nodes to data, styler, and export logic.

#### util/report/

Utilities for generating CSV, Excel, and PDF reports from attendance data.

- **AttendanceReportRow.java**: Immutable data object for a single report row (date/time, session, course, student, status, method, confidence, note).
- **CsvReportGenerator.java**: Generates CSV reports from `AttendanceReportRow` lists.
- **EmailSettings.java**: Report-specific email settings helper (e.g. subject/prefixes for report mails).
- **PdfReportGenerator.java**: Generates PDF attendance reports (tables, headers) using PDFBox/iText.
- **ReportGenerator.java**: Interface implemented by all report generator types (CSV/XLSX/PDF).
- **ReportSpec.java**: Encapsulates report configuration (filters, selected columns, sort order, output type).
- **XlsxReportGenerator.java**: Generates Excel (XLSX) reports using Apache POI.

#### util/security/

Security-related helpers.

- **PasswordUtil.java**: Password hashing and verification utilities.
- **RandomUtil.java**: Utilities for generating random values (e.g. secure tokens).

#### util/security/log/

Application-level logging helpers.

- **ApplicationLogger.java**: Main logger for the application; configures appenders and logging behaviour.
- **AttendanceLogger.java**: Logger focused on attendance-related events.
- **BaseLogger.java**: Common base for concrete loggers (formatting, file naming).
- **LoggerFacade.java**: Facade used by other parts of the app to log without depending on a specific logger implementation.

#### util/validation/

Input validation utilities and helpers for enforcing business rules.

- **AuthValidator.java**: Validates login/registration/verification-related inputs.
- **ProfileValidator.java**: Validates profile update inputs (names, phone numbers).
- **ValidationResult.java**: Represents the result of a validation (valid flag plus messages).
- **Validator.java**: Generic validator interface for implementing field or object validations.

---

### ApplicationContext.java

Central dependency injection / wiring container that manages singleton instances of repositories, services, and shared utilities.

### MainApp.java

JavaFX application entry point; bootstraps configuration, loads the initial FXML, and starts the UI.

## Key Features

- **Facial Recognition**: Automated attendance marking using OpenCV-based facial recognition
- **User Management**: Role-based access control (Student, Instructor, Admin)
- **Course Management**: Create and manage courses with enrollment tracking
- **Attendance Tracking**: Record and monitor student attendance
- **Report Generation**: Export attendance reports in multiple formats (PDF)
- **Email Notifications**: Send attendance notifications to students
- **User Profiles**: Manage student and instructor profile information
- **Session Management**: Track user sessions and authentication

## Dependencies

### Runtime Dependencies

| Dependency             | Version  | Purpose                                                        |
| ---------------------- | -------- | -------------------------------------------------------------- |
| JavaFX Controls        | 21.0.3   | Core UI controls and layout components                         |
| JavaFX FXML            | 21.0.3   | FXML-based UI loading for JavaFX screens                       |
| JavaFX Swing           | 21.0.3   | Integration bridge between Swing and JavaFX (if needed)        |
| ControlsFX             | 11.2.1   | Additional JavaFX controls (dialogs, notifications, etc.)      |
| OpenCV                 | 4.9.0-0  | Computer vision utilities (face detection / recognition)       |
| Apache PDFBox          | 2.0.30   | Base PDF report generation and manipulation                    |
| iText                  | 5.5.13.3 | Extra PDF decoration (styling, headers/footers)                |
| Apache POI (poi-ooxml) | 5.2.5    | Exporting reports to Excel (XLSX)                              |
| Jakarta Mail           | 2.0.1    | Sending reports via email (SMTP)                               |
| Jakarta Activation     | 2.0.1    | Handling email attachments and MIME types                      |
| PostgreSQL JDBC        | 42.6.0   | PostgreSQL database driver (JDBC)                              |
| dotenv-java            | 2.3.2    | Loading configuration from `.env` into environment variables   |
| SLF4J Simple           | 2.0.13   | Lightweight logging backend used by PDFBox and other libraries |

## Installation and Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+

### Building the Project

```bash
mvn clean package
```

### Running the Application

```bash
mvn javafx:run
```

## Configuration

### ENV

Copy `.env.example` file in the project root and change it to `.env`. Modify according to user or system requirements.

### Config Properties

Copy `config.properties.example` file in the project root and change it to `config.properties`. Modify according to user or system requirements.

## Architecture

The application follows a three-layer architecture:

1. **Controller Layer**: Handles UI interactions via JavaFX
2. **Service Layer**: Contains business logic and validation
3. **Repository Layer**: Manages data access and database operations

This separation ensures loose coupling, testability, and maintainability.

## Application Features

### 1. Authentication & Access Control

- **Login Page**

  - First screen on startup; routes users to either the **Student** or **Admin** interface based on their role.
  - Shows validation messages for invalid credentials or configuration issues. :contentReference[oaicite:0]{index=0}

- **User Registration & Verification**

  - Registration is only valid if an admin has invited the user (email-based flow).
  - A **verification table** in the database stores tokens for email verification and password reset.
  - Verification emails are sent with unique links so new users can activate their account.

- **Forgot Password**
  - Users can request a password reset link sent to their email.
  - Uses the same verification token mechanism for secure password updates.

### 2. Student Portal

The student interface is organized into tabs for **Face Capture**, **My Attendance**, and **My Profile**.

#### 2.1 Face Capture Tab

- Allows students to **enrol their face** for automated attendance.
- Shows a status line (e.g. _“Camera not started”_) to guide the student.
- Large camera preview area with a **“Start Camera”** button.
- Step-by-step instructions explain the coloured rectangles:
  - **Green**: exactly one face detected.
  - **Orange**: no face detected.
  - **Red**: multiple faces detected.

#### 2.2 My Attendance Tab

- Filters at the top:
  - **Course** selector.
  - **Date range** (From / To).
  - Reset button to clear filters. :contentReference[oaicite:1]{index=1}
- **Course Attendance Summary** panel:
  - Course name, attended count, total classes, and percentage bar.
- Detailed **attendance history table**:
  - Columns for date, course code, course name, and status (Present / Absent / Late / Pending).
  - Shows total number of records matching the current filters.
  - **Colour-coded statuses** for easier visual scanning.

#### 2.3 My Profile Tab

- Used to create or update the student’s profile.
- If no profile exists, shows a message plus a **“Create Profile”** button.
- Editable fields: first name, last name, phone number.
- **Save Changes** and **Cancel** buttons to confirm or discard edits.

### 3. Admin Portal

The admin interface contains multiple tabs: **Dashboard**, **Students**, **Enrolments**, **Sessions**, **Attendance**, **Live Recognition**, **Reports**, **Settings**, and **Profile**.

#### 3.1 Dashboard Tab

- Quick filters: **From/To** date range, **Course**, **Group**, and status checkboxes (On-time, Late, Absent, Excused), plus **Reset**. :contentReference[oaicite:2]{index=2}
- Top summary cards show key KPIs:
  - Total Students
  - Total Sessions
  - **Present Today**
- Charts and analytics:
  - **“Attendance (This Week)”** chart by day.
  - **Arrival Time Distribution**.
  - **Today’s Status Split**.
  - **Attendance by Course**.
- Each chart has:
  - **Zoom button**: opens a close-up of the chart in a separate window.
  - **Download button**: saves the chart as an image file.
  - **Tooltips**: shows numbers and categories when the chart is hovered upon.

#### 3.2 Students Tab

- Admin view for managing students.
- Displays student list and basic info (ID, name, etc.).
- Supports viewing and editing student details via the **Student Profile** dialog.

#### 3.3 Enrolments Tab

- Shows which students are enrolled in which courses.
- Typical columns: Student ID, Name, Course.
- Used to confirm that a student belongs to a course before their attendance is recorded.

#### 3.4 Sessions Tab

- Lists all scheduled/created class sessions:
  - Columns include Session ID, Course, Date, Start, End, Location, Late Threshold, Status, Auto Start, Auto Stop, and an **Action / View More** button.
- Actions:
  - **Create Session**: create one or more sessions using form inputs.
  - **Delete**: remove selected sessions.
  - **Start** / **Stop**: manually open/close sessions. Once closed, a session is considered expired and cannot be reopened.
  - **Auto Start / Auto Stop**: enable automatic opening/closing at configured start/end times.
  - **View More**: opens detailed attendance records for that session.

#### 3.5 Attendance Tab

- Shows **session-level attendance** in a table:
  - Columns: Select, Student ID, Name, Status, Method, Marked At, Last Seen, Note.
- Features:
  - **Create**: add attendance for students who enrolled after the session was created.
  - **Delete**: remove one or more records.
  - **Save Changes**: update Status and Note; other fields are read-only.
  - **Clear Edit**: reverts all unsaved changes in the attendance table back to their original values and resets the edit state.
  - **Back**: navigate back to session view page.
- Implements patterns such as:
  - `AttendanceRecord` entities with proper encapsulation.
  - `AttendanceMarker` interface with multiple implementations (e.g. auto vs manual) for polymorphic marking.
  - Observer pattern to update the roster view when attendance changes.

#### 3.6 Live Recognition Tab

- Accessible only when there is at least one **active session** (session-based access restriction).
- Shows:
  - **Detected face** preview and confidence value.
  - **Recognition history** table showing both high- and low-confidence detections; can be cleared.
- Threshold logic:
  - **Upper confidence threshold** (e.g. > 70%): automatically marks attendance.
  - **Lower confidence threshold** (e.g. < 30%): shows an alert for unknown faces.
- Designed for real-time marking during a running session.

#### 3.7 Reports Tab

- Filters:
  - Date range (From/To) or **Latest slot**.
  - Session, Course, Status, Method, and Confidence filters.
  - **Reset filters** button to clear them.
- Export:
  - Generate reports based on the current filters in **CSV**, **Excel (XLSX)**, or **PDF**.
- Field selection:
  - Choose “Select All” or toggle individual columns such as date/time, session, student info, course, method, confidence, status, and notes.
- Email integration:
  - Enter recipient, subject, and message.
  - Send the generated report directly as an email attachment (PDF/CSV/Excel).
- Footer:
  - Message area used to display status / feedback from report operations. :contentReference[oaicite:3]{index=3}

#### 3.8 Settings Tab

- Configuration form for system-level values:
  - Camera index.
  - Recognition algorithm (e.g. histogram vs OpenFace).
  - Recognition threshold.
  - Late threshold (minutes).
  - Database path / connection.
  - Enrollment image count (number of face images captured per student).
- Changes are persisted (config file or DB) and reused at next startup.

#### 3.9 Admin Profile Tab

- Same layout as student profile page for consistency.
- Lets the admin update their own first name, last name, and phone number.
- Buttons: **Save Changes**, **Cancel**.

### 4. System Configuration & Behaviour

- Uses **dotenv-java** to load `.env` into environment variables for database, email, and camera settings.
- Additional defaults and thresholds are stored in `config.properties`.
- On startup, the app:
  1. Loads configuration from `.env` and properties.
  2. Validates critical settings (e.g. DB connectivity).
  3. Shows JavaFX alerts if configuration is invalid and stops before login to prevent runtime errors.

### 5. Extra Features & Design Highlights

- **Light / Dark Mode**

  - Toggle button at the top-right corner switches between themes.
  - Both themes are supported across main pages (Dashboard, Reports, etc.).

- **Auto Session Policies (Rules Engine)**

  - A family of rule classes implementing a common `AutoSessionRule` interface.
  - Rules wrap each other (decorator style), adding extra conditions such as:
    - Session status checks.
    - Overlap / conflict prevention.
    - Time-based validation (e.g. start/end times).
  - This makes auto-start/stop logic extensible without changing core session code.

- **OOP & Architecture**
  - Encapsulation in entities like `Session` and `AttendanceRecord`.
  - Polymorphism in attendance marking strategies and rule sets.
  - MVC separation between **Model**, **View** (FXML), and **Controller**.
  - Observer pattern used to keep GUI tables in sync with attendance events.

## Extra Features (Beyond Base Requirements)

Compared to a basic attendance system, this project includes:

- **Advanced Reporting**

  - Export to **PDF, CSV, and Excel (XLSX)** with selectable fields.
  - One-click **email sending** of generated reports as attachments with input field for email text content.

- **Dashboard & Analytics**

  - KPI cards (students, sessions, present today).
  - Multiple charts with **zoom** and **download as image** functions.
  - Central `ChartStyler` and `ChartExporter` utilities for consistent look and reusability.

- **Auto Session Rules**

  - Pluggable rule classes (decorator pattern) to validate auto start/stop logic.
  - Prevents invalid or overlapping sessions without hard-coding conditions.

  - **Secure Token-Based Verification System**
  - Uses a dedicated verification table to store one-time tokens linked to users and expiry timestamps.
  - **Registration verification**: new accounts remain inactive until the user clicks a unique link sent to their email, preventing fake or mistyped accounts.
  - **Password reset**: the "Forgot Password" flow issues a time-limited reset token; the system validates the token and expiry before allowing the password change.

  - **Controlled Onboarding and Enrolment Flow**
  - Users can only register successfully if they have been invited / recognised by the admin, ensuring that only valid students and staff enter the system.
  - Student records and user accounts are linked so that only enrolled students can appear in course attendance lists.
  - Admins manage enrolment via the **Enrolments** tab, ensuring that recognition and attendance marking are always tied to the correct course and cohort.

- **Dark / Light Mode**
  - Theme toggle that applies across main screens (Dashboard, Reports, etc.).

## Development Status

The project is actively under development with ongoing enhancements to recognition accuracy, user interface improvements, and additional reporting features.
