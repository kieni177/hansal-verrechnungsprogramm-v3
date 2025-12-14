# 🚀 Quick Start Guide

## Get Started in 3 Steps

### Step 1: Prerequisites
Make sure you have Docker installed and running:
- Download Docker Desktop from https://www.docker.com/products/docker-desktop
- Start Docker Desktop

#### Windows-Specific Setup
1. **Enable WSL 2** (recommended): Docker Desktop works best with WSL 2
   - Open PowerShell as Administrator and run:
     ```powershell
     wsl --install
     ```
   - Restart your computer
2. **Start Docker Desktop** and wait for it to fully initialize (system tray icon shows "Docker Desktop is running")
3. **Use PowerShell or Command Prompt** for all commands below

### Step 2: Start the Application
#### Option A: Using the start script (Linux/Mac)
```bash
./start.sh
```

#### Option B: Manual start (All platforms)
```bash
docker-compose up -d --build
```

### Step 3: Access the Application
Open your browser and go to:
- **Frontend**: http://localhost
- **API Docs**: http://localhost:8080/actuator/health

## ✅ Verify Installation

1. Check all services are running:
```bash
docker-compose ps
```

You should see 3 services running:
- `hansal-postgres` (database)
- `hansal-backend` (API)
- `hansal-frontend` (web app)

2. Test the backend API:

**Linux/Mac:**
```bash
curl http://localhost:8080/actuator/health
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

**Or simply open in browser:** http://localhost:8080/actuator/health

Expected response: `{"status":"UP"}`

## 📖 Using the Application

### 1. Dashboard
- Navigate to http://localhost
- View overview of products, orders, and invoices

### 2. Manage Products
- Click "Produkte" in the sidebar
- Add new products with name, price, description
- Edit or delete existing products
- Click column headers to sort the table
- Use the search field to filter products

### 3. Create Orders
- Click "Bestellungen" in the sidebar
- Create new orders for customers
- **Customer Autocomplete**: Start typing a customer name to see suggestions from previous orders
- When selecting a known customer, phone and address are auto-filled
- Add multiple products to each order
- View order totals and status
- Click column headers to sort, use filters to search

### 4. Manage Slaughters
- Click "Schlachtungen" in the sidebar
- Record new cattle slaughters with meat cuts
- Stock is automatically updated based on slaughter data
- Track available weight per product

### 5. Generate Invoices
- Click "Rechnungen" in the sidebar
- Generate invoice from any order
- **Overwrite existing**: If an invoice exists, you'll be asked to confirm overwriting
- **Batch Download**: Select multiple invoices and download as one combined PDF
- Download individual invoices as PDF
- Click column headers to sort, use filters to search

## 🛑 Stopping the Application

```bash
docker-compose down
```

To also remove all data:
```bash
docker-compose down -v
```

## 🔧 Common Issues

### Port Already in Use
If port 80 or 8080 is already in use, modify `docker-compose.yml`:
```yaml
services:
  frontend:
    ports:
      - "3000:80"  # Change 3000 to any available port
  backend:
    ports:
      - "8081:8080"  # Change 8081 to any available port
```

### Service Not Starting
View logs to diagnose:
```bash
docker-compose logs backend
docker-compose logs frontend
docker-compose logs postgres
```

### Database Connection Issues
Wait a bit longer for PostgreSQL to fully start:
```bash
docker-compose restart backend
```

### Windows-Specific Issues

#### Docker Desktop Not Starting
- Ensure **Hyper-V** and **WSL 2** are enabled
- Open PowerShell as Administrator:
  ```powershell
  dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
  dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
  ```
- Restart your computer

#### "docker-compose" Command Not Found
- Use `docker compose` (with a space) instead of `docker-compose` on newer Docker Desktop versions:
  ```powershell
  docker compose up -d --build
  docker compose ps
  docker compose down
  ```

#### Port 80 Blocked by Windows Services
Port 80 may be used by IIS or other Windows services:
```powershell
# Check what's using port 80
netstat -ano | findstr :80

# Stop IIS if running
iisreset /stop
```
Or change the frontend port in `docker-compose.yml` (see "Port Already in Use" above)

#### Firewall Blocking Access
If you can't access localhost:
1. Open **Windows Defender Firewall**
2. Click **Allow an app through firewall**
3. Ensure **Docker Desktop** is allowed for private networks

#### Line Ending Issues (CRLF vs LF)
If you get script errors, configure Git to use LF:
```powershell
git config --global core.autocrlf input
```

## 🔄 Updating the Application

If the application is already running and you have new code:

```bash
cd hansal-verrechnungsprogramm-v3
docker-compose down                    # Stop (keeps data!)
git pull origin master                 # Get new code (or copy files)
docker-compose up -d --build           # Rebuild and start
docker-compose ps                      # Verify running
```

> **Warning**: Never use `docker-compose down -v` during updates - this deletes your database!

For detailed update instructions, see the [README.md](README.md#-updating-an-existing-installation).

## 📚 Next Steps

1. Read the full [README.md](README.md) for detailed documentation
2. Explore the API endpoints
3. Customize the application for your needs
4. Add your own business logic

## 💡 Tips

- The database is persistent - your data will survive restarts
- Use `docker-compose logs -f` to watch logs in real-time
- Press `Ctrl+C` to stop watching logs
- Backend API documentation is available at the `/api` endpoints

## 🎯 Test Data

The application starts with an empty database. Create test data:

1. **Add a Product**:
   - Go to Products → Add Product
   - Name: "Sample Product"
   - Price: 99.99
   - Description: "Test product"

2. **Create an Order**:
   - Go to Orders → Create Order
   - Customer Name: "John Doe"
   - Add the product you created
   - Save the order

3. **Generate Invoice**:
   - Go to Invoices
   - Create invoice from your order
   - Download as PDF

## 🆘 Need Help?

- Check the [README.md](README.md) for detailed documentation
- Review Docker logs: `docker-compose logs -f`
- Verify all services are running: `docker-compose ps`
- Restart services: `docker-compose restart`

---

**Happy Coding! 🎉**
