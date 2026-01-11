# iKistro - IKEA Store Management System

iKistro is a comprehensive Java-based desktop application designed to simulate an IKEA store management system. It provides a rich user interface for both customers and administrators, facilitating e-commerce operations such as product browsing, shopping cart management, wishlist curation, and order processing. The system leverages a dual-database architecture, utilizing both Oracle Database and MongoDB to handle structured and unstructured data efficiently.

## Features

### User Roles
*   **Customer**:
    *   **Browse Products**: Content-rich catalog with filtering and sorting options.
    *   **Search**: Find products by name or category.
    *   **Shopping Cart**: Add items, manage quantities, and checkout.
    *   **Wishlist**: Save favorite items for later.
    *   **Product Kits**: View and purchase curated bundles of products.
    *   **Reviews**: Seamless integration for reading and writing product reviews.
    *   **User Profile**: Manage shipping addresses and view order history.
*   **Administrator**:
    *   **Product Management**: Add, update, and remove products.
    *   **Inventory Control**: Monitor stock levels and manage replenishment.
    *   **Discount Management**: Create and manage promotional discount codes.
    *   **Category Management**: Organize products into improved hierarchies.
    *   **Bundle Management**: Create and modify product kits.

### Key Functionalities
*   **Dual Data Storage**:
    *   **Oracle Database**: Handles relational data like Users, Products, Orders, and Inventory.
    *   **MongoDB**: Manages unstructured or high-volume data such as Reviews and Logs.
*   **Search & Filtering**: Advanced search capabilities and category-based filtering.
*   **Dynamic UI**: A modern Swing-based interface with custom themes (`Theme.java`) and responsive layouts.
*   **Authentication**: Secure login and sign-up processes with role-based access control.

## Technology Stack

*   **Language**: Java (JDK 8+)
*   **GUI Framework**: Java Swing (AWT/Swing)
*   **Databases**:
    *   Oracle Database (JDBC)
    *   MongoDB (Mongo Java Driver)
*   **External Libraries**:
    *   `ojdbc8.jar` (Oracle JDBC Driver)
    *   `mongodb-driver-sync`, `mongodb-driver-core`, `bson` (MongoDB Drivers)
    *   `slf4j` (Logging facade)

## Project Structure

The project is organized into the following packages:

*   `ui`: Contains all Swing-based user interface classes (Frames, Panels, Dialogs).
*   `backend`: Service classes handling business logic and database interactions.
*   `models`: POJOs (Plain Old Java Objects) representing data entities (e.g., User, Product, Order).
*   `drivers`: Contains necessary JDBC and database drivers.

## Setup and Installation

### Prerequisites
1.  **Java Development Kit (JDK)**: Ensure JDK 8 or higher is installed.
2.  **Oracle Database**: A running instance of Oracle Database.
    *   Ensure the schema supports the tables required by `DataSeeder` or the backend services.
3.  **MongoDB**: A running instance of MongoDB.
4.  **Database Configuration**:
    *   Update database connection strings in `backend/OracleDB.java` and `backend/MongoDB.java` if necessary.

### Running the Application

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/yourusername/iKistro.git
    cd iKistro
    ```

2.  **Compile the Code**:
    Navigate to the source directory (e.g. `src`) and compile the Java files, ensuring the `drivers` folder is in the classpath.
    ```bash
    # Example from the project root
    javac -cp ".;drivers/*" -d bin src/backend/*.java src/models/*.java src/ui/*.java
    ```

3.  **Run the Application**:
    Start the application from the `RoleSelectionFrame`.
    ```bash
    # Example run command
    java -cp ".;bin;drivers/*" ui.RoleSelectionFrame
    ```

## Contributing
Contributions are welcome! Please fork the repository and submit a pull request for any enhancements or bug fixes.

## License
This project is licensed under the MIT License - see the LICENSE file for details.
