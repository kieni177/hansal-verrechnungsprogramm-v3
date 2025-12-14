# Hansal Verrechnungsprogramm v3

A full-stack invoice and order management system built with Angular 20 and Spring Boot 3.

## 📋 Features

- **Product Management**: Create, update, delete, and search products
- **Order Management**: Create and manage customer orders with multiple items
- **Customer Autocomplete**: Smart autocomplete for customer names with auto-fill of phone/address from previous orders
- **Invoice Generation**: Generate invoices from orders with PDF export
- **Invoice Overwrite**: Dialog confirmation when recreating invoices for orders
- **Batch PDF Download**: Download multiple invoices as a single combined PDF document
- **Table Sorting & Filtering**: Click column headers to sort, search/filter across all tables
- **Slaughter Management**: Track cattle slaughters and meat production with automatic stock updates
- **Admin Dashboard**: Overview of products, orders, and invoices
- **PDF Export**: Download invoices as professional PDF documents
- **RESTful API**: Complete backend API for all operations
- **Responsive UI**: Material Design interface
- **Database Migrations**: Liquibase-managed schema for safe updates

## 🛠️ Technology Stack

### Backend
- Java 21
- Spring Boot 3.2.1
- Spring Data JPA
- Spring Security
- PostgreSQL 16
- Liquibase (Database migrations)
- iText 8 (PDF generation)
- Maven

### Frontend
- Angular 20
- Angular Material
- TypeScript 5.7
- RxJS 7.8
- SCSS

### DevOps
- Docker & Docker Compose
- Nginx
- Multi-stage builds

## 🚀 Quick Start with Docker

### Prerequisites
- Docker (v20.10+)
- Docker Compose (v2.0+)

### Setting Up on a New PC

#### What You Need
1. **Required Software**
   - Docker (version 20.10 or higher)
   - Docker Compose (version 2.0 or higher)
   - Git (optional, for cloning)

2. **System Requirements**
   - Free ports: 80, 8080, 5432
   - Disk space: ~2-3 GB for Docker images
   - RAM: At least 4 GB recommended

3. **Files Required**
   - Complete project directory: `hansal-verrechnungsprogramm-v3/`
   - Transfer via Git clone or copy entire folder to new PC

#### Installation Steps

1. **Transfer the project to the new PC**
   - Option A: Clone via Git: `git clone <repository-url>`
   - Option B: Copy entire `hansal-verrechnungsprogramm-v3/` folder (USB, network, etc.)

2. **Navigate to project directory**
```bash
cd hansal-verrechnungsprogramm-v3
```

3. **Start all services** (first time will take 5-10 minutes to build)
```bash
docker-compose up -d
```

This will:
- Build the backend Spring Boot application
- Build the frontend Angular application
- Start PostgreSQL database
- Start Nginx web server
- Connect all services
- **Auto-restart containers after PC reboot** (restart policy enabled)

4. **Verify services are running**
```bash
docker-compose ps
```

5. **Access the application**
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080/api
- **Database**: localhost:5432

#### Managing the Application

**Stop all services** (containers will NOT restart on PC reboot)
```bash
docker-compose down
```

**Stop and remove all data** (deletes database)
```bash
docker-compose down -v
```

**Restart services**
```bash
docker-compose restart
```

**View logs**
```bash
docker-compose logs -f
```

#### Auto-Start Behavior

The application is configured with `restart: unless-stopped` policy:
- ✅ Containers automatically start after PC reboot
- ✅ Containers restart if they crash
- ✅ Database data persists across restarts
- ⚠️ Containers only stay stopped if you run `docker-compose down`

## 📁 Project Structure

```
hansal-verrechnungsprogramm-v3/
├── backend/                          # Spring Boot Backend
│   ├── src/
│   │   └── main/
│   │       ├── java/com/hansal/verrechnungsprogramm/
│   │       │   ├── config/          # Security & Configuration
│   │       │   ├── controller/      # REST Controllers
│   │       │   ├── model/           # JPA Entities
│   │       │   ├── repository/      # Data Repositories
│   │       │   ├── service/         # Business Logic
│   │       │   └── VerrechnungsprogrammApplication.java
│   │       └── resources/
│   │           └── application.properties
│   ├── Dockerfile                   # Backend Docker image
│   └── pom.xml                      # Maven dependencies
│
├── frontend/                        # Angular Frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/         # UI Components
│   │   │   │   ├── dashboard/
│   │   │   │   ├── products/
│   │   │   │   ├── orders/
│   │   │   │   ├── invoices/
│   │   │   │   └── login/
│   │   │   ├── models/             # TypeScript Interfaces
│   │   │   ├── services/           # API Services
│   │   │   ├── app.component.ts
│   │   │   ├── app.config.ts
│   │   │   └── app.routes.ts
│   │   ├── environments/           # Environment configs
│   │   ├── index.html
│   │   ├── main.ts
│   │   └── styles.scss
│   ├── Dockerfile                  # Frontend Docker image
│   ├── nginx.conf                  # Nginx configuration
│   ├── angular.json
│   ├── package.json
│   └── tsconfig.json
│
└── docker-compose.yml              # Orchestration file
```

