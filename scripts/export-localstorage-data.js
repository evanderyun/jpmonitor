// =================================================================
// DATA MIGRATION EXPORT SCRIPT
// PT JAVA PERSADA MANDIRI ERP - LocalStorage to PostgreSQL
// =================================================================

// Step 1: Run this script in your browser console (F12) 
// Step 2: Copy the output JSON
// Step 3: Save to a file (migration-data.json)
// Step 4: Use the import script on backend

console.log("🔍 Exporting LocalStorage Data...");

const dbKey = 'jpm-database';
const rawData = localStorage.getItem(dbKey);

if (!rawData) {
    console.error("❌ No data found in LocalStorage!");
} else {
    try {
        const db = JSON.parse(rawData);

        const exportData = {
            employees: db.employees || [],
            equipment: db.equipment || [],
            suppliers: db.suppliers || [],
            locations: db.locations || [],
            exported_at: new Date().toISOString(),
            source: 'LocalStorage',
            total_records: {
                employees: (db.employees || []).length,
                equipment: (db.equipment || []).length,
                suppliers: (db.suppliers || []).length,
                locations: (db.locations || []).length
            }
        };

        console.log("✅ Export Complete!");
        console.log("📊 Summary:", exportData.total_records);
        console.log("📋 Copy the JSON below:");
        console.log("=".repeat(60));
        console.log(JSON.stringify(exportData, null, 2));
        console.log("=".repeat(60));

        // Auto-download as file
        const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `jpm-migration-${Date.now()}.json`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        console.log("💾 File downloaded automatically!");

    } catch (error) {
        console.error("❌ Error parsing data:", error);
    }
}
