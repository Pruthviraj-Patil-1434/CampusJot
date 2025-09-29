# CampusJot 📝

### Collaborative Notes, Organized Campus Life

**CampusJot** is a web platform designed to streamline academic collaboration for students. It provides a centralized, organized, and collaborative space where students can create study groups, share notes, and manage course materials effectively, ensuring that important information is never missed.

---

## ✨ Key Features

- **👥 Collaborative Groups**: Create secure workspaces for your classes. Join a group with a unique Group Name and Group ID.
- **🗂️ Subject-Based Organization**: Organize your group's content into distinct folders for different subjects, keeping your workspace tidy and easy to navigate.
- **✍️ In-Browser Note Editor**: Create new text files and edit them directly in the browser with real-time collaboration.
- **📤 Upload Existing Files**: Easily upload existing documents, PDFs, and images to share with your group members.
- **🔐 Secure Verification System**: User accounts are protected by email and password verification, ensuring that only registered users can access the platform.

---

## 📸 Screenshots

A quick look at the user flow and interface of CampusJot.

`![Landing Page](CampusJot/CampusJot/outputs/home.png)`
`![User Home Page](CampusJot/CampusJot/outputs/userHome.png)`
`![Create Group ](CampusJot/CampusJot/outputs/createGroup.png)`
`![Group Members](CampusJot/CampusJot/outputs/GroupMembers.png)`

---

## 🛠️ Tech Stack

- **Backend**: Spring Boot
- **ORM**: Spring Data JPA / Hibernate
- **Frontend**: Thymeleaf (Server-Side Templating)
- **Database**: PostgreSQL
- **Cloud Storage**: Supabase
- **Build Tool**: Maven

---

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

- JDK 17 or later
- Maven
- A Supabase account for object storage
- A local or cloud-hosted PostgreSQL database

### Installation & Configuration

1.  **Clone the repository**

    ```bash
    git clone https://github.com/Pruthviraj-Patil-1434/CampusJot.git
    ```

2.  **Navigate to the project directory**

    ```bash
    cd CampusJot
    ```

3.  **Configure Environment Variables**
    For security and flexibility, the project is configured using environment variables rather than hardcoding credentials. Create a file named `.env` in the root directory or configure these variables in your IDE's run settings.

    - `SPRING_DATASOURCE_URL`: The JDBC URL for your PostgreSQL database.
      - _Example_: `jdbc:postgresql://localhost:5432/campusjot_db`
    - `SPRING_DATASOURCE_USERNAME`: Your database username.
    - `SPRING_DATASOURCE_PASSWORD`: Your database password.
    - `SUPABASE_URL`: Your Supabase project URL.
    - `SUPABASE_KEY`: Your Supabase public `anon` key.

    The `src/main/resources/application.properties` file is already set up to read these variables:

    ```properties
    # Database Configuration
    spring.datasource.url=${SPRING_DATASOURCE_URL}
    spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
    spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

    # Supabase Configuration
    supabase.url=${SUPABASE_URL}
    supabase.key=${SUPABASE_KEY}
    ```

4.  **Run the application locally**

    ```bash
    mvn spring-boot:run
    ```

    The application will be available at `http://localhost:8080`.

---

## Usage

1.  **Sign Up**: Create a new account using your email and a unique username.
2.  **Create or Join a Group**: After logging in, you can either create a new collaborative group and invite others or join an existing group using its name and ID.
3.  **Organize Content**: Inside a group, create folders for different subjects to keep notes organized.
4.  **Add and Collaborate**: Upload existing notes or create new ones in the text editor. Work with your group members in real-time.

---

## 👨‍💻 Author

**Team Phoenix**