## 🔧 Local Development (without Docker)

### Backend Setup

1. **Prerequisites**
   - Java 21
   - Maven 3.9+
   - PostgreSQL 16 (or use H2 in-memory)

2. **Configure Database** (edit `backend/src/main/resources/application.properties`)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hansal_db
spring.datasource.username=hansal_user
spring.datasource.password=hansal_password
```

3. **Run Backend**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs on: http://localhost:8080

### Frontend Setup

1. **Prerequisites**
   - Node.js 20+
   - npm 10+

2. **Install Dependencies**
```bash
cd frontend
npm install
```

3. **Run Frontend**
```bash
npm start
```

Frontend runs on: http://localhost:4200

## 📡 API Endpoints

### Products
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?name={name}` - Search products
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Orders
- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/customers` - Get unique customers (for autocomplete)
- `GET /api/orders/search?customerName={name}` - Search orders
- `GET /api/orders/status/{status}` - Get orders by status
- `POST /api/orders` - Create order
- `PUT /api/orders/{id}` - Update order
- `PATCH /api/orders/{id}/status?status={status}` - Update order status
- `DELETE /api/orders/{id}` - Delete order

### Invoices
- `GET /api/invoices` - Get all invoices
- `GET /api/invoices/{id}` - Get invoice by ID
- `GET /api/invoices/number/{number}` - Get invoice by number
- `GET /api/invoices/by-order/{orderId}` - Get invoice by order ID
- `POST /api/invoices/from-order/{orderId}` - Create invoice from order
- `POST /api/invoices/batch/pdf` - Download multiple invoices as combined PDF
- `PUT /api/invoices/{id}` - Update invoice
- `DELETE /api/invoices/{id}` - Delete invoice
- `GET /api/invoices/{id}/pdf` - Download invoice PDF

### Health Check
- `GET /actuator/health` - Application health status

## 🗄️ Database Schema

The application uses JPA with automatic schema generation. Main entities:

- **Product**: Product catalog
- **Order**: Customer orders
- **OrderItem**: Line items in orders
- **Invoice**: Generated invoices
- **User**: Admin users

## 🔐 Security

The application uses Spring Security with JWT authentication (configured but simplified for demo).

Default settings:
- Most endpoints require authentication
- `/api/auth/**` endpoints are public
- Actuator endpoints are public
- CORS enabled for development

## 🐳 Docker Configuration

### Services

1. **PostgreSQL** (`postgres`)
   - Port: 5432
   - Database: `hansal_db`
   - User: `hansal_user`
   - Password: `hansal_password`

2. **Backend** (`backend`)
   - Port: 8080
   - Depends on: postgres
   - Health check enabled

3. **Frontend** (`frontend`)
   - Port: 80
   - Depends on: backend
   - Serves static files via Nginx
   - Proxies API requests to backend

### Volumes
- `postgres_data`: Persists database data

### Networks
- `hansal-network`: Bridge network connecting all services

## 📊 Data Flow

```
User → Nginx (Frontend) → Angular App
                ↓
        API Request (/api/*)
                ↓
        Nginx Proxy → Backend (Spring Boot)
                ↓
        Database (PostgreSQL)
```

## 🎨 Customization

### Change Database Configuration
Edit `docker-compose.yml`:
```yaml
environment:
  POSTGRES_DB: your_db_name
  POSTGRES_USER: your_user
  POSTGRES_PASSWORD: your_password
```

### Change Ports
Edit `docker-compose.yml` ports section:
```yaml
ports:
  - "3000:80"    # Frontend
  - "8081:8080"  # Backend
```

### Modify API Base URL
For production, update `frontend/src/environments/environment.prod.ts`:
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://your-domain.com/api'
};
```

## 🔍 Monitoring & Logs

### View logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### Check service status
```bash
docker-compose ps
```

### Health check
```bash
curl http://localhost:8080/actuator/health
```

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 📝 Default Data

The application starts with an empty database. You can:
1. Create products through the UI
2. Create orders referencing those products
3. Generate invoices from orders
4. Download invoices as PDFs

## 🛠️ Troubleshooting

### Backend won't start
- Check if PostgreSQL is running: `docker-compose ps`
- View backend logs: `docker-compose logs backend`
- Ensure port 8080 is not in use

### Frontend shows connection errors
- Ensure backend is running and healthy
- Check backend URL in environment files
- Clear browser cache

### Database connection issues
- Wait for postgres health check to pass
- Check database credentials in docker-compose.yml
- Verify network connectivity between containers

### PDF generation fails
- Check backend logs for iText errors
- Ensure order has items before creating invoice

## 🔄 Updating an Existing Installation

If the application is already running on a PC and you need to update it with new code:

### Step-by-Step Update Guide

#### 1. Transfer the Updated Code

**Option A: Using Git (Recommended)**
```bash
cd hansal-verrechnungsprogramm-v3
git pull origin master
```

**Option B: Manual File Copy**
- Copy the updated `hansal-verrechnungsprogramm-v3/` folder to the PC
- Replace the existing folder (or merge changes)

#### 2. Stop the Running Application
```bash
cd hansal-verrechnungsprogramm-v3
docker-compose down
```

> **Important**: Do NOT use `docker-compose down -v` as this will delete your database!

#### 3. Rebuild and Start with New Code
```bash
docker-compose up -d --build
```

This will:
- Rebuild the backend with new Java code
- Rebuild the frontend with new Angular code
- Run any new Liquibase database migrations automatically
- Restart all services

#### 4. Verify the Update
```bash
# Check all services are running
docker-compose ps

# Check backend health
curl http://localhost:8080/actuator/health

# View logs if needed
docker-compose logs -f
```

#### 5. Access the Updated Application
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080/api

### Quick Update Commands (Copy & Paste)

**Full Update Sequence:**
```bash
cd hansal-verrechnungsprogramm-v3
docker-compose down
git pull origin master  # or copy new files
docker-compose up -d --build
docker-compose ps
```

### What Happens During Update

| Component | Action |
|-----------|--------|
| **Database** | Data is preserved. New Liquibase migrations run automatically. |
| **Backend** | Rebuilt from source. New endpoints available immediately. |
| **Frontend** | Rebuilt from source. New UI features available after refresh. |
| **Configuration** | docker-compose.yml changes take effect. |

### Troubleshooting Updates

**Build fails with errors:**
```bash
# View build logs
docker-compose logs backend
docker-compose logs frontend

# Try a clean rebuild
docker-compose build --no-cache
docker-compose up -d
```

**Database migration fails:**
```bash
# Check Liquibase logs
docker-compose logs backend | grep -i liquibase

# If migration is stuck, check the databasechangeloglock table
docker-compose exec postgres psql -U hansal_user -d hansal_db -c "SELECT * FROM databasechangeloglock;"

# Release stuck lock if needed
docker-compose exec postgres psql -U hansal_user -d hansal_db -c "UPDATE databasechangeloglock SET locked=false;"
```

**Frontend not showing new features:**
- Clear browser cache (Ctrl+Shift+R or Cmd+Shift+R)
- Try incognito/private browsing mode

**Rolling back to previous version:**
```bash
# If using Git
git checkout <previous-commit-hash>
docker-compose up -d --build

# If using file copies, restore from backup
```

### Backup Before Update (Recommended)

**Backup Database:**
```bash
docker-compose exec postgres pg_dump -U hansal_user hansal_db > backup_$(date +%Y%m%d).sql
```

**Restore Database (if needed):**
```bash
cat backup_20241214.sql | docker-compose exec -T postgres psql -U hansal_user hansal_db
```

---

## 🚀 Production Deployment

For production:

1. **Update environment variables**
2. **Use strong passwords and secrets**
3. **Enable HTTPS**
4. **Configure proper CORS origins**
5. **Set up database backups**
6. **Use external PostgreSQL instance**
7. **Enable production logging**
8. **Configure JWT secret properly**

## 📄 License

This project is for internal use by Hansal.

## 👥 Contributors

- Development Team

## 📞 Support

For issues and questions, please contact the development team.

---

**Version**: 3.1.0
**Last Updated**: December 2024

### Changelog

#### v3.1.0 (December 2024)
- Added customer autocomplete with auto-fill of phone/address
- Added batch PDF download for multiple invoices
- Added table sorting (click column headers) and filtering for all tables
- Added invoice overwrite confirmation dialog
- Added Liquibase database migration system
- Improved table sorting with ViewChild setter pattern

#### v3.0.0 (October 2024)
- Initial release with full product, order, and invoice management
- PDF invoice generation
- Docker deployment
