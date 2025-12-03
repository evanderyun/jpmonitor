<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://github.com/user-attachments/assets/0aa67016-6eaf-458a-adb2-6e31a0763ed6" />
</div>

# PT Java Persada Mandiri ERP System

Complete ERP system for mining operations with **automatic data persistence**.

> **✅ Data Persistence Enabled**: All your data is automatically saved to browser localStorage and persists across page refreshes!

## Features

- 📊 **Fleet Management** - Equipment tracking, maintenance, work orders
- 📦 **Inventory Management** - Spare parts, transactions, stock control
- 👥 **Employee Management** - Staff records, timesheets
- 🚚 **Logistics** - Asset mutations, goods shipments
- 📈 **Production Tracking** - Daily production records, analytics
- 🔒 **Audit Trail** - Complete transaction history
- 💾 **Auto-Save** - All changes automatically saved (no manual save needed!)

## Run Locally

**Prerequisites:** Node.js 18+

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Set the Gemini API key** (optional, for AI features):
   Edit [.env.local](.env.local):
   ```
   GEMINI_API_KEY=your_actual_api_key_here
   ```

3. **Run the app:**
   ```bash
   npm run dev
   ```
   
   Open [http://localhost:3000](http://localhost:3000) in your browser.

## Data Persistence

### How It Works

- **Auto-Save**: All data changes are automatically saved to browser localStorage
- **Auto-Load**: Data is automatically loaded when you open the app
- **No Data Loss**: Refresh the page anytime - your data persists!
- **Storage Size**: Currently ~11 KB (well below browser limits)

### Storage Information

Check storage stats in browser console:
```javascript
db.getStorageInfo()
// Returns: { exists: true, size: "11.04 KB", timestamp: "...", age: "..." }
```

### Reset Data

To reset all data to initial seed data:
```javascript
db.resetToSeedData()  // Clears localStorage and reloads page
```

Or manually clear via DevTools:
1. Open DevTools (F12)
2. Application → Local Storage
3. Delete `jpm_erp_database` key
4. Refresh page

## Limitations

> [!WARNING]
> **Current Implementation**: This uses browser localStorage (Option 1 - Quick Fix)
> 
> - ✅ Perfect for: Demo, development, single-user usage
> - ❌ Not suitable for: Multi-user production, team collaboration
> - 📦 Data stored locally in browser only (not synchronized across devices)

## Future Migration

For production deployment with multi-user support, see migration plan to **Option 2 (Backend + PostgreSQL)** in the project documentation.

## Technology Stack

- **Frontend**: React 19, TypeScript, Vite
- **Charts**: Recharts
- **Icons**: Lucide React
- **Persistence**: Browser localStorage (auto-save enabled)

## Documentation

View your app in AI Studio: https://ai.studio/apps/drive/1huTP9rvkeALaVxN7mbQKVk606eG_mx2U

---

**Status**: ✅ Ready for Demo & Development  
**Data Persistence**: ✅ Enabled  
**Last Updated**: 2025-11-24
